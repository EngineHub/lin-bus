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

import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.TableSelectionModel;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableRow;
import javafx.scene.control.TreeTableView;
import org.enginehub.linbus.stream.LinBinaryIO;
import org.enginehub.linbus.tree.LinCompoundTag;
import org.enginehub.linbus.tree.LinListTag;
import org.enginehub.linbus.tree.LinRootEntry;
import org.enginehub.linbus.tree.LinTag;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * The core of this app, this sets up the entire NBT tree view.
 */
public final class NbtTreeView {

    public record TagEntry(String name, LinTag<?> tag) {
    }

    public static TreeTableView<TagEntry> create() {
        TreeTableView<TagEntry> treeView = new TreeTableView<>();

        treeView.setEditable(true);

        TreeTableColumn<TagEntry, TagEntry> typeNameCol = createTypeNameColumn();
        TreeTableColumn<TagEntry, TagEntry> valueCol = createValueColumn();
        treeView.getColumns().add(typeNameCol);
        treeView.getColumns().add(valueCol);
        treeView.setColumnResizePolicy(TreeTableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        treeView.widthProperty().addListener(new ChangeListener<>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                double width = newValue.doubleValue();
                // Type/Name is usually small, so keep it at 25% to start
                typeNameCol.setPrefWidth(width / 4);
                valueCol.setPrefWidth(width * 3 / 4);
                // Unsubscribe from further changes
                treeView.widthProperty().removeListener(this);
            }
        });

        treeView.getStyleClass().add("zebra");
        treeView.setRowFactory(view -> {
            TreeTableRow<TagEntry> row = new TreeTableRow<>();
            // Compensate for the lack of `:not` in JavaFX CSS
            FxSS.addObservableClass(row, row.selectedProperty().map(selected -> selected ? null : "unselected"));
            return row;
        });
        return treeView;
    }

    private static TreeTableColumn<TagEntry, TagEntry> createTypeNameColumn() {
        TreeTableColumn<TagEntry, TagEntry> typeNameCol = new TreeTableColumn<>("Type/Name");
        typeNameCol.setCellValueFactory(cellData -> Bindings.createObjectBinding(cellData.getValue()::getValue));
        typeNameCol.setCellFactory(column -> new TypeNameEditableCell());
        return typeNameCol;
    }

    private static TreeTableColumn<TagEntry, TagEntry> createValueColumn() {
        TreeTableColumn<TagEntry, TagEntry> valueCol = new TreeTableColumn<>("Value");
        valueCol.setCellValueFactory(cellData -> cellData.getValue().valueProperty());
        valueCol.setCellFactory(column -> new ValueEditableCell());
        return valueCol;
    }

    public static TreeItem<TagEntry> loadTreeItem(Path file) throws IOException {
        LinRootEntry root;
        try (var dataInput = new DataInputStream(new GZIPInputStream(Files.newInputStream(file)))) {
            root = LinBinaryIO.readUsing(dataInput, LinRootEntry::readFrom);
        }
        assert root != null;
        return new TagEntryTreeItem(root.name(), root.value());
    }

    public static void saveTreeItem(Path file, TreeItem<TagEntry> root) throws IOException {
        if (root.getValue() == null) {
            throw new IllegalStateException("Nothing to save");
        }
        LinRootEntry rootEntry = new LinRootEntry(root.getValue().name(), (LinCompoundTag) root.getValue().tag());
        try (var dataOutput = new DataOutputStream(new GZIPOutputStream(Files.newOutputStream(file)))) {
            LinBinaryIO.write(dataOutput, rootEntry);
        }
    }

    private record MoveControl(
        BiPredicate<Integer, List<?>> moveFunction,
        int offset
    ) {
        static final MoveControl UP = new MoveControl(
            NbtTreeView::trySwapUp, -1
        );
        static final MoveControl DOWN = new MoveControl(
            NbtTreeView::trySwapDown, 1
        );
    }

    public static void moveEntryUp(TreeTableView<TagEntry> treeView) {
        moveEntry(treeView, MoveControl.UP);
    }

    public static void moveEntryDown(TreeTableView<TagEntry> treeView) {
        moveEntry(treeView, MoveControl.DOWN);
    }

    private static void moveEntry(TreeTableView<TagEntry> treeView, MoveControl moveControl) {
        TreeItem<TagEntry> selectedItem = treeView.getSelectionModel().getSelectedItem();
        if (selectedItem == null || selectedItem.getValue() == null) {
            return;
        }
        TreeItem<TagEntry> parent = selectedItem.getParent();
        if (parent == null || parent.getValue() == null) {
            return;
        }
        TagEntry parentEntry = parent.getValue();
        // We need to save this before modifying the tree, as the selection will change
        int currentSelection = treeView.getSelectionModel().getSelectedIndex();
        if (parentEntry.tag() instanceof LinListTag<?> listTag) {
            @SuppressWarnings("unchecked")
            LinListTag<LinTag<?>> casted = (LinListTag<LinTag<?>>) listTag;
            int index = Integer.parseInt(selectedItem.getValue().name());

            List<LinTag<?>> values = new ArrayList<>(listTag.value());
            if (!moveControl.moveFunction.test(index, values)) {
                return;
            }

            // Adjust the name of the item we're moving
            int newIndex = index + moveControl.offset;
            selectedItem.setValue(new TagEntry(
                String.valueOf(newIndex), selectedItem.getValue().tag()
            ));

            if (!moveControl.moveFunction.test(index, parent.getChildren())) {
                throw new IllegalStateException("Children and keys are out of sync");
            }

            TreeItem<TagEntry> movedIntoSelectedSlot = parent.getChildren().get(index);
            movedIntoSelectedSlot.setValue(new TagEntry(
                String.valueOf(index), movedIntoSelectedSlot.getValue().tag()
            ));

            parent.setValue(new TagEntry(parentEntry.name(), LinListTag.of(casted.elementType(), values)));

            // Also re-select the appropriate item
            treeView.getSelectionModel().clearSelection(currentSelection);
            treeView.getSelectionModel().select(currentSelection + moveControl.offset);
        } else if (parentEntry.tag() instanceof LinCompoundTag compoundTag) {
            // This is a bit more complicated, as we need to actually re-order the whole map.
            // First, swap the relevant keys in the order
            List<String> keys = new ArrayList<>(compoundTag.value().keySet());
            int keyIndex = keys.indexOf(selectedItem.getValue().name());
            if (!moveControl.moveFunction.test(keyIndex, keys)) {
                return;
            }

            // Since that worked, we can do it to the children too
            if (!moveControl.moveFunction.test(keyIndex, parent.getChildren())) {
                throw new IllegalStateException("Children and keys are out of sync");
            }

            // Rebuild the compound tag using the new order
            LinCompoundTag.Builder builder = LinCompoundTag.builder();
            for (String key : keys) {
                builder.put(key, compoundTag.value().get(key));
            }

            parent.setValue(new TagEntry(parentEntry.name(), builder.build()));

            // Also re-select the appropriate item
            treeView.getSelectionModel().clearSelection(currentSelection);
            treeView.getSelectionModel().select(currentSelection + moveControl.offset);
        }
    }

    private static boolean trySwapUp(int index, List<?> values) {
        if (index < 1) {
            // If at the start, nothing to do
            return false;
        }
        Collections.swap(values, index, index - 1);
        return true;
    }

    private static boolean trySwapDown(int index, List<?> values) {
        if (index >= values.size() - 1) {
            // If at the end, nothing to do
            return false;
        }
        Collections.swap(values, index, index + 1);
        return true;
    }

    private NbtTreeView() {
    }

}
