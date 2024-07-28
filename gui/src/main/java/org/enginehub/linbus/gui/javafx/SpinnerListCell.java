package org.enginehub.linbus.gui.javafx;

import javafx.scene.control.ListCell;
import javafx.scene.control.Spinner;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import org.enginehub.linbus.gui.util.ErrorReporter;
import org.jspecify.annotations.Nullable;

import java.util.Objects;
import java.util.function.Function;

/**
 * Based on {@link javafx.scene.control.cell.TextFieldListCell} but with a spinner for numeric values.
 *
 * <p>
 * {@code T} must be {@code byte}, {@code int}, or {@code long}.
 * </p>
 */
class SpinnerListCell<T extends Number> extends ListCell<T> {
    private @Nullable Spinner<Long> spinner;

    private void initializeNonEditing() {
        T item = getItem();
        if (item == null) {
            throw new IllegalStateException("Item should not be null");
        }
        setText(item.toString());
        setGraphic(null);
    }

    private void initializeEditing(Spinner<Long> nonNullSpinner) {
        T item = getItem();
        if (item == null) {
            throw new IllegalStateException("Item should not be null");
        }
        nonNullSpinner.getValueFactory().setValue(item.longValue());
        setText(null);
        setGraphic(nonNullSpinner);
    }

    @Override
    protected void updateItem(@Nullable T item, boolean empty) {
        if (Objects.equals(item, getItem())) {
            return;
        }

        super.updateItem(item, empty);

        if (item == null) {
            setText(null);
            setGraphic(null);
            spinner = null;
            setEditable(false);
            return;
        }

        if (isEditing()) {
            if (spinner == null) {
                throw new IllegalStateException("Graphic should not be null when editing");
            }

            initializeEditing(spinner);
        } else {
            initializeNonEditing();
        }

        setEditable(true);
    }

    @Override
    public void startEdit() {
        super.startEdit();
        if (!isEditing()) {
            return;
        }

        Spinner<Long> spinnerLocal = spinner;
        if (spinnerLocal == null) {
            spinner = spinnerLocal = initializeEditableGraphic();
        }

        initializeEditing(spinnerLocal);

        TextField textField = spinnerLocal.getEditor();
        textField.selectAll();
        textField.requestFocus();
    }

    private Spinner<Long> initializeEditableGraphic() {
        record Result<T>(Spinner<Long> spinner, Function<Long, T> extractValue) {
        }
        // Safe because we checked it in the switch case labels
        @SuppressWarnings("unchecked")
        Result<T> result = switch (getItem()) {
            case Byte b -> (Result<T>) new Result<>(
                new Spinner<>(new LongSpinnerValueFactory(Byte.MIN_VALUE, Byte.MAX_VALUE, b)),
                Long::byteValue
            );
            case Integer i -> (Result<T>) new Result<>(
                new Spinner<>(new LongSpinnerValueFactory(Integer.MIN_VALUE, Integer.MAX_VALUE, i)),
                Long::intValue
            );
            case Long l -> (Result<T>) new Result<>(
                new Spinner<>(new LongSpinnerValueFactory(Long.MIN_VALUE, Long.MAX_VALUE, l)),
                Long::longValue
            );
            default -> throw new IllegalStateException("Unexpected value type: " + getItem().getClass().getName());
        };
        TextField textField = result.spinner.getEditor();
        textField.setOnAction(event -> {

            // Commit the value before calling the action
            try {
                result.spinner.commitValue();
            } catch (NumberFormatException e) {
                ErrorReporter.reportError(ErrorReporter.Level.INFORM, "Invalid number format", e);
                // Ignore invalid input
                return;
            }
            commitEdit(result.extractValue.apply(result.spinner.getValue()));
            event.consume();
        });
        textField.setOnKeyReleased(t -> {
            if (t.getCode() == KeyCode.ESCAPE) {
                cancelEdit();
                t.consume();
            }
        });
        result.spinner.setEditable(true);
        result.spinner.setOnKeyPressed(t -> {
            if (t.getCode() == KeyCode.ENTER) {
                // This occurs because despite us consuming the textField action event, the spinner still
                // propagates the key event again. We need to consume it here as well.
                t.consume();
            }
        });
        return result.spinner;
    }

    @Override
    public void cancelEdit() {
        super.cancelEdit();

        initializeNonEditing();
    }

}
