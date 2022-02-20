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
import org.jetbrains.annotations.NotNull;

/**
 * Represents a double tag.
 */
public final class LinDoubleTag extends LinNumberTag<@NotNull Double, LinDoubleTag> {
    private final double value;

    /**
     * Creates a new double tag.
     *
     * @param value the value
     */
    public LinDoubleTag(double value) {
        this.value = value;
    }

    @Override
    public @NotNull LinTagType<LinDoubleTag> type() {
        return LinTagType.doubleTag();
    }

    @Override
    public @NotNull Double value() {
        return value;
    }

    /**
     * Get the value as a primitive double, to avoid boxing.
     *
     * @return the value
     */
    public double valueAsDouble() {
        return value;
    }

    @Override
    public @NotNull LinStream linStream() {
        return LinStream.of(new LinToken.Double(value));
    }
}
