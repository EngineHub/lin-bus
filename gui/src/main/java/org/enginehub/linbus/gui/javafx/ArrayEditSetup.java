/*
 * Copyright (c) EngineHub <https://enginehub.org>
 * Copyright (c) contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

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
