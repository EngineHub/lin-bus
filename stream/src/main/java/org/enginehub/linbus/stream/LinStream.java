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

import org.enginehub.linbus.common.internal.AbstractIterator;
import org.enginehub.linbus.stream.impl.OptionalInfoCalculator;
import org.enginehub.linbus.stream.token.LinToken;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Iterator;
import java.util.Optional;

/**
 * Represents a stream of {@link LinToken LinTokens}.
 */
public interface LinStream extends LinStreamable {
    /**
     * {@return an empty stream}
     */
    static LinStream of() {
        return () -> null;
    }

    /**
     * {@return a stream of a single token}
     *
     * @param token the token
     */
    static LinStream of(LinToken token) {
        return new LinStream() {
            private LinToken nextToken = token;

            @Override
            public @Nullable LinToken nextOrNull() {
                var token = nextToken;
                nextToken = null;
                return token;
            }
        };
    }

    /**
     * {@return a stream over all the given tokens}
     *
     * @param tokens the tokens
     */
    static LinStream of(LinToken... tokens) {
        return new LinStream() {
            private int index = 0;

            @Override
            public @Nullable LinToken nextOrNull() {
                if (index >= tokens.length) {
                    return null;
                }
                var token = tokens[index];
                index++;
                return token;
            }
        };
    }

    /**
     * {@return the next token in the stream if any} If this returns {@code null}, the stream is exhausted. Once a
     * stream is exhausted, it will always return {@code null}.
     *
     * @throws IOException if an I/O error occurs
     */
    @Nullable LinToken nextOrNull() throws IOException;

    /**
     * {@return the next token in the stream if any} This API is awarded the shorter name in expectation of Project
     * Valhalla, where it will become just as efficient as the {@link #nextOrNull()} method.
     *
     * @throws IOException if an I/O error occurs
     */
    default Optional<LinToken> next() throws IOException {
        return Optional.ofNullable(nextOrNull());
    }

    /**
     * Fill in optional information, such as list sizes and types.
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
     * @return the content of this stream, but with optional information filled in
     */
    default LinStream calculateOptionalInfo() {
        return new OptionalInfoCalculator(this);
    }

    /**
     * A {@link LinStream} is always streamable as itself.
     *
     * @return this
     */
    @Override
    default @NotNull LinStream linStream() {
        return this;
    }

    /**
     * Convert this stream to an {@link Iterator}. You should not use this stream after this method is called.
     *
     * <p>
     * Any {@link IOException}s thrown by this stream will be propagated as {@link UncheckedIOException}s.
     * </p>
     *
     * @return an iterator over this stream
     */
    default @NotNull Iterator<LinToken> asIterator() {
        return new AbstractIterator<>() {
            @Override
            protected @Nullable LinToken computeNext() {
                try {
                    LinToken linToken = nextOrNull();
                    return linToken == null ? end() : linToken;
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            }
        };
    }
}
