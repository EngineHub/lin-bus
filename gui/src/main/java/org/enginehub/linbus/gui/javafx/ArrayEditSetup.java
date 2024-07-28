package org.enginehub.linbus.gui.javafx;

import javafx.scene.Parent;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.control.ListView;
import javafx.scene.layout.VBox;
import org.enginehub.linbus.gui.util.MoreFutures;

import java.util.concurrent.CompletableFuture;

public class ArrayEditSetup<T extends Number> {

    private static <T extends Number> ListView<T> createListView(BufferObservableList<T> backingList) {
        ListView<T> listView = new ListView<>(backingList);
        listView.setCellFactory(lv -> new SpinnerListCell<>());
        listView.setEditable(true);
        return listView;
    }

    public final Parent root;

    public ArrayEditSetup(BufferObservableList<T> backingList) {
        this.root = new VBox(
            createListView(backingList)
        );
    }

    public CompletableFuture<Boolean> showForUpdate() {
        var dialog = new Dialog<ButtonType>();
        dialog.setTitle("Edit Array");
        DialogPane dialogPane = dialog.getDialogPane();
        dialogPane.setContent(root);
        // No "Cancel" button since it's confusing if you just intended to view the array
        // Cancel can be done by closing the dialog.
        dialogPane.getButtonTypes().addAll(ButtonType.APPLY);

        dialog.show();

        return MoreFutures.create((resolve, reject) -> dialog.setOnHidden(event ->
            resolve.accept(dialog.getResult() == ButtonType.APPLY)
        ));
    }
}
