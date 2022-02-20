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

/**
 * A tag that represents a number. This allows for generic handling of tags that provide {@link Number} subtypes.
 *
 * @param <T> the specific type of number this tag represents
 * @param <SELF> the type of this tag
 */
public sealed abstract class LinNumberTag<T extends Number, SELF extends LinNumberTag<T, SELF>> extends LinTag<T, SELF>
    permits LinByteTag, LinDoubleTag, LinFloatTag, LinIntTag, LinLongTag, LinShortTag {
    /**
     * Constructor for subclasses.
     */
    protected LinNumberTag() {
    }
}
