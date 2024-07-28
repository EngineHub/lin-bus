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

import javafx.collections.ObservableList;
import javafx.scene.control.TreeItem;
import org.enginehub.linbus.tree.LinCompoundTag;
import org.enginehub.linbus.tree.LinListTag;
import org.enginehub.linbus.tree.LinTag;

import java.util.Collection;
import java.util.List;
import java.util.stream.IntStream;

final class TagEntryTreeItem extends TreeItem<NbtTreeView.TagEntry> {
    private boolean builtChildren;

    TagEntryTreeItem(String name, LinTag<?> tag) {
        super(new NbtTreeView.TagEntry(name, tag));
        builtChildren = false;
    }

    @Override
    public ObservableList<TreeItem<NbtTreeView.TagEntry>> getChildren() {
        if (!builtChildren) {
            // Shouldn't this go _after_? Well, actually, no. Updating this list can re-trigger a call to this
            // method, and we only want to build the children once.
            builtChildren = true;
            super.getChildren().setAll(buildChildren());
        }
        return super.getChildren();
    }

    private Collection<? extends TreeItem<NbtTreeView.TagEntry>> buildChildren() {
        return switch (getValue().tag()) {
            case LinCompoundTag compoundTag -> compoundTag.value().entrySet().stream()
                .map(entry -> {
                    TreeItem<NbtTreeView.TagEntry> child = new TagEntryTreeItem(entry.getKey(), entry.getValue());
                    child.addEventHandler(TreeItem.valueChangedEvent(), this::updateCompoundTag);
                    return child;
                })
                .toList();
            case LinListTag<?> listTag -> IntStream.range(0, listTag.value().size())
                .mapToObj(i -> {
                    TreeItem<NbtTreeView.TagEntry> child = new TagEntryTreeItem(String.valueOf(i), listTag.value().get(i));
                    child.addEventHandler(TreeItem.valueChangedEvent(), this::updateListTag);
                    return child;
                })
                .toList();
            default -> List.of();
        };
    }

    private void updateCompoundTag(TreeModificationEvent<NbtTreeView.TagEntry> event) {
        NbtTreeView.TagEntry currentValue = getValue();
        LinCompoundTag newTag = ((LinCompoundTag) currentValue.tag()).toBuilder()
            .put(event.getTreeItem().getValue().name(), event.getTreeItem().getValue().tag())
            .build();
        setValue(new NbtTreeView.TagEntry(currentValue.name(), newTag));
        event.consume();
    }

    private void updateListTag(TreeModificationEvent<NbtTreeView.TagEntry> event) {
        // Force generic list type, so we can set
        NbtTreeView.TagEntry currentValue = getValue();
        @SuppressWarnings("unchecked")
        LinListTag<LinTag<?>> cast = (LinListTag<LinTag<?>>) currentValue.tag();
        LinListTag<?> newTag = cast.toBuilder()
            .set(Integer.parseInt(event.getTreeItem().getValue().name()), event.getTreeItem().getValue().tag())
            .build();
        setValue(new NbtTreeView.TagEntry(currentValue.name(), newTag));
        event.consume();
    }

    @Override
    public boolean isLeaf() {
        return switch (getValue().tag()) {
            case LinCompoundTag compoundTag -> compoundTag.value().isEmpty();
            case LinListTag<?> listTag -> listTag.value().isEmpty();
            // All other tags, including array tags, are leaves
            // We represent arrays with their own viewer since they usually have a lot of elements
            default -> true;
        };
    }
}
