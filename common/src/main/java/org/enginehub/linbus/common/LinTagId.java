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
    END(0),
    /**
     * The {@code BYTE} tag ID.
     */
    BYTE(1),
    /**
     * The {@code SHORT} tag ID.
     */
    SHORT(2),
    /**
     * The {@code INT} tag ID.
     */
    INT(3),
    /**
     * The {@code LONG} tag ID.
     */
    LONG(4),
    /**
     * The {@code FLOAT} tag ID.
     */
    FLOAT(5),
    /**
     * The {@code DOUBLE} tag ID.
     */
    DOUBLE(6),
    /**
     * The {@code BYTE_ARRAY} tag ID.
     */
    BYTE_ARRAY(7),
    /**
     * The {@code STRING} tag ID.
     */
    STRING(8),
    /**
     * The {@code LIST} tag ID.
     */
    LIST(9),
    /**
     * The {@code COMPOUND} tag ID.
     */
    COMPOUND(10),
    /**
     * The {@code INT_ARRAY} tag ID.
     */
    INT_ARRAY(11),
    /**
     * The {@code LONG_ARRAY} tag ID.
     */
    LONG_ARRAY(12),
    ;

    /**
     * Get the {@link LinTagId} for the given int ID.
     *
     * @param id the int ID
     * @return the corresponding {@link LinTagId}
     */
    public static LinTagId fromId(int id) {
        if (id < 0 || id >= LinTagId.values().length) {
            throw new IllegalArgumentException("Invalid NBT ID: " + id);
        }
        return LinTagId.values()[id];
    }

    private final int id;

    LinTagId(int id) {
        this.id = id;
    }

    /**
     * {@return the int ID of this tag}
     */
    public int id() {
        return id;
    }

    @Override
    public String toString() {
        return name() + "[id=" + id + "]";
    }
}
