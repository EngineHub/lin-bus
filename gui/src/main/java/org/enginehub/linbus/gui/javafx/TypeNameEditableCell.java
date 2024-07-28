package org.enginehub.linbus.gui.javafx;

import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableCell;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import org.enginehub.linbus.tree.LinCompoundTag;
import org.jspecify.annotations.Nullable;

import java.util.List;

/**
 * Based on {@link javafx.scene.control.cell.TextFieldTreeTableCell} but adapted to allow a graphic.
 */
class TypeNameEditableCell extends TreeTableCell<NbtTreeView.TagEntry, NbtTreeView.TagEntry> {
    private @Nullable TextField textField;

    private void initializeNonEditing() {
        NbtTreeView.TagEntry item = getItem();
        FxFonts.AndString toSet;
        if (item.name().isEmpty()) {
            toSet = new FxFonts.AndString(FxFonts.ITALIC, "<empty name>");
        } else if (item.name().isBlank()) {
            toSet = new FxFonts.AndString(FxFonts.ITALIC, "<blank name>");
        } else {
            toSet = new FxFonts.AndString(FxFonts.DEFAULT, item.name());
        }
        setText(toSet.string());
        setFont(toSet.font());
        setGraphic(Icons.iconForTag(item.tag()));
    }

    private void initializeEditing(TextField nonNullTextField) {
        nonNullTextField.setText(getItem().name());
        setText(null);
        HBox container = new HBox(4);
        container.getChildren().setAll(List.of(Icons.iconForTag(getItem().tag()), nonNullTextField));
        setGraphic(container);
    }

    @Override
    protected void updateItem(NbtTreeView.@Nullable TagEntry item, boolean empty) {
        if (item == getItem()) {
            return;
        }

        super.updateItem(item, empty);

        if (item == null) {
            setText(null);
            setGraphic(null);
            textField = null;
            setEditable(false);
            // Don't show a tooltip if there's no item
            Tooltip tooltip = getTooltip();
            if (tooltip != null) {
                tooltip.hide();
            }
            this.setTooltip(null);
            return;
        }

        if (isEditing()) {
            if (textField == null) {
                throw new IllegalStateException("TextField should not be null when editing");
            }

            initializeEditing(textField);
        } else {
            initializeNonEditing();
        }

        if (getTooltip() == null) {
            setTooltip(MinorFixes.fixTooltip(new Tooltip()));
        }
        getTooltip().setText(item.tag().type().id().name());
        getTooltip().setGraphic(Icons.iconForTag(item.tag()));

        // We can be edited if we're in a LinCompoundTag or we're the root of the tree
        TreeItem<NbtTreeView.TagEntry> parent = getTreeTableView().getTreeItem(getIndex()).getParent();
        setEditable(parent == null || parent.getValue().tag() instanceof LinCompoundTag);
    }

    @Override
    public void startEdit() {
        super.startEdit();
        if (!isEditing()) {
            return;
        }

        if (textField == null) {
            textField = new TextField();
            textField.setOnAction(event -> {
                NbtTreeView.TagEntry oldItem = getItem();
                String newName = textField.getText();
                TreeItem<NbtTreeView.TagEntry> item = getTreeTableView().getTreeItem(getIndex());
                // If we need to, update the parent tag. If this is root then we don't need to do that.
                TreeItem<NbtTreeView.TagEntry> parentTree = item.getParent();
                if (parentTree != null) {
                    LinCompoundTag newParent = ((LinCompoundTag) parentTree.getValue().tag()).toBuilder()
                        .remove(oldItem.name())
                        .put(newName, oldItem.tag())
                        .build();
                    parentTree.setValue(new NbtTreeView.TagEntry(parentTree.getValue().name(), newParent));
                }
                item.setValue(new NbtTreeView.TagEntry(newName, oldItem.tag()));
                commitEdit(item.getValue());
                event.consume();
            });
            textField.setOnKeyReleased(t -> {
                if (t.getCode() == KeyCode.ESCAPE) {
                    cancelEdit();
                    t.consume();
                }
            });
        }

        initializeEditing(textField);

        textField.selectAll();
        textField.requestFocus();
    }

    @Override
    public void cancelEdit() {
        super.cancelEdit();

        initializeNonEditing();
    }

}
