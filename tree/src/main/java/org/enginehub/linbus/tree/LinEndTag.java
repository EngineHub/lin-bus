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

import org.enginehub.linbus.stream.LinStream;
import org.jetbrains.annotations.NotNull;

/**
 * Represents an end tag.
 */
public final class LinEndTag extends LinTag<Void> {
    private static final LinEndTag INSTANCE = new LinEndTag();

    /**
     * The sole instance of the end tag.
     *
     * @return the end tag
     */
    public static LinEndTag instance() {
        return INSTANCE;
    }

    private LinEndTag() {
    }

    @Override
    public @NotNull LinTagType<@NotNull LinEndTag> type() {
        return LinTagType.endTag();
    }

    @Override
    public Void value() {
        return null;
    }

    @Override
    public @NotNull LinStream linStream() {
        return LinStream.of();
    }

    @Override
    public int hashCode() {
        return System.identityHashCode(this);
    }

    @Override
    public @NotNull String toString() {
        return getClass().getSimpleName();
    }
}
