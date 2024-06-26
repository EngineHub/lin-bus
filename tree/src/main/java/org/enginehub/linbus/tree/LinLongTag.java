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
import org.enginehub.linbus.stream.token.LinToken;

/**
 * Represents a long tag.
 */
public final class LinLongTag extends LinNumberTag<Long> {

    /**
     * Create a new long tag.
     *
     * @param value the value
     * @return the tag
     */
    public static LinLongTag of(long value) {
        return new LinLongTag(value);
    }

    private final long value;

    private LinLongTag(long value) {
        this.value = value;
    }

    @Override
    public LinTagType<LinLongTag> type() {
        return LinTagType.longTag();
    }

    @Override
    public Long value() {
        return value;
    }

    /**
     * Get the value as a primitive long, to avoid boxing.
     *
     * @return the value
     */
    public long valueAsLong() {
        return value;
    }

    @Override
    public LinStream linStream() {
        return LinStream.of(new LinToken.Long(value));
    }
}
