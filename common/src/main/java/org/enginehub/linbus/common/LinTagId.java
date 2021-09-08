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

package org.enginehub.linbus.common;

/**
 * Constants for a tag ID.
 */
public enum LinTagId {
    /**
     * The {@code END} tag ID.
     */
    END,
    /**
     * The {@code BYTE} tag ID.
     */
    BYTE,
    /**
     * The {@code SHORT} tag ID.
     */
    SHORT,
    /**
     * The {@code INT} tag ID.
     */
    INT,
    /**
     * The {@code LONG} tag ID.
     */
    LONG,
    /**
     * The {@code FLOAT} tag ID.
     */
    FLOAT,
    /**
     * The {@code DOUBLE} tag ID.
     */
    DOUBLE,
    /**
     * The {@code BYTE_ARRAY} tag ID.
     */
    BYTE_ARRAY,
    /**
     * The {@code STRING} tag ID.
     */
    STRING,
    /**
     * The {@code LIST} tag ID.
     */
    LIST,
    /**
     * The {@code COMPOUND} tag ID.
     */
    COMPOUND,
    /**
     * The {@code INT_ARRAY} tag ID.
     */
    INT_ARRAY,
    /**
     * The {@code LONG_ARRAY} tag ID.
     */
    LONG_ARRAY,
    ;

    public static LinTagId fromId(int id) {
        if (id < 0 || id >= LinTagId.values().length) {
            throw new IllegalArgumentException("Invalid NBT ID: " + id);
        }
        return LinTagId.values()[id];
    }

    private final int id = ordinal();

    LinTagId() {
    }

    public int id() {
        return id;
    }

    @Override
    public String toString() {
        return name() + "[" + id + "]";
    }
}
