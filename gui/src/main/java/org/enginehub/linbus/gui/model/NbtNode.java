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

package org.enginehub.linbus.gui.model;

import com.google.common.collect.ImmutableList;
import net.octyl.polymer.annotations.PolymerizeApi;

import java.util.List;
import java.util.stream.Stream;

public record NbtNode<D extends NbtNodeData>(
    D data,
    List<NbtNode<?>> children
) {
    public NbtNode {
        if (!data.allowChildren() && !children.isEmpty()) {
            throw new IllegalStateException("Children are not allowed for data type " + data.getClass().getName());
        }
        children = ImmutableList.copyOf(children);
    }

    public Stream<NbtNode<?>> visitAll() {
        if (children.isEmpty()) {
            return Stream.of(this);
        }
        return Stream.concat(Stream.of(this), children.stream().flatMap(NbtNode::visitAll));
    }
}
