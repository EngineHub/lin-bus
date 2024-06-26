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
 * Represents a double tag.
 */
public final class LinDoubleTag extends LinNumberTag<Double> {

    /**
     * Creates a new double tag.
     *
     * @param value the value
     * @return the tag
     */
    public static LinDoubleTag of(double value) {
        return new LinDoubleTag(value);
    }

    private final double value;

    private LinDoubleTag(double value) {
        this.value = value;
    }

    @Override
    public LinTagType<LinDoubleTag> type() {
        return LinTagType.doubleTag();
    }

    @Override
    public Double value() {
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
    public LinStream linStream() {
        return LinStream.of(new LinToken.Double(value));
    }
}
