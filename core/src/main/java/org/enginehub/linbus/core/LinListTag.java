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

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.*;

public final class LinListTag<T extends @NonNull LinTag<?, ?>> extends LinTag<@NonNull List<T>, LinListTag<T>> {
    public static <T extends @NonNull LinTag<?, ?>> LinListTag<T> empty(LinTagType<T> elementType) {
        return builder(elementType).build();
    }

    public static LinListTag<? extends @NonNull LinTag<?, ?>> readFrom(DataInput input) throws IOException {
        int id = input.readByte();
        @SuppressWarnings("unchecked")
        LinTagType<LinTag<?, ?>> type = (LinTagType<LinTag<?, ?>>) LinTagType.getById(id);
        int size = input.readInt();
        if (size > 0 && LinTagType.endTag().equals(type)) {
            throw new IllegalStateException("Read a non-empty list with an element type of 'end', this is not legal");
        }
        var builder = builder(type);
        for (int i = 0; i < size; i++) {
            builder.add(type.readFrom(input));
        }
        return builder.build();
    }

    public static <T extends @NonNull LinTag<?, ?>> Builder<T> builder(LinTagType<T> elementType) {
        return new Builder<>(elementType);
    }

    public static final class Builder<T extends @NonNull LinTag<?, ?>> {
        private final LinTagType<T> elementType;
        private final List<T> collector;

        private Builder(LinTagType<T> elementType) {
            this.elementType = elementType;
            this.collector = new ArrayList<>();
        }

        private Builder(LinListTag<T> base) {
            this.elementType = base.elementType;
            this.collector = new ArrayList<>(base.value);
        }

        public Builder<T> add(T tag) {
            if (tag.type() != elementType) {
                throw new IllegalArgumentException("Element is not of type " + elementType.name() + " but "
                    + tag.type().name());
            }
            this.collector.add(tag);
            return this;
        }

        public LinListTag<T> build() {
            return new LinListTag<>(this.elementType, List.copyOf(this.collector), false);
        }
    }

    private final LinTagType<T> elementType;
    private final List<T> value;

    public LinListTag(LinTagType<T> elementType, List<T> value) {
        this(elementType, List.copyOf(value), true);
    }

    private LinListTag(LinTagType<T> elementType, List<T> value, boolean check) {
        Objects.requireNonNull(value, "value is null");
        if (check) {
            for (T t : value) {
                if (t.type() != elementType) {
                    throw new IllegalArgumentException("Element is not of type " + elementType.name() + " but "
                        + t.type().name());
                }
            }
        }
        if (!value.isEmpty() && elementType == LinTagType.endTag()) {
            throw new IllegalArgumentException("A non-empty list cannot be of type 'end'");
        }
        this.elementType = elementType;
        this.value = value;
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
