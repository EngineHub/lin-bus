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

package org.enginehub.linbus.format.snbt.impl;

import org.enginehub.linbus.common.internal.AbstractIterator;
import org.enginehub.linbus.common.internal.CharPredicate;
import org.enginehub.linbus.stream.token.LinToken;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.DataInput;
import java.io.EOFException;
import java.io.IOException;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.Supplier;

/**
 * Reads a stream of tokens from a {@link DataInput}.
 */
public class LinSnbtReader extends AbstractIterator<LinToken> {

    private sealed interface State {
        /**
         * We're inside a compound right now.
         *
         * <p>
         * The cursor should either point to a comma or a closing brace.
         * </p>
         */
        record InCompound() implements State {
        }

        /**
         * We need to get the name of the next entry.
         *
         * <p>
         * After this, the cursor will be one character after the colon.
         * </p>
         */
        record CompoundEntryName() implements State {
        }

        /**
         * We're inside a list right now.
         *
         * <p>
         * The cursor should either point to a comma or a closing bracket.
         * </p>
         */
        record InList() implements State {
        }

        /**
         * We're inside a byte array right now.
         *
         * <p>
         * The cursor should always point to the start of a value.
         * </p>
         */
        record InByteArray() implements State {
        }

        /**
         * We're inside an int array right now.
         *
         * <p>
         * The cursor should always point to the start of a value.
         * </p>
         */
        record InIntArray() implements State {
        }

        /**
         * We're inside a long array right now.
         *
         * <p>
         * The cursor should always point to the start of a value.
         * </p>
         */
        record InLongArray() implements State {
        }

        /**
         * We need to read a value. Usually, we'll just return the value, and not push a new state, unless we need to
         * read a complex value such as a compound or list.
         */
        record ReadValue(boolean mustBeCompound) implements State {
        }
    }

    private static final int BUFFER_SIZE = 4096;

    private final Reader input;
    /**
     * The state stack. We're currently on the one that's LAST.
     */
    private final Deque<State> stateStack;
    /**
     * The leftover token stack. Because there's no coroutines. We're currently on the one that's LAST.
     */
    private final Deque<LinToken> tokenQueue;
    private int charIndex;

    /**
     * Creates a new reader.
     *
     * @param input the input to read from
     */
    public LinSnbtReader(Reader input) {
        this.input = input.markSupported() ? input : new BufferedReader(input);
        this.stateStack = new ArrayDeque<>(List.of(new State.ReadValue(true)));
        this.tokenQueue = new ArrayDeque<>();
    }

    private char readChar() throws IOException {
        int cEof = input.read();
        if (cEof == -1) {
            throw new EOFException(errorPrefix() + "Unexpected end of input");
        }
        charIndex++;
        return (char) cEof;
    }

    private char readCharSkipWhitespace(int markBeforeReading) throws IOException {
        char c;
        do {
            if (markBeforeReading > 0) {
                input.mark(markBeforeReading);
            }
            c = readChar();
        } while (Character.isWhitespace(c));
        return c;
    }

    private void readWhitespaceUntil(CharPredicate c) throws IOException {
        char read;
        while (true) {
            read = readChar();
            if (c.test(read)) {
                break;
            }
            if (!Character.isWhitespace(read)) {
                throw new IllegalStateException(errorPrefix() + "Unexpected character " + c);
            }
        }
    }

    private String errorPrefix() {
        return "At character index " + charIndex + ": ";
    }

    private @NotNull IllegalStateException unexpectedCharacterError(char c) {
        return new IllegalStateException(errorPrefix() + "Unexpected character: " + c);
    }

    @Override
    protected LinToken computeNext() {
        var token = tokenQueue.pollFirst();
        while (token == null) {
            try {
                State state = stateStack.peekLast();
                if (state == null) {
                    return end();
                }
                fillTokenStack(state);
                token = tokenQueue.pollFirst();
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }

        return token;
    }

    private void fillTokenStack(State state) throws IOException {
        if (state instanceof State.ReadValue rv) {
            readValue(rv.mustBeCompound);
        } else if (state instanceof State.InCompound) {
            advanceCompound();
        } else if (state instanceof State.CompoundEntryName) {
            readName();
        } else if (state instanceof State.InList) {
            advanceList();
        } else if (state instanceof State.InByteArray) {
            advanceArray(
                LinToken.Byte.class,
                ByteBuffer::allocate,
                (buffer, t) -> buffer.put(t.value()),
                buffer -> new LinToken.ByteArrayContent(buffer.flip().asReadOnlyBuffer()),
                LinToken.ByteArrayEnd::new
            );
        } else if (state instanceof State.InIntArray) {
            advanceArray(
                LinToken.Int.class,
                IntBuffer::allocate,
                (buffer, t) -> buffer.put(t.value()),
                buffer -> new LinToken.IntArrayContent(buffer.flip().asReadOnlyBuffer()),
                LinToken.IntArrayEnd::new
            );
        } else if (state instanceof State.InLongArray) {
            advanceArray(
                LinToken.Long.class,
                LongBuffer::allocate,
                (buffer, t) -> buffer.put(t.value()),
                buffer -> new LinToken.LongArrayContent(buffer.flip().asReadOnlyBuffer()),
                LinToken.LongArrayEnd::new
            );
        } else {
            throw new IllegalStateException(errorPrefix() + "Unknown state: " + state);
        }
    }

    private void readValue(boolean mustBeCompound) throws IOException {
        // Remove the ReadValue
        stateStack.removeLast();
        char c = readCharSkipWhitespace(1);
        if (mustBeCompound && c != '{') {
            throw new IllegalStateException(errorPrefix() + "Expected '{' but got '" + c + "'");
        }
        switch (c) {
            case '{' -> {
                stateStack.addLast(new State.InCompound());
                stateStack.addLast(new State.CompoundEntryName());
                tokenQueue.addLast(new LinToken.CompoundStart());
            }
            case '[' -> prepareListLike();
            case '"', '\'' -> tokenQueue.addLast(new LinToken.String(parseString(c)));
            default -> {
                // Reset before the char we read
                input.reset();
                charIndex -= 1;
                LinToken token = readSimpleValue();
                tokenQueue.addLast(token);
            }
        }
    }

    private LinToken readSimpleValue() throws IOException {
        char c;
        var builder = new StringBuilder();
        while (true) {
            // Ensure we can restore this character if it's the terminator.
            input.mark(1);
            c = readChar();
            if (c == ',' || c == '}' || c == ']') {
                break;
            }
            if (Character.isWhitespace(c)) {
                readWhitespaceUntil(next -> next == ',' || next == '}' || next == ']');
                break;
            }
            if (!Elusion.isSafeCharacter(c)) {
                throw unexpectedCharacterError(c);
            }
            builder.append(c);
        }
        input.reset();
        charIndex -= 1;

        // Convert our collected value into the typed token it really is.
        return getTokenFor(builder.toString());
    }

    private void advanceCompound() throws IOException {
        char c = readCharSkipWhitespace(-1);
        if (c == '}') {
            stateStack.removeLast();
            tokenQueue.addLast(new LinToken.CompoundEnd());
        } else if (c == ',') {
            stateStack.addLast(new State.CompoundEntryName());
        } else {
            throw unexpectedCharacterError(c);
        }
    }

    private void readName() throws IOException {
        // Remove CompoundEntryName
        stateStack.removeLast();
        char firstChar = readCharSkipWhitespace(-1);
        var name = switch (firstChar) {
            case '"', '\'' -> {
                String string = parseString(firstChar);
                char c = readCharSkipWhitespace(-1);
                if (c != ':') {
                    throw unexpectedCharacterError(c);
                }
                yield string;
            }
            default -> {
                var builder = new StringBuilder();
                builder.append(firstChar);
                while (true) {
                    char c = readChar();
                    if (c == ':') {
                        break;
                    }
                    // If we hit whitespace, eat until we get the ':'
                    if (Character.isWhitespace(c)) {
                        readWhitespaceUntil(next -> next == ':');
                        break;
                    }
                    if (!Elusion.isSafeCharacter(c)) {
                        throw unexpectedCharacterError(c);
                    }
                    builder.append(c);
                }
                yield builder.toString();
            }
        };
        stateStack.addLast(new State.ReadValue(false));
        tokenQueue.addLast(new LinToken.Name(name));
    }

    private void advanceList() throws IOException {
        char c = readCharSkipWhitespace(-1);
        if (c == ']') {
            stateStack.removeLast();
            tokenQueue.addLast(new LinToken.ListEnd());
        } else if (c == ',') {
            stateStack.addLast(new State.ReadValue(false));
        } else {
            throw unexpectedCharacterError(c);
        }
    }

    private interface ArrayAdvancer<T extends Buffer, L extends LinToken> {
        Class<L> tagType();

        T allocate();

        void put(T array, L token);

        LinToken produceContent(T array);

        LinToken produceEnd();
    }

    private <T extends Buffer, L extends LinToken> void advanceArray(
        Class<L> tagType,
        IntFunction<T> allocator,
        BiConsumer<T, L> putter,
        Function<T, LinToken> contentProducer,
        Supplier<LinToken> endProducer
    ) throws IOException {
        boolean isEnd = false;
        var buffer = allocator.apply(BUFFER_SIZE);
        while (buffer.hasRemaining()) {
            LinToken nextValue = readSimpleValue();
            if (!tagType.isInstance(nextValue)) {
                throw new IllegalStateException(errorPrefix() + "Expected " + tagType.getSimpleName() + " token, got " + nextValue);
            }
            putter.accept(buffer, tagType.cast(nextValue));
            System.err.println("Read " + nextValue);
            char c = readCharSkipWhitespace(-1);
            System.err.println("Read " + c);
            if (c == ']') {
                isEnd = true;
                break;
            } else if (c != ',') {
                throw unexpectedCharacterError(c);
            }
        }
        tokenQueue.addLast(contentProducer.apply(buffer));
        if (isEnd) {
            stateStack.removeLast();
            tokenQueue.addLast(endProducer.get());
        }
    }

    private void prepareListLike() throws IOException {
        input.mark(2);
        char typing = readChar();
        char semicolonCheck = readChar();
        if (semicolonCheck == ';' && typing != '"' && typing != '\'') {
            switch (typing) {
                case 'B' -> {
                    stateStack.addLast(new State.InByteArray());
                    tokenQueue.addLast(new LinToken.ByteArrayStart());
                }
                case 'I' -> {
                    stateStack.addLast(new State.InIntArray());
                    tokenQueue.addLast(new LinToken.IntArrayStart());
                }
                case 'L' -> {
                    stateStack.addLast(new State.InLongArray());
                    tokenQueue.addLast(new LinToken.LongArrayStart());
                }
                default -> throw new IllegalStateException(errorPrefix() + "Invalid array type: " + typing);
            }
        } else {
            // Not an array, reset to the '['
            input.reset();
            charIndex -= 2;

            stateStack.addLast(new State.InList());
            stateStack.addLast(new State.ReadValue(false));
            tokenQueue.addLast(new LinToken.ListStart());
        }
    }

    private String parseString(char quoteChar) throws IOException {
        var sb = new StringBuilder();
        boolean escaped = false;
        while (true) {
            char c = readChar();
            if (!escaped) {
                if (c == quoteChar) {
                    return sb.toString();
                } else if (c == '\\') {
                    escaped = true;
                    continue;
                }
            } else if (c != quoteChar && c != '\\') {
                throw new IllegalStateException(errorPrefix() + "Invalid escape: \\" + c);
            } else {
                escaped = false;
            }
            sb.append(c);
        }
    }

    private LinToken getTokenFor(String valueString) {
        char last = valueString.isEmpty() ? '\0' : valueString.charAt(valueString.length() - 1);
        return switch (last) {
            case 'B', 'b' -> {
                try {
                    yield new LinToken.Byte(Byte.parseByte(valueString.substring(0, valueString.length() - 1)));
                } catch (NumberFormatException e) {
                    yield new LinToken.String(valueString);
                }
            }
            case 'L', 'l' -> {
                try {
                    yield new LinToken.Long(Long.parseLong(valueString.substring(0, valueString.length() - 1)));
                } catch (NumberFormatException e) {
                    yield new LinToken.String(valueString);
                }
            }
            case 'S', 's' -> {
                try {
                    yield new LinToken.Short(Short.parseShort(valueString.substring(0, valueString.length() - 1)));
                } catch (NumberFormatException e) {
                    yield new LinToken.String(valueString);
                }
            }
            // Note: I deviate from the Mojang implementation here, which does NOT consider NaN and Infinity as
            // floats/doubles. I think this is an implementation bug, but unsure.
            case 'F', 'f' -> {
                try {
                    yield new LinToken.Float(Float.parseFloat(valueString.substring(0, valueString.length() - 1)));
                } catch (NumberFormatException e) {
                    yield new LinToken.String(valueString);
                }
            }
            case 'D', 'd' -> {
                try {
                    yield new LinToken.Double(Double.parseDouble(valueString.substring(0, valueString.length() - 1)));
                } catch (NumberFormatException e) {
                    yield new LinToken.String(valueString);
                }
            }
            default -> {
                // Might be an integer.
                try {
                    yield new LinToken.Int(Integer.parseInt(valueString));
                } catch (NumberFormatException e) {
                    // Nope.
                }
                // Might be a double.
                try {
                    yield new LinToken.Double(Double.parseDouble(valueString));
                } catch (NumberFormatException e) {
                    // Nope.
                }
                // Might be a boolean.
                boolean isTrue = valueString.equalsIgnoreCase("true");
                if (isTrue || valueString.equalsIgnoreCase("false")) {
                    yield new LinToken.Byte((byte) (isTrue ? 1 : 0));
                }
                // Nope, it's a string.
                yield new LinToken.String(valueString);
            }
        };
    }
}
