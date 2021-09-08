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

package org.enginehub.linbus.tree;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.enginehub.linbus.common.LinTagId;

import java.io.DataInput;
import java.io.IOException;
import java.util.Comparator;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * Represents a tag type.
 */
public final class LinTagType<T extends LinTag<?, ?>> {
    private static final LinTagType<LinEndTag> END_TAG = new LinTagType<>(
        LinTagId.END, LinEndTag.class
    );

    public static LinTagType<LinEndTag> endTag() {
        return END_TAG;
    }

    private static final LinTagType<LinByteTag> BYTE_TAG = new LinTagType<>(
        LinTagId.BYTE, LinByteTag.class
    );

    public static LinTagType<LinByteTag> byteTag() {
        return BYTE_TAG;
    }

    private static final LinTagType<LinShortTag> SHORT_TAG = new LinTagType<>(
        LinTagId.SHORT, LinShortTag.class
    );

    public static LinTagType<LinShortTag> shortTag() {
        return SHORT_TAG;
    }

    private static final LinTagType<LinIntTag> INT_TAG = new LinTagType<>(
        LinTagId.INT, LinIntTag.class
    );

    public static LinTagType<LinIntTag> intTag() {
        return INT_TAG;
    }

    private static final LinTagType<LinLongTag> LONG_TAG = new LinTagType<>(
        LinTagId.LONG, LinLongTag.class
    );

    public static LinTagType<LinLongTag> longTag() {
        return LONG_TAG;
    }

    private static final LinTagType<LinFloatTag> FLOAT_TAG = new LinTagType<>(
        LinTagId.FLOAT, LinFloatTag.class
    );

    public static LinTagType<LinFloatTag> floatTag() {
        return FLOAT_TAG;
    }

    private static final LinTagType<LinDoubleTag> DOUBLE_TAG = new LinTagType<>(
        LinTagId.DOUBLE, LinDoubleTag.class
    );

    public static LinTagType<LinDoubleTag> doubleTag() {
        return DOUBLE_TAG;
    }

    private static final LinTagType<LinByteArrayTag> BYTE_ARRAY_TAG = new LinTagType<>(
        LinTagId.BYTE_ARRAY, LinByteArrayTag.class
    );

    public static LinTagType<LinByteArrayTag> byteArrayTag() {
        return BYTE_ARRAY_TAG;
    }

    private static final LinTagType<LinStringTag> STRING_TAG = new LinTagType<>(
        LinTagId.STRING, LinStringTag.class
    );

    public static LinTagType<LinStringTag> stringTag() {
        return STRING_TAG;
    }

    @SuppressWarnings("unchecked")
    private static final LinTagType<LinListTag<? extends @NonNull LinTag<?, ?>>> LIST_TAG = new LinTagType<>(
        LinTagId.LIST, (Class<LinListTag<? extends @NonNull LinTag<?, ?>>>) (Object) LinListTag.class
    );

    public static <T extends @NonNull LinTag<?, ?>> LinTagType<LinListTag<T>> listTag() {
        @SuppressWarnings("unchecked")
        LinTagType<LinListTag<T>> cast = (LinTagType<LinListTag<T>>) (Object) LIST_TAG;
        return cast;
    }

    private static final LinTagType<LinCompoundTag> COMPOUND_TAG = new LinTagType<>(
        LinTagId.COMPOUND, LinCompoundTag.class
    );

    public static LinTagType<LinCompoundTag> compoundTag() {
        return COMPOUND_TAG;
    }

    private static final LinTagType<LinIntArrayTag> INT_ARRAY_TAG = new LinTagType<>(
        LinTagId.INT_ARRAY, LinIntArrayTag.class
    );

    public static LinTagType<LinIntArrayTag> intArrayTag() {
        return INT_ARRAY_TAG;
    }

    private static final LinTagType<LinLongArrayTag> LONG_ARRAY_TAG = new LinTagType<>(
        LinTagId.LONG_ARRAY, LinLongArrayTag.class
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
            .sorted(Comparator.comparing(LinTagType::id))
            .toArray(LinTagType<?>[]::new);

    public static LinTagType<?> fromId(LinTagId id) {
        return LIN_TAG_TYPES[id.id()];
    }

    private final LinTagId id;
    private final Class<T> javaType;

    private LinTagType(LinTagId id, Class<T> javaType) {
        this.id = id;
        this.javaType = javaType;
    }

    public String name() {
        return id.name();
    }

    public LinTagId id() {
        return id;
    }

    public T cast(LinTag<?, ?> tag) {
        if (tag.type() != this) {
            throw new IllegalArgumentException("Tag is a " + tag.type().name() + ", not a " + name());
        }
        return javaType.cast(tag);
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
        return id.toString();
    }
}
