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

package org.enginehub.linbus.core;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Map;

/**
 * Common code between {@link LinCompoundTag} and {@link LinRootEntry}.
 */
class LinCompoundEntry {
    static Map. @Nullable Entry<String, @NonNull LinTag<?, ?>> readFrom(DataInput input) throws IOException {
        var type = LinTagType.getById(
            input.readUnsignedByte()
        );
        if (type == LinTagType.endTag()) {
            return null;
        }
        var name = input.readUTF();
        return Map.entry(name, type.readFrom(input));
    }

    static void writeTo(DataOutput output, String name, LinTag<?, ?> tag) throws IOException {
        // id
        output.writeByte(tag.type().id());
        // name
        output.writeUTF(name);
        // payload
        tag.writeTo(output);
    }
}
