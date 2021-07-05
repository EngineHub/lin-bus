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

import org.enginehub.linbus.core.LinCompoundTag;

import java.io.DataInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.zip.GZIPInputStream;

public record NbtTreeModel(
    NbtNode<?> root
) {
    public static NbtTreeModel loadTreeModel(Path file) throws IOException {
        LinCompoundTag root;
        try (var dataInput = new DataInputStream(new GZIPInputStream(Files.newInputStream(file)))) {
            root = LinCompoundTag.readFrom(dataInput);
        }
        System.err.println(root);
        return new NbtTreeModel(new NbtNode<>(new NbtNodeData.Value<>(root), List.of()));
    }
}
