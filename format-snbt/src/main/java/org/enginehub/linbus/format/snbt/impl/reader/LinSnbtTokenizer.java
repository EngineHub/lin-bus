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

package org.enginehub.linbus.format.snbt.impl.reader;

import org.enginehub.linbus.common.internal.AbstractIterator;
import org.enginehub.linbus.format.snbt.impl.Elusion;
import org.enginehub.linbus.stream.exception.NbtParseException;
import org.enginehub.linbus.stream.token.LinToken;
import org.jspecify.annotations.Nullable;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.io.UncheckedIOException;

/**
 * But not like, a tokenizer into {@link LinToken}s. But like our own internal {@link SnbtToken}. Sigh.
 */
public class LinSnbtTokenizer extends AbstractIterator<SnbtTokenWithMetadata> {
    private final Reader input;
    private int charIndex = -1;
    private boolean eatAllWhitespaceAfter;

    /**
     * Create a new tokenizer.
     *
     * @param input the input to read from
     */
    public LinSnbtTokenizer(Reader input) {
        this.input = input.markSupported() ? input : new BufferedReader(input);
    }

    private String errorPrefix() {
        return "At character index " + charIndex + ": ";
    }

    private void skipWhitespace() throws IOException {
        while (true) {
            input.mark(1);
            int next = input.read();
            if (next == -1) {
                break;
            }
            charIndex++;
            if (!Character.isWhitespace(next)) {
                input.reset();
                charIndex--;
                break;
            }
        }
    }

    @Override
    protected @Nullable SnbtTokenWithMetadata computeNext() {
        try {
            skipWhitespace();
            input.mark(1);
            int next = input.read();
            if (next == -1) {
                return end();
            }
            charIndex++;
            return switch (next) {
                case '{' -> new SnbtTokenWithMetadata(SnbtToken.CompoundStart.INSTANCE, charIndex);
                case '}' -> new SnbtTokenWithMetadata(SnbtToken.CompoundEnd.INSTANCE, charIndex);
                case '[' -> new SnbtTokenWithMetadata(SnbtToken.ListLikeStart.INSTANCE, charIndex);
                case ']' -> new SnbtTokenWithMetadata(SnbtToken.ListLikeEnd.INSTANCE, charIndex);
                case ':' -> new SnbtTokenWithMetadata(SnbtToken.EntrySeparator.INSTANCE, charIndex);
                case ';' -> new SnbtTokenWithMetadata(SnbtToken.ListTypeSeparator.INSTANCE, charIndex);
                case ',' -> new SnbtTokenWithMetadata(SnbtToken.Separator.INSTANCE, charIndex);
                case '"', '\'' -> {
                    int initialCharIndex = this.charIndex;
                    yield new SnbtTokenWithMetadata(new SnbtToken.Text(true, readQuotedText((char) next)), initialCharIndex);
                }
                default -> {
                    input.reset();
                    charIndex--;
                    yield readSimpleValue();
                }
            };
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private SnbtTokenWithMetadata readSimpleValue() throws IOException {
        int initialCharIndex = charIndex + 1;
        var builder = new StringBuilder();
        boolean wasWhitespace = false;
        while (true) {
            input.mark(1);
            int next = input.read();
            if (next == -1) {
                break;
            }
            charIndex++;
            if (next == ',' || next == ':' || next == ';' || next == '}' || next == ']') {
                input.reset();
                charIndex--;
                break;
            } else if (Character.isWhitespace(next)) {
                wasWhitespace = true;
                continue;
            } else if (!Elusion.isSafeCharacter((char) next)) {
                throw new NbtParseException(errorPrefix() + "Unexpected character: " + (char) next);
            } else if (wasWhitespace) {
                // Whitespace can only occur before terminators, not in the middle of a value.
                throw new NbtParseException(errorPrefix() + "Found non-terminator after whitespace");
            }
            builder.append((char) next);
        }
        return new SnbtTokenWithMetadata(new SnbtToken.Text(false, builder.toString()), initialCharIndex);
    }

    private String readQuotedText(char quoteChar) throws IOException {
        var sb = new StringBuilder();
        boolean escaped = false;
        while (true) {
            int c = input.read();
            if (c == -1) {
                throw new NbtParseException(errorPrefix() + "Unexpected end of input in quoted value");
            }
            charIndex++;
            if (!escaped) {
                if (c == quoteChar) {
                    return sb.toString();
                } else if (c == '\\') {
                    escaped = true;
                    continue;
                }
            } else if (c != quoteChar && c != '\\') {
                throw new NbtParseException(errorPrefix() + "Invalid escape: \\" + (char) c);
            } else {
                escaped = false;
            }
            sb.append((char) c);
        }
    }
}
