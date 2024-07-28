package org.enginehub.linbus.gui.javafx;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableCell;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;
import org.enginehub.linbus.gui.util.ErrorReporter;
import org.enginehub.linbus.gui.util.PluralizerRule;
import org.enginehub.linbus.tree.LinByteArrayTag;
import org.enginehub.linbus.tree.LinByteTag;
import org.enginehub.linbus.tree.LinCompoundTag;
import org.enginehub.linbus.tree.LinDoubleTag;
import org.enginehub.linbus.tree.LinEndTag;
import org.enginehub.linbus.tree.LinFloatTag;
import org.enginehub.linbus.tree.LinIntArrayTag;
import org.enginehub.linbus.tree.LinIntTag;
import org.enginehub.linbus.tree.LinListTag;
import org.enginehub.linbus.tree.LinLongArrayTag;
import org.enginehub.linbus.tree.LinLongTag;
import org.enginehub.linbus.tree.LinShortTag;
import org.enginehub.linbus.tree.LinStringTag;
import org.enginehub.linbus.tree.LinTag;
import org.jspecify.annotations.Nullable;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Based on {@link javafx.scene.control.cell.TextFieldTreeTableCell} but adapted to allow a graphic.
 */
class ValueEditableCell extends TreeTableCell<NbtTreeView.TagEntry, NbtTreeView.TagEntry> {
    private static final PluralizerRule ENTRIES = new PluralizerRule("entry", "entries");
    private static final PluralizerRule BYTES = new PluralizerRule("byte", "bytes");
    private static final PluralizerRule INTS = new PluralizerRule("int", "ints");
    private static final PluralizerRule LONGS = new PluralizerRule("long", "longs");

    private static FxFonts.AndString getFontTextForValue(NbtTreeView.TagEntry item) {
        return switch (item.tag()) {
            case LinCompoundTag tag ->
                new FxFonts.AndString(FxFonts.ITALIC, ENTRIES.applyWithCount(tag.value().size()));
            case LinListTag<?> tag -> new FxFonts.AndString(FxFonts.ITALIC, ENTRIES.applyWithCount(tag.value().size()));
            case LinByteArrayTag byteArrayTag -> bufferToString(byteArrayTag.view(), BYTES, ByteBuffer::get);
            case LinIntArrayTag intArrayTag -> bufferToString(intArrayTag.view(), INTS, IntBuffer::get);
            case LinLongArrayTag longArrayTag -> bufferToString(longArrayTag.view(), LONGS, LongBuffer::get);
            case LinEndTag tag -> new FxFonts.AndString(FxFonts.ITALIC, "End");
            default -> new FxFonts.AndString(FxFonts.DEFAULT, item.tag().value().toString());
        };
    }

    @FunctionalInterface
    private interface BufferGetElement<B extends Buffer, T> {
        T apply(B buffer, int index);
    }

    private static <B extends Buffer> FxFonts.AndString bufferToString(
        B buffer, PluralizerRule rule, BufferGetElement<B, ?> getElement
    ) {
        if (buffer.remaining() > 10) {
            return new FxFonts.AndString(FxFonts.ITALIC, rule.applyWithCount(buffer.remaining()));
        }
        return new FxFonts.AndString(
            FxFonts.DEFAULT,
            IntStream.range(0, buffer.remaining())
                .mapToObj(i -> getElement.apply(buffer, i).toString())
                .collect(Collectors.joining(", ", "[", "]"))
        );
    }

    private static boolean isSimpleValueTag(LinTag<?> tag) {
        return !(
            tag instanceof LinCompoundTag ||
                tag instanceof LinListTag ||
                tag instanceof LinByteArrayTag ||
                tag instanceof LinIntArrayTag ||
                tag instanceof LinLongArrayTag ||
                tag instanceof LinEndTag
        );
    }

    private sealed interface EditableGraphic {

        Node node();

        TextField textField();

        record Text(TextField textField) implements EditableGraphic {
            @Override
            public Node node() {
                return textField;
            }
        }

        record LongSpinner(Spinner<Long> spinner) implements EditableGraphic {
            @Override
            public Node node() {
                return spinner;
            }

            @Override
            public TextField textField() {
                return spinner.getEditor();
            }
        }

        record DoubleSpinner(Spinner<Double> spinner) implements EditableGraphic {
            @Override
            public Node node() {
                return spinner;
            }

            @Override
            public TextField textField() {
                return spinner.getEditor();
            }
        }
    }

    private @Nullable EventHandler<MouseEvent> doubleClickHandler;
    private @Nullable EditableGraphic editableGraphic;

    private void initializeNonEditing() {
        NbtTreeView.TagEntry item = getItem();
        if (item == null) {
            throw new IllegalStateException("Item should not be null");
        }
        FxFonts.AndString fontText = getFontTextForValue(item);
        setText(fontText.string());
        setFont(fontText.font());
        setGraphic(null);
    }

    private void initializeEditing(EditableGraphic nonNullEditableGraphic) {
        NbtTreeView.TagEntry item = getItem();
        if (item == null) {
            throw new IllegalStateException("Item should not be null");
        }
        switch (item.tag()) {
            case LinByteTag tag -> ((EditableGraphic.LongSpinner) nonNullEditableGraphic).spinner
                .getValueFactory().setValue((long) tag.value());
            case LinDoubleTag tag -> ((EditableGraphic.DoubleSpinner) nonNullEditableGraphic).spinner
                .getValueFactory().setValue(tag.value());
            case LinFloatTag tag -> ((EditableGraphic.DoubleSpinner) nonNullEditableGraphic).spinner
                .getValueFactory().setValue((double) tag.value());
            case LinIntTag tag -> ((EditableGraphic.LongSpinner) nonNullEditableGraphic).spinner
                .getValueFactory().setValue((long) tag.value());
            case LinLongTag tag -> ((EditableGraphic.LongSpinner) nonNullEditableGraphic).spinner
                .getValueFactory().setValue(tag.value());
            case LinShortTag tag -> ((EditableGraphic.LongSpinner) nonNullEditableGraphic).spinner
                .getValueFactory().setValue((long) tag.value());
            case LinStringTag tag -> ((EditableGraphic.Text) nonNullEditableGraphic).textField
                .setText(tag.value());
            default -> throw new IllegalStateException("Un-editable tag type: " + item.tag());
        }
        setText(null);
        setGraphic(nonNullEditableGraphic.node());
    }

    @Override
    protected void updateItem(NbtTreeView.@Nullable TagEntry item, boolean empty) {
        if (item == getItem()) {
            return;
        }

        super.updateItem(item, empty);

        if (doubleClickHandler != null && (item == null || isSimpleValueTag(item.tag()))) {
            // Clear handler for arrays
            removeEventHandler(MouseEvent.MOUSE_CLICKED, doubleClickHandler);
            doubleClickHandler = null;
        }

        if (item == null) {
            setText(null);
            setGraphic(null);
            editableGraphic = null;
            setEditable(false);
            return;
        }

        if (isEditing()) {
            if (editableGraphic == null) {
                throw new IllegalStateException("Graphic should not be null when editing");
            }

            initializeEditing(editableGraphic);
        } else {
            initializeNonEditing();
        }

        // We can be edited now if we're a simple value tag
        if (isSimpleValueTag(item.tag())) {
            setEditable(true);
        } else {
            setEditable(false);

            if (item.tag() instanceof LinLongArrayTag ||
                item.tag() instanceof LinIntArrayTag ||
                item.tag() instanceof LinByteArrayTag) {
                // These are editable, but not directly. We use a popup for them.
                if (doubleClickHandler == null) {
                    doubleClickHandler = event -> {
                        if (event.getClickCount() == 2) {
                            BufferObservableList<? extends Number> buffer = switch (item.tag()) {
                                case LinByteArrayTag byteArrayTag -> new BufferObservableList.OfByte(
                                    ByteBuffer.wrap(byteArrayTag.value())
                                );
                                case LinIntArrayTag intArrayTag -> new BufferObservableList.OfInt(
                                    IntBuffer.wrap(intArrayTag.value())
                                );
                                case LinLongArrayTag longArrayTag -> new BufferObservableList.OfLong(
                                    LongBuffer.wrap(longArrayTag.value())
                                );
                                default -> throw new AssertionError("Unreachable");
                            };
                            new ArrayEditSetup<>(buffer).showForUpdate().thenAccept(changesAccepted -> {
                                if (!changesAccepted) {
                                    return;
                                }

                                applyUpdate(switch (item.tag()) {
                                    case LinByteArrayTag __ -> LinByteArrayTag.of(
                                        ((BufferObservableList.OfByte) buffer).buffer().array()
                                    );
                                    case LinIntArrayTag __ -> LinIntArrayTag.of(
                                        ((BufferObservableList.OfInt) buffer).buffer().array()
                                    );
                                    case LinLongArrayTag __ -> LinLongArrayTag.of(
                                        ((BufferObservableList.OfLong) buffer).buffer().array()
                                    );
                                    default -> throw new AssertionError("Unreachable");
                                });
                            });
                        }
                    };
                }
                addEventHandler(MouseEvent.MOUSE_CLICKED, doubleClickHandler);
            }
        }
    }

    @Override
    public void startEdit() {
        super.startEdit();
        if (!isEditing()) {
            return;
        }

        EditableGraphic graphic = editableGraphic;
        if (graphic == null) {
            editableGraphic = graphic = initializeEditableGraphic();
        }

        initializeEditing(graphic);

        TextField textField = graphic.textField();
        textField.selectAll();
        textField.requestFocus();
    }

    private EditableGraphic initializeEditableGraphic() {
        record Result(EditableGraphic graphic, Consumer<ActionEvent> onAction) {
        }
        Result result = switch (getItem().tag()) {
            case LinByteTag tag -> {
                EditableGraphic.LongSpinner graphic = new EditableGraphic.LongSpinner(new Spinner<>(
                    new LongSpinnerValueFactory(Byte.MIN_VALUE, Byte.MAX_VALUE, tag.value())
                ));
                yield new Result(
                    graphic,
                    event -> applyUpdateWithEvent(event, LinByteTag.of(graphic.spinner.getValue().byteValue()))
                );
            }
            case LinDoubleTag tag -> {
                EditableGraphic.DoubleSpinner graphic = new EditableGraphic.DoubleSpinner(new Spinner<>(
                    new SpinnerValueFactory.DoubleSpinnerValueFactory(Double.MIN_VALUE, Double.MAX_VALUE, tag.value())
                ));
                yield new Result(
                    graphic,
                    event -> applyUpdateWithEvent(event, LinDoubleTag.of(graphic.spinner.getValue()))
                );
            }
            case LinFloatTag tag -> {
                EditableGraphic.DoubleSpinner graphic = new EditableGraphic.DoubleSpinner(new Spinner<>(
                    new SpinnerValueFactory.DoubleSpinnerValueFactory(Float.MIN_VALUE, Float.MAX_VALUE, tag.value())
                ));
                yield new Result(
                    graphic,
                    event -> applyUpdateWithEvent(event, LinFloatTag.of(graphic.spinner.getValue().floatValue()))
                );
            }
            case LinIntTag tag -> {
                EditableGraphic.LongSpinner graphic = new EditableGraphic.LongSpinner(new Spinner<>(
                    new LongSpinnerValueFactory(Integer.MIN_VALUE, Integer.MAX_VALUE, tag.value())
                ));
                yield new Result(
                    graphic,
                    event -> applyUpdateWithEvent(event, LinIntTag.of(graphic.spinner.getValue().intValue()))
                );
            }
            case LinLongTag tag -> {
                EditableGraphic.LongSpinner graphic = new EditableGraphic.LongSpinner(new Spinner<>(
                    new LongSpinnerValueFactory(Long.MIN_VALUE, Long.MAX_VALUE, tag.value())
                ));
                yield new Result(
                    graphic,
                    event -> applyUpdateWithEvent(event, LinLongTag.of(graphic.spinner.getValue()))
                );
            }
            case LinShortTag tag -> {
                EditableGraphic.LongSpinner graphic = new EditableGraphic.LongSpinner(new Spinner<>(
                    new LongSpinnerValueFactory(Short.MIN_VALUE, Short.MAX_VALUE, tag.value())
                ));
                yield new Result(
                    graphic,
                    event -> applyUpdateWithEvent(event, LinShortTag.of(graphic.spinner.getValue().shortValue()))
                );
            }
            case LinStringTag tag -> {
                EditableGraphic.Text graphic = new EditableGraphic.Text(new TextField(tag.value()));
                yield new Result(
                    graphic,
                    event -> applyUpdateWithEvent(event, LinStringTag.of(graphic.textField().getText()))
                );
            }
            default -> throw new IllegalStateException("Un-editable tag type: " + getItem().tag());
        };
        TextField textField = result.graphic.textField();
        textField.setOnAction(event -> {
            if (result.graphic.node() instanceof Spinner<?> spinner) {
                // Commit the value before calling the action
                try {
                    spinner.commitValue();
                } catch (NumberFormatException e) {
                    ErrorReporter.reportError(ErrorReporter.Level.INFORM, "Invalid number format", e);
                    // Ignore invalid input
                    return;
                }
            }
            result.onAction.accept(event);
        });
        textField.setOnKeyReleased(t -> {
            if (t.getCode() == KeyCode.ESCAPE) {
                cancelEdit();
                t.consume();
            }
        });
        if (result.graphic.node() instanceof Spinner<?> spinner) {
            spinner.setEditable(true);
            spinner.setOnKeyPressed(t -> {
                if (t.getCode() == KeyCode.ENTER) {
                    // This occurs because despite us consuming the textField action event, the spinner still
                    // propagates the key event again. We need to consume it here as well.
                    t.consume();
                }
            });
        }
        return result.graphic;
    }

    private void applyUpdateWithEvent(ActionEvent event, LinTag<?> newTag) {
        applyUpdate(newTag);
        event.consume();
    }

    private void applyUpdate(LinTag<?> newTag) {
        TreeItem<NbtTreeView.TagEntry> item = getTreeTableView().getTreeItem(getIndex());
        item.setValue(new NbtTreeView.TagEntry(item.getValue().name(), newTag));
        commitEdit(item.getValue());
    }

    @Override
    public void cancelEdit() {
        super.cancelEdit();

        initializeNonEditing();
    }

}
