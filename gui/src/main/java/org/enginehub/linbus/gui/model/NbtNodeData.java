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

import org.enginehub.linbus.tree.LinCompoundTag;
import org.enginehub.linbus.tree.LinListTag;
import org.enginehub.linbus.tree.LinTag;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;

public sealed interface NbtNodeData {

    boolean allowChildren();

    record File(FileType fileType, Path location) implements NbtNodeData {
        public static File from(Path location) throws IOException {
            var attrs = Files.readAttributes(location, BasicFileAttributes.class);
            FileType fileType;
            if (attrs.isRegularFile()) {
                fileType = FileType.NORMAL;
            } else if (attrs.isDirectory()) {
                fileType = FileType.DIRECTORY;
            } else {
                throw new IllegalArgumentException(location + " is not a normal file or directory");
            }
            return new File(fileType, location);
        }

        @Override
        public boolean allowChildren() {
            return true;
        }
    }

    record Value<T extends LinTag<?, ?>>(T tag) implements NbtNodeData {
        @Override
        public boolean allowChildren() {
            return tag instanceof LinCompoundTag || tag instanceof LinListTag;
        }
    }
}
