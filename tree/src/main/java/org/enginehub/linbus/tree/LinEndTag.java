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

import org.enginehub.linbus.stream.token.LinToken;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.Iterator;
import java.util.Spliterator;
import java.util.Spliterators;

/**
 * Represents an end tag.
 */
public final class LinEndTag extends LinTag<Void, LinEndTag> {
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
    public LinTagType<LinEndTag> type() {
        return LinTagType.endTag();
    }

    @Override
    public Void value() {
        return null;
    }

    @Override
    public @NotNull Iterator<LinToken> iterator() {
        return Collections.emptyIterator();
    }

    @Override
    public Spliterator<LinToken> spliterator() {
        return Spliterators.emptySpliterator();
    }

    @Override
    public int hashCode() {
        return System.identityHashCode(this);
    }

    @Override
    public String toString() {
        return type().name();
    }
}
