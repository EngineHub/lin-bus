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
import org.enginehub.linbus.tree.impl.LinTagReader;
import org.jetbrains.annotations.NotNull;

import java.util.Iterator;

/**
 * Utility methods for {@link LinTag}.
 */
public class LinTags {
    /**
     * Read a tag from the given stream.
     *
     * @param tokens the stream to read from
     * @param type the type of tag to read
     * @return the tag
     */
    public static <T extends LinTag<?, T>> T readFrom(@NotNull Iterator<? extends @NotNull LinToken> tokens, LinTagType<T> type) {
        return LinTagReader.readValue(tokens, type);
    }

    private LinTags() {
    }
}
