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

package org.enginehub.linbus.stream.impl;

import org.enginehub.linbus.common.LinTagId;
import org.enginehub.linbus.common.internal.AbstractIterator;
import org.enginehub.linbus.stream.token.LinToken;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;

public class OptionalInfoCalculator extends AbstractIterator<LinToken> {
    private interface OptionalFill {
        /**
         * Try to fill the optional value with another token.
         *
         * @return the filled token if done
         */
        @Nullable LinToken tryFill(LinToken token);
    }

    private final Iterator<? extends @NotNull LinToken> original;
    private Deque<LinToken> tokenBuffer;

    public OptionalInfoCalculator(Iterator<? extends @NotNull LinToken> original) {
        this.original = original;
    }

    @Override
    protected LinToken computeNext() {
        if (tokenBuffer != null) {
            var next = tokenBuffer.pollFirst();
            if (next != null) {
                return next;
            } else {
                tokenBuffer = null;
            }
        }
        if (!original.hasNext()) {
            return end();
        }

        TokenAndBuffer tokenAndBuffer = fillIfNeeded(original.next());
        if (tokenAndBuffer.buffer != null && !tokenAndBuffer.buffer.isEmpty()) {
            tokenBuffer = tokenAndBuffer.buffer;
        }
        return tokenAndBuffer.token;
    }

    private record TokenAndBuffer(@NotNull LinToken token, @Nullable Deque<LinToken> buffer) {
    }

    private TokenAndBuffer fillIfNeeded(LinToken token) {
        if (token instanceof LinToken.ListStart listStart && (listStart.size().isEmpty() || listStart.elementId().isEmpty())) {
            return getFilled(new ListStartFill(listStart));
        } else if (token instanceof LinToken.ByteArrayStart byteArrayStart) {
            if (byteArrayStart.size().isPresent()) {
                return new TokenAndBuffer(token, null);
            }
            return getFilled(new ByteArrayStartFill());
        } else if (token instanceof LinToken.IntArrayStart intArrayStart) {
            if (intArrayStart.size().isPresent()) {
                return new TokenAndBuffer(token, null);
            }
            return getFilled(new IntArrayStartFill());
        } else if (token instanceof LinToken.LongArrayStart longArrayStart && longArrayStart.size().isEmpty()) {
            return getFilled(new LongArrayStartFill());
        } else if (token instanceof LinToken.Name name && name.id().isEmpty()) {
            return getFilled(new NameFill(name.name()));
        }
        return new TokenAndBuffer(token, null);
    }

    private TokenAndBuffer getFilled(OptionalFill fill) {
        var buffer = new ArrayDeque<LinToken>();
        var consumedTokenStack = new ArrayDeque<LinToken>();
        while (!buffer.isEmpty() || original.hasNext()) {
            var next = buffer.pollFirst();
            if (next == null) {
                // Replenish our buffer by taking the next token (and filling if needed).
                var tokenAndBuffer = fillIfNeeded(original.next());
                buffer.add(tokenAndBuffer.token);
                consumedTokenStack.add(tokenAndBuffer.token);
                if (tokenAndBuffer.buffer != null) {
                    buffer.addAll(tokenAndBuffer.buffer);
                    consumedTokenStack.addAll(tokenAndBuffer.buffer);
                }
                continue;
            }
            var filled = fill.tryFill(next);
            if (filled != null) {
                return new TokenAndBuffer(filled, consumedTokenStack);
            }
        }
        throw new IllegalStateException("Optional value not filled by the end of token stream");
    }

    private static final class ListStartFill implements OptionalFill {
        private final int knownSize;
        private final ValueCounter counter;
        private LinTagId elementId;

        public ListStartFill(LinToken.ListStart listStart) {
            if (listStart.size().isPresent()) {
                this.knownSize = listStart.size().getAsInt();
                this.counter = null;
            } else {
                this.knownSize = -1;
                this.counter = new ValueCounter();
            }
            this.elementId = listStart.elementId().orElse(null);
        }

        @Override
        public @Nullable LinToken tryFill(LinToken token) {
            if (counter == null || !counter.isNested()) {
                var elementId = this.elementId;
                if (elementId == null) {
                    if (token instanceof LinToken.ListEnd) {
                        elementId = LinTagId.END;
                    } else {
                        elementId = token.tagId().orElseThrow(() ->
                            new IllegalStateException("Token doesn't represent a tag directly: " + token)
                        );
                    }
                    this.elementId = elementId;
                }
                if (counter == null) {
                    return new LinToken.ListStart(knownSize, elementId);
                }
                if (token instanceof LinToken.ListEnd) {
                    return new LinToken.ListStart(counter.count(), elementId);
                }
            }
            counter.add(token);
            return null;
        }
    }

    private static final class ByteArrayStartFill implements OptionalFill {
        private int size;

        @Override
        public @Nullable LinToken tryFill(LinToken token) {
            if (token instanceof LinToken.ByteArrayEnd) {
                return new LinToken.ByteArrayStart(size);
            }

            if (token instanceof LinToken.ByteArrayContent content) {
                size += content.buffer().remaining();
            } else {
                throw new IllegalStateException("Unexpected token: " + token);
            }
            return null;
        }
    }

    private static final class IntArrayStartFill implements OptionalFill {
        private int size;

        @Override
        public @Nullable LinToken tryFill(LinToken token) {
            if (token instanceof LinToken.IntArrayEnd) {
                return new LinToken.IntArrayStart(size);
            }

            if (token instanceof LinToken.IntArrayContent content) {
                size += content.buffer().remaining();
            } else {
                throw new IllegalStateException("Unexpected token: " + token);
            }
            return null;
        }
    }

    private static final class LongArrayStartFill implements OptionalFill {
        private int size;

        @Override
        public @Nullable LinToken tryFill(LinToken token) {
            if (token instanceof LinToken.LongArrayEnd) {
                return new LinToken.LongArrayStart(size);
            }

            if (token instanceof LinToken.LongArrayContent content) {
                size += content.buffer().remaining();
            } else {
                throw new IllegalStateException("Unexpected token: " + token);
            }
            return null;
        }
    }

    private static final class NameFill implements OptionalFill {
        private final String name;

        private NameFill(String name) {
            this.name = name;
        }

        @Override
        public LinToken tryFill(LinToken token) {
            return new LinToken.Name(name, token.tagId().orElseThrow(() ->
                new IllegalStateException("Token doesn't represent a tag directly: " + token)
            ));
        }
    }
}
