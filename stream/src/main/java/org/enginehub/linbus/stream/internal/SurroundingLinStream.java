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
import org.enginehub.linbus.stream.token.LinToken;
import org.jspecify.annotations.Nullable;

import java.io.IOException;

/**
 * A {@link LinStream} that wraps another {@link LinStream} and adds a token to the beginning and end of the stream.
 */
public class SurroundingLinStream implements LinStream {
    private @Nullable LinToken prefix;
    private @Nullable LinStream stream;
    private @Nullable LinToken suffix;

    /**
     * Creates a new {@link SurroundingLinStream} with the given prefix and suffix.
     *
     * @param prefix the prefix token, may be null
     * @param stream the stream to wrap
     * @param suffix the suffix token, may be null
     */
    public SurroundingLinStream(@Nullable LinToken prefix, LinStream stream, @Nullable LinToken suffix) {
        this.prefix = prefix;
        this.stream = stream;
        this.suffix = suffix;
    }

    @Override
    public @Nullable LinToken nextOrNull() throws IOException {
        if (prefix != null) {
            LinToken token = prefix;
            prefix = null;
            return token;
        }
        LinToken token = stream == null ? null : stream.nextOrNull();
        if (token == null) {
            stream = null;
            token = suffix;
            suffix = null;
        }
        return token;
    }
}
