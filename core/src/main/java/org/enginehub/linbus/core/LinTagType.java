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
import org.jetbrains.annotations.NotNull;

import java.io.DataInput;
import java.io.IOException;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * Represents a tag type.
 *
 * <p>
 * Tag types are naturally sorted by their {@link #id()}.
 * </p>
 */
public final class LinTagType<T extends LinTag<?>> implements Comparable<LinTagType<?>> {
    private static final LinTagType<LinEndTag> END_TAG = new LinTagType<>(
        "end", 0, LinEndTag.class, LinEndTag::readFrom
    );

    public static LinTagType<LinEndTag> endTag() {
        return END_TAG;
    }

    private static final LinTagType<LinByteTag> BYTE_TAG = new LinTagType<>(
        "byte", 1, LinByteTag.class, LinByteTag::readFrom
    );

    public static LinTagType<LinByteTag> byteTag() {
        return BYTE_TAG;
    }

    private static final LinTagType<LinShortTag> SHORT_TAG = new LinTagType<>(
        "short", 2, LinShortTag.class, LinShortTag::readFrom
    );

    public static LinTagType<LinShortTag> shortTag() {
        return SHORT_TAG;
    }

    private static final LinTagType<LinIntTag> INT_TAG = new LinTagType<>(
        "int", 3, LinIntTag.class, LinIntTag::readFrom
    );

    public static LinTagType<LinIntTag> intTag() {
        return INT_TAG;
    }

    private static final LinTagType<LinLongTag> LONG_TAG = new LinTagType<>(
        "long", 4, LinLongTag.class, LinLongTag::readFrom
    );

    public static LinTagType<LinLongTag> longTag() {
        return LONG_TAG;
    }

    private static final LinTagType<LinFloatTag> FLOAT_TAG = new LinTagType<>(
        "float", 5, LinFloatTag.class, LinFloatTag::readFrom
    );

    public static LinTagType<LinFloatTag> floatTag() {
        return FLOAT_TAG;
    }

    private static final LinTagType<LinDoubleTag> DOUBLE_TAG = new LinTagType<>(
        "double", 6, LinDoubleTag.class, LinDoubleTag::readFrom
    );

    public static LinTagType<LinDoubleTag> doubleTag() {
        return DOUBLE_TAG;
    }

    private static final LinTagType<LinByteArrayTag> BYTE_ARRAY_TAG = new LinTagType<>(
        "byteArray", 7, LinByteArrayTag.class, LinByteArrayTag::readFrom
    );

    public static LinTagType<LinByteArrayTag> byteArrayTag() {
        return BYTE_ARRAY_TAG;
    }

    private static final LinTagType<LinStringTag> STRING_TAG = new LinTagType<>(
        "string", 8, LinStringTag.class, LinStringTag::readFrom
    );

    public static LinTagType<LinStringTag> stringTag() {
        return STRING_TAG;
    }

    @SuppressWarnings("unchecked")
    private static final LinTagType<LinListTag<? extends @NonNull LinTag<?>>> LIST_TAG = new LinTagType<>(
        "list", 9, (Class<LinListTag<? extends @NonNull LinTag<?>>>) (Object) LinListTag.class,
        LinListTag::readFrom
    );

    public static <T extends @NonNull LinTag<?>> LinTagType<LinListTag<T>> listTag() {
        @SuppressWarnings("unchecked")
        LinTagType<LinListTag<T>> cast = (LinTagType<LinListTag<T>>) (Object) LIST_TAG;
        return cast;
    }

    private static final LinTagType<LinCompoundTag> COMPOUND_TAG = new LinTagType<>(
        "compound", 10, LinCompoundTag.class, LinCompoundTag::readFrom
    );

    public static LinTagType<LinCompoundTag> compoundTag() {
        return COMPOUND_TAG;
    }

    private static final LinTagType<LinIntArrayTag> INT_ARRAY_TAG = new LinTagType<>(
        "intArray", 11, LinIntArrayTag.class, LinIntArrayTag::readFrom
    );

    public static LinTagType<LinIntArrayTag> intArrayTag() {
        return INT_ARRAY_TAG;
    }

    private static final LinTagType<LinLongArrayTag> LONG_ARRAY_TAG = new LinTagType<>(
        "longArray", 12, LinLongArrayTag.class, LinLongArrayTag::readFrom
    );

    public static LinTagType<LinLongArrayTag> longArrayTag() {
        return LONG_ARRAY_TAG;
    }

    private static final @NonNull LinTagType<?>[] LIN_TAG_TYPES =
        Stream.of(
            END_TAG,
            BYTE_TAG,
            SHORT_TAG,
            INT_TAG,
            LONG_TAG,
            FLOAT_TAG,
            DOUBLE_TAG,
            BYTE_ARRAY_TAG,
            STRING_TAG,
            LIST_TAG,
            COMPOUND_TAG,
            INT_ARRAY_TAG,
            LONG_ARRAY_TAG
        )
            .sorted()
            .toArray(LinTagType<?>[]::new);

    public static @NonNull LinTagType<?> getById(int id) {
        if (id < 0 || id >= LIN_TAG_TYPES.length) {
            throw new IllegalArgumentException("ID is out of range: " + id);
        }
        return LIN_TAG_TYPES[id];
    }

    private interface LinTagReader<T> {
        T readFrom(DataInput input) throws IOException;
    }

    private final String name;
    private final int id;
    private final Class<T> javaType;
    private final LinTagReader<T> reader;

    private LinTagType(String name, int id, Class<T> javaType, LinTagReader<T> reader) {
        this.name = name;
        this.id = id;
        this.javaType = javaType;
        this.reader = reader;
    }

    public final String name() {
        return name;
    }

    public final int id() {
        return id;
    }

    public final T cast(LinTag<?> tag) {
        if (tag.type() != this) {
            throw new IllegalArgumentException("Tag is a " + tag.type().name() + ", not a " + name());
        }
        return javaType.cast(tag);
    }

    public final T readFrom(DataInput input) throws IOException {
        return reader.readFrom(input);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LinTagType<?> that = (LinTagType<?>) o;
        return id == that.id && javaType.equals(that.javaType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, javaType);
    }

    @Override
    public String toString() {
        return name + "[" + id + "]";
    }

    @Override
    public int compareTo(@NotNull LinTagType<?> o) {
        return Integer.compare(id, o.id);
    }
}
