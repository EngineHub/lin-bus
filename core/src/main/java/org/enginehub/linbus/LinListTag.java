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

package org.enginehub.linbus;

import org.checkerframework.checker.nullness.qual.NonNull;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.*;

public final class LinListTag<T extends @NonNull LinTag<?>> extends LinTag<@NonNull List<T>> {
    public static <T extends @NonNull LinTag<?>> LinListTag<T> empty(LinTagType<T> elementType) {
        return new LinListTag<>(elementType, Collections.emptyList(), true);
    }

    public static LinListTag<? extends @NonNull LinTag<?>> readFrom(DataInput input) throws IOException {
        int id = input.readByte();
        @SuppressWarnings("unchecked")
        LinTagType<LinTag<?>> type = (LinTagType<LinTag<?>>) LinTagType.getById(id);
        int size = input.readInt();
        if (size > 0 && LinTagType.endTag().equals(type)) {
            throw new IllegalStateException("Read a non-empty list with an element type of 'end', this is not legal");
        }
        List<LinTag<?>> value = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            value.add(type.readFrom(input));
        }
        return new LinListTag<>(
            type, value, true
        );
    }

    private final LinTagType<T> elementType;
    private final List<T> value;

    public LinListTag(LinTagType<T> elementType, List<T> value) {
        this(elementType, new ArrayList<>(value), true);
    }

    private LinListTag(LinTagType<T> elementType, List<T> value, boolean iSwearToNotModifyValue) {
        if (!iSwearToNotModifyValue) {
            throw new IllegalArgumentException("You think you're clever, huh?");
        }
        Objects.requireNonNull(value, "value is null");
        for (T t : value) {
            if (t.type() != elementType) {
                throw new IllegalArgumentException("Element is not of type " + elementType.name() + " but "
                    + t.type().name());
            }
        }
        if (!value.isEmpty() && elementType == LinTagType.endTag()) {
            throw new IllegalArgumentException("A non-empty list cannot be of type 'end'");
        }
        this.elementType = elementType;
        this.value = Collections.unmodifiableList(value);
    }

    @Override
    public LinTagType<LinListTag<T>> type() {
        return LinTagType.listTag();
    }

    public LinTagType<T> elementType() {
        return elementType;
    }

    @Override
    public @NonNull List<T> value() {
        return value;
    }

    /**
     * Direct shorthand for {@link #value() value()}{@code .}{@link List#get(int) get(index)}.
     */
    public T get(int index) {
        return value.get(index);
    }

    @Override
    public void writeTo(DataOutput output) throws IOException {
        output.write(elementType.id());
        output.writeInt(value.size());
        for (T t : value) {
            t.writeTo(output);
        }
    }
}
