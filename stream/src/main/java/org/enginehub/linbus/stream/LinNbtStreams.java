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

package org.enginehub.linbus.stream;

import org.enginehub.linbus.stream.impl.OptionalInfoCalculator;
import org.enginehub.linbus.stream.token.LinToken;
import org.jetbrains.annotations.NotNull;

import java.util.Iterator;

/**
 * Operations on the core "stream" type, {@code Iterator<? extends @NotNull LinToken>}.
 */
public class LinNbtStreams {
    /**
     * Fill in optional information, such as list sizes and types, from the input stream.
     *
     * <p>
     * Due to the nature of the information, this may cache large amounts of data in memory. For example, all elements
     * of a list will be held until the end of said list is reached.
     * </p>
     *
     * <p>
     * If the optional information is already present, this method does essentially nothing, and costs almost no
     * memory.
     * </p>
     *
     * @param tokens the original token stream
     * @return the same tokens, but with optional information filled in
     */
    public static Iterator<? extends @NotNull LinToken> calculateOptionalInfo(Iterator<? extends @NotNull LinToken> tokens) {
        return tokens instanceof OptionalInfoCalculator ? tokens : new OptionalInfoCalculator(tokens);
    }
    private LinNbtStreams() {
    }
}
