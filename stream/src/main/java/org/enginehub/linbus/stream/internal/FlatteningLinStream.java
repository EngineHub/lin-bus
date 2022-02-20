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

package org.enginehub.linbus.stream.internal;

import org.enginehub.linbus.stream.LinStream;
import org.enginehub.linbus.stream.LinStreamable;
import org.enginehub.linbus.stream.token.LinToken;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.Iterator;

/**
 * Flattening stream.
 */
public class FlatteningLinStream implements LinStream {
    private final Iterator<? extends LinStreamable> streamables;
    private LinStream current;

    /**
     * Create a new flattening stream.
     *
     * @param streamables the streamables to flatten
     */
    public FlatteningLinStream(Iterator<? extends LinStreamable> streamables) {
        this.streamables = streamables;
    }

    @Override
    public @Nullable LinToken nextOrNull() throws IOException {
        while (true) {
            if (current == null) {
                if (!streamables.hasNext()) {
                    return null;
                }
                current = streamables.next().linStream();
            }
            LinToken token = current.nextOrNull();
            if (token == null) {
                current = null;
                continue;
            }
            return token;
        }
    }
}
