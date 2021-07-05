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
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;

public final class LinCompoundTag extends LinTag<@NonNull Map<String, @NonNull LinTag<?>>> {
    /**
     * Read the implicit root tag from the given input. The root is either an empty compound tag,
     * or a compound tag with a single entry, usually pointing to another compound tag.
     *
     * @param input the input
     * @return the root tag
     * @throws IOException if there is an I/O error
     */
    public static LinCompoundTag readRootFrom(DataInput input) throws IOException {
        Map<String, @NonNull LinTag<?>> value = new LinkedHashMap<>(1);
        readOnce(input, value);
        return new LinCompoundTag(value, true);
    }

    public static LinCompoundTag readFrom(DataInput input) throws IOException {
        Map<String, @NonNull LinTag<?>> value = new LinkedHashMap<>();
        while (readOnce(input, value)) {
            // continue reading
        }
        return new LinCompoundTag(value, true);
    }

    private static boolean readOnce(DataInput input, Map<String, @NonNull LinTag<?>> value) throws IOException {
        int id = input.readByte();
        LinTagType<?> type = LinTagType.getById(id);
        if (type == LinTagType.endTag()) {
            return false;
        }
        String name = input.readUTF();
        LinTag<?> tag = type.readFrom(input);
        value.put(name, tag);
        return true;
    }

    private final Map<String, @NonNull LinTag<?>> value;

    public LinCompoundTag(@NonNull Map<String, @NonNull LinTag<?>> value) {
        this(Map.copyOf(value), true);
    }

    private LinCompoundTag(@NonNull Map<String, @NonNull LinTag<?>> value, boolean iSwearToNotModifyValue) {
        if (!iSwearToNotModifyValue) {
            throw new IllegalArgumentException("You think you're clever, huh?");
        }
        this.value = Collections.unmodifiableMap(Objects.requireNonNull(value, "value is null"));
    }

    @Override
    public LinTagType<LinCompoundTag> type() {
        return LinTagType.compoundTag();
    }

    @Override
    public @NonNull Map<String, @NonNull LinTag<?>> value() {
        return value;
    }

    public <T extends LinTag<?>> @Nullable T findTag(@NonNull String key, @NonNull LinTagType<T> type) {
        LinTag<?> tag = value.get(key);
        return type == tag.type() ? type.cast(tag) : null;
    }

    public <T extends LinTag<?>> @NonNull T getTag(@NonNull String key, @NonNull LinTagType<T> type) {
        LinTag<?> tag = value.get(key);

        if (tag == null) {
            throw new NoSuchElementException("No tag under the key '" + key + "' exists");
        }

        if (type != tag.type()) {
            throw new IllegalStateException("Tag under '" + key + "' exists, but is a " + tag.type().name()
                + " instead of " + type.name());
        }

        return type.cast(tag);
    }

    @Override
    public void writeTo(DataOutput output) throws IOException {
        for (Map.Entry<String, LinTag<?>> entry : value.entrySet()) {
            // id
            output.writeByte(entry.getValue().type().id());
            // name
            output.writeUTF(entry.getKey());
            // payload
            entry.getValue().writeTo(output);
        }
        // finish with the end tag
        output.write(LinTagType.endTag().id());
    }

    @Override
    public String toString() {
        return "compound" + value;
    }
}
