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

package org.enginehub.linbus.stream.visitor;

import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.Comparator;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * Represents a tag type.
 *
 * <p>
 * Tag types are naturally sorted by their {@link #id()}.
 * </p>
 */
public final class LinTagVisitorType<T extends LinTagVisitor> {
    public static final int END_TAG_ID = 0;
    private static final LinTagVisitorType<LinEndTagVisitor> END_TAG = new LinTagVisitorType<>(
        "end", END_TAG_ID, LinEndTagVisitor.class
    );

    public static LinTagVisitorType<LinEndTagVisitor> endTag() {
        return END_TAG;
    }

    public static final int BYTE_TAG_ID = 1;
    private static final LinTagVisitorType<LinByteTagVisitor> BYTE_TAG = new LinTagVisitorType<>(
        "byte", BYTE_TAG_ID, LinByteTagVisitor.class
    );

    public static LinTagVisitorType<LinByteTagVisitor> byteTag() {
        return BYTE_TAG;
    }

    public static final int SHORT_TAG_ID = 2;
    private static final LinTagVisitorType<LinShortTagVisitor> SHORT_TAG = new LinTagVisitorType<>(
        "short", SHORT_TAG_ID, LinShortTagVisitor.class
    );

    public static LinTagVisitorType<LinShortTagVisitor> shortTag() {
        return SHORT_TAG;
    }

    public static final int INT_TAG_ID = 3;
    private static final LinTagVisitorType<LinIntTagVisitor> INT_TAG = new LinTagVisitorType<>(
        "int", INT_TAG_ID, LinIntTagVisitor.class
    );

    public static LinTagVisitorType<LinIntTagVisitor> intTag() {
        return INT_TAG;
    }

    public static final int LONG_TAG_ID = 4;
    private static final LinTagVisitorType<LinLongTagVisitor> LONG_TAG = new LinTagVisitorType<>(
        "long", LONG_TAG_ID, LinLongTagVisitor.class
    );

    public static LinTagVisitorType<LinLongTagVisitor> longTag() {
        return LONG_TAG;
    }

    public static final int FLOAT_TAG_ID = 5;
    private static final LinTagVisitorType<LinFloatTagVisitor> FLOAT_TAG = new LinTagVisitorType<>(
        "float", FLOAT_TAG_ID, LinFloatTagVisitor.class
    );

    public static LinTagVisitorType<LinFloatTagVisitor> floatTag() {
        return FLOAT_TAG;
    }

    public static final int DOUBLE_TAG_ID = 6;
    private static final LinTagVisitorType<LinDoubleTagVisitor> DOUBLE_TAG = new LinTagVisitorType<>(
        "double", DOUBLE_TAG_ID, LinDoubleTagVisitor.class
    );

    public static LinTagVisitorType<LinDoubleTagVisitor> doubleTag() {
        return DOUBLE_TAG;
    }

    public static final int BYTE_ARRAY_TAG_ID = 7;
    private static final LinTagVisitorType<LinByteArrayTagVisitor> BYTE_ARRAY_TAG = new LinTagVisitorType<>(
        "byteArray", BYTE_ARRAY_TAG_ID, LinByteArrayTagVisitor.class
    );

    public static LinTagVisitorType<LinByteArrayTagVisitor> byteArrayTag() {
        return BYTE_ARRAY_TAG;
    }

    public static final int STRING_TAG_ID = 8;
    private static final LinTagVisitorType<LinStringTagVisitor> STRING_TAG = new LinTagVisitorType<>(
        "string", STRING_TAG_ID, LinStringTagVisitor.class
    );

    public static LinTagVisitorType<LinStringTagVisitor> stringTag() {
        return STRING_TAG;
    }

    public static final int LIST_TAG_ID = 9;
    private static final LinTagVisitorType<LinListTagVisitor> LIST_TAG = new LinTagVisitorType<>(
        "list", LIST_TAG_ID, LinListTagVisitor.class
    );

    public static LinTagVisitorType<LinListTagVisitor> listTag() {
        return LIST_TAG;
    }

    public static final int COMPOUND_TAG_ID = 10;
    private static final LinTagVisitorType<LinCompoundTagVisitor> COMPOUND_TAG = new LinTagVisitorType<>(
        "compound", COMPOUND_TAG_ID, LinCompoundTagVisitor.class
    );

    public static LinTagVisitorType<LinCompoundTagVisitor> compoundTag() {
        return COMPOUND_TAG;
    }

    public static final int INT_ARRAY_TAG_ID = 11;
    private static final LinTagVisitorType<LinIntArrayTagVisitor> INT_ARRAY_TAG = new LinTagVisitorType<>(
        "intArray", INT_ARRAY_TAG_ID, LinIntArrayTagVisitor.class
    );

    public static LinTagVisitorType<LinIntArrayTagVisitor> intArrayTag() {
        return INT_ARRAY_TAG;
    }

    public static final int LONG_ARRAY_TAG_ID = 12;
    private static final LinTagVisitorType<LinLongArrayTagVisitor> LONG_ARRAY_TAG = new LinTagVisitorType<>(
        "longArray", LONG_ARRAY_TAG_ID, LinLongArrayTagVisitor.class
    );

    public static LinTagVisitorType<LinLongArrayTagVisitor> longArrayTag() {
        return LONG_ARRAY_TAG;
    }

    private static final @NonNull LinTagVisitorType<?>[] LIN_TAG_TYPES =
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
            .sorted(Comparator.comparing(LinTagVisitorType::id))
            .toArray(LinTagVisitorType<?>[]::new);

    public static @NonNull LinTagVisitorType<?> getById(int id) {
        if (id < 0 || id >= LIN_TAG_TYPES.length) {
            throw new IllegalArgumentException("ID is out of range: " + id);
        }
        return LIN_TAG_TYPES[id];
    }

    private final String name;
    private final int id;
    private final Class<T> javaType;

    private LinTagVisitorType(String name, int id, Class<T> javaType) {
        this.name = name;
        this.id = id;
        this.javaType = javaType;
    }

    public String name() {
        return name;
    }

    public int id() {
        return id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LinTagVisitorType<?> that = (LinTagVisitorType<?>) o;
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
}
