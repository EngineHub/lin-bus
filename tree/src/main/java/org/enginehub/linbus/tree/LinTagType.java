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

import org.enginehub.linbus.common.LinTagId;
import org.jetbrains.annotations.NotNull;

import java.util.Comparator;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * Represents a tag type.
 *
 * <p>
 * This is different from the plain {@link LinTagId}, because it offers generic methods for casting to tag types.
 * </p>
 */
public final class LinTagType<T extends LinTag<?, ?>> {
    private static final LinTagType<LinEndTag> END_TAG = new LinTagType<>(
        LinTagId.END, LinEndTag.class
    );

    /**
     * {@return the {@link LinTagType} for the end tag}
     */
    public static LinTagType<LinEndTag> endTag() {
        return END_TAG;
    }

    private static final LinTagType<LinByteTag> BYTE_TAG = new LinTagType<>(
        LinTagId.BYTE, LinByteTag.class
    );

    /**
     * {@return the {@link LinTagType} for the byte tag}
     */
    public static LinTagType<LinByteTag> byteTag() {
        return BYTE_TAG;
    }

    private static final LinTagType<LinShortTag> SHORT_TAG = new LinTagType<>(
        LinTagId.SHORT, LinShortTag.class
    );

    /**
     * {@return the {@link LinTagType} for the short tag}
     */
    public static LinTagType<LinShortTag> shortTag() {
        return SHORT_TAG;
    }

    private static final LinTagType<LinIntTag> INT_TAG = new LinTagType<>(
        LinTagId.INT, LinIntTag.class
    );

    /**
     * {@return the {@link LinTagType} for the int tag}
     */
    public static LinTagType<LinIntTag> intTag() {
        return INT_TAG;
    }

    private static final LinTagType<LinLongTag> LONG_TAG = new LinTagType<>(
        LinTagId.LONG, LinLongTag.class
    );

    /**
     * {@return the {@link LinTagType} for the long tag}
     */
    public static LinTagType<LinLongTag> longTag() {
        return LONG_TAG;
    }

    private static final LinTagType<LinFloatTag> FLOAT_TAG = new LinTagType<>(
        LinTagId.FLOAT, LinFloatTag.class
    );

    /**
     * {@return the {@link LinTagType} for the float tag}
     */
    public static LinTagType<LinFloatTag> floatTag() {
        return FLOAT_TAG;
    }

    private static final LinTagType<LinDoubleTag> DOUBLE_TAG = new LinTagType<>(
        LinTagId.DOUBLE, LinDoubleTag.class
    );

    /**
     * {@return the {@link LinTagType} for the double tag}
     */
    public static LinTagType<LinDoubleTag> doubleTag() {
        return DOUBLE_TAG;
    }

    private static final LinTagType<LinByteArrayTag> BYTE_ARRAY_TAG = new LinTagType<>(
        LinTagId.BYTE_ARRAY, LinByteArrayTag.class
    );

    /**
     * {@return the {@link LinTagType} for the byte array tag}
     */
    public static LinTagType<LinByteArrayTag> byteArrayTag() {
        return BYTE_ARRAY_TAG;
    }

    private static final LinTagType<LinStringTag> STRING_TAG = new LinTagType<>(
        LinTagId.STRING, LinStringTag.class
    );

    /**
     * {@return the {@link LinTagType} for the string tag}
     */
    public static LinTagType<LinStringTag> stringTag() {
        return STRING_TAG;
    }

    @SuppressWarnings("unchecked")
    private static final LinTagType<LinListTag<? extends @NotNull LinTag<?, ?>>> LIST_TAG = new LinTagType<>(
        LinTagId.LIST, (Class<LinListTag<? extends @NotNull LinTag<?, ?>>>) (Object) LinListTag.class
    );

    /**
     * {@return the {@link LinTagType} for the list tag}
     *
     * @param <T> the type of the list elements
     */
    public static <T extends @NotNull LinTag<?, ?>> LinTagType<LinListTag<T>> listTag() {
        @SuppressWarnings("unchecked")
        LinTagType<LinListTag<T>> cast = (LinTagType<LinListTag<T>>) (Object) LIST_TAG;
        return cast;
    }

    private static final LinTagType<LinCompoundTag> COMPOUND_TAG = new LinTagType<>(
        LinTagId.COMPOUND, LinCompoundTag.class
    );

    /**
     * {@return the {@link LinTagType} for the compound tag}
     */
    public static LinTagType<LinCompoundTag> compoundTag() {
        return COMPOUND_TAG;
    }

    private static final LinTagType<LinIntArrayTag> INT_ARRAY_TAG = new LinTagType<>(
        LinTagId.INT_ARRAY, LinIntArrayTag.class
    );

    /**
     * {@return the {@link LinTagType} for the int array tag}
     */
    public static LinTagType<LinIntArrayTag> intArrayTag() {
        return INT_ARRAY_TAG;
    }

    private static final LinTagType<LinLongArrayTag> LONG_ARRAY_TAG = new LinTagType<>(
        LinTagId.LONG_ARRAY, LinLongArrayTag.class
    );

    /**
     * {@return the {@link LinTagType} for the long array tag}
     */
    public static LinTagType<LinLongArrayTag> longArrayTag() {
        return LONG_ARRAY_TAG;
    }

    private static final @NotNull LinTagType<?>[] LIN_TAG_TYPES =
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

    /**
     * Get the {@link LinTagType} for the given {@link LinTagId}.
     *
     * @param id The {@link LinTagId} to get the {@link LinTagType} for.
     * @return The {@link LinTagType} for the given {@link LinTagId}.
     */
    public static LinTagType<?> fromId(LinTagId id) {
        return LIN_TAG_TYPES[id.id()];
    }

    private final LinTagId id;
    private final Class<T> javaType;

    private LinTagType(LinTagId id, Class<T> javaType) {
        this.id = id;
        this.javaType = javaType;
    }

    /**
     * {@return the name of this tag type}
     */
    public String name() {
        return id.name();
    }

    /**
     * {@return the id of this tag type}
     */
    public LinTagId id() {
        return id;
    }

    /**
     * Cast the tag to the type of this tag type.
     *
     * @param tag the tag to cast
     * @return the tag as {@code T}
     * @throws IllegalArgumentException if the tag is not of type {@code T}
     */
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
        return id == that.id;
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return id.toString();
    }
}
