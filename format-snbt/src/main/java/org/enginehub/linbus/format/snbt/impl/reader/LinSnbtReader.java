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

import org.enginehub.linbus.stream.LinStream;
import org.enginehub.linbus.stream.exception.NbtParseException;
import org.enginehub.linbus.stream.token.LinToken;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.DataInput;
import java.io.IOException;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.Supplier;

/**
 * Reads a stream of tokens from a {@link DataInput}.
 */
public class LinSnbtReader implements LinStream {

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

    private final Iterator<? extends @NotNull SnbtTokenWithMetadata> input;
    /**
     * The state stack. We're currently on the one that's LAST.
     */
    private final Deque<State> stateStack;
    /**
     * Tokens are pushed to the end of this queue, then yielded from the front.
     */
    private final Deque<LinToken> tokenQueue;
    /**
     * The pushback token stack, which is used to push back tokens that we've read.
     */
    private final Deque<@NotNull SnbtTokenWithMetadata> readAgainStack;
    private int charIndex;

    /**
     * Creates a new reader.
     *
     * @param input the input to read from
     */
    public LinSnbtReader(Iterator<? extends @NotNull SnbtTokenWithMetadata> input) {
        this.input = input;
        this.stateStack = new ArrayDeque<>(List.of(new State.ReadValue(true)));
        this.tokenQueue = new ArrayDeque<>();
        this.readAgainStack = new ArrayDeque<>();
    }

    private @NotNull SnbtTokenWithMetadata read() {
        var token = readAgainStack.pollFirst();
        if (token != null) {
            return token;
        }
        if (!input.hasNext()) {
            throw new NbtParseException(errorPrefix() + "Unexpected end of input");
        }
        var next = input.next();
        charIndex = next.charIndex();
        return next;
    }

    private String errorPrefix() {
        return "At character index " + charIndex + ": ";
    }

    private NbtParseException unexpectedTokenError(SnbtToken token) {
        return new NbtParseException(errorPrefix() + "Unexpected token: " + token);
    }

    private NbtParseException unexpectedTokenSpecificError(SnbtToken token, String expected) {
        return new NbtParseException(errorPrefix() + "Unexpected token: " + token + ", expected " + expected);
    }

    @Override
    public @Nullable LinToken nextOrNull() throws IOException {
        var token = tokenQueue.pollFirst();
        while (token == null) {
            State state = stateStack.peekLast();
            if (state == null) {
                return null;
            }
            fillTokenStack(state);
            token = tokenQueue.pollFirst();
        }

        return token;
    }

    private void fillTokenStack(State state) {
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

    private void readValue(boolean mustBeCompound) {
        // Remove the ReadValue
        stateStack.removeLast();
        var token = read().token();
        if (token instanceof SnbtToken.CompoundStart) {
            stateStack.addLast(new State.InCompound());
            stateStack.addLast(new State.CompoundEntryName());
            tokenQueue.addLast(new LinToken.CompoundStart());
            return;
        }

        if (mustBeCompound) {
            throw unexpectedTokenSpecificError(token, SnbtToken.CompoundStart.INSTANCE.toString());
        }

        if (token instanceof SnbtToken.ListLikeStart) {
            prepareListLike();
        } else if (token instanceof SnbtToken.Text text) {
            var linToken = text.quoted()
                ? new LinToken.String(text.content())
                : getTokenFor(text.content());
            tokenQueue.addLast(linToken);
        } else {
            throw unexpectedTokenError(token);
        }
    }

    private void advanceCompound() {
        var token = read().token();
        if (token instanceof SnbtToken.CompoundEnd) {
            stateStack.removeLast();
            tokenQueue.addLast(new LinToken.CompoundEnd());
        } else if (token instanceof SnbtToken.Separator) {
            stateStack.addLast(new State.CompoundEntryName());
        } else {
            throw unexpectedTokenError(token);
        }
    }

    private void readName() {
        // Remove CompoundEntryName
        stateStack.removeLast();
        var token = read().token();
        if (!(token instanceof SnbtToken.Text text)) {
            throw unexpectedTokenSpecificError(token, "Text");
        }
        token = read().token();
        if (!(token instanceof SnbtToken.EntrySeparator)) {
            throw unexpectedTokenSpecificError(token, SnbtToken.EntrySeparator.INSTANCE.toString());
        }
        stateStack.addLast(new State.ReadValue(false));
        tokenQueue.addLast(new LinToken.Name(text.content()));
    }

    private void advanceList() {
        var token = read().token();
        if (token instanceof SnbtToken.ListLikeEnd) {
            stateStack.removeLast();
            tokenQueue.addLast(new LinToken.ListEnd());
        } else if (token instanceof SnbtToken.Separator) {
            stateStack.addLast(new State.ReadValue(false));
        } else {
            throw unexpectedTokenError(token);
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
    ) {
        boolean isEnd = false;
        var buffer = allocator.apply(BUFFER_SIZE);
        while (buffer.hasRemaining()) {
            var token = read().token();
            if (!(token instanceof SnbtToken.Text text)) {
                throw unexpectedTokenSpecificError(token, "Text");
            }
            LinToken nextValue = getTokenFor(text.content());
            if (!tagType.isInstance(nextValue)) {
                throw new NbtParseException(errorPrefix() + "Expected " + tagType.getSimpleName() + " token, got " + nextValue);
            }
            putter.accept(buffer, tagType.cast(nextValue));
            token = read().token();
            if (token instanceof SnbtToken.ListLikeEnd) {
                isEnd = true;
                break;
            } else if (!(token instanceof SnbtToken.Separator)) {
                throw unexpectedTokenError(token);
            }
        }
        tokenQueue.addLast(contentProducer.apply(buffer));
        if (isEnd) {
            stateStack.removeLast();
            tokenQueue.addLast(endProducer.get());
        }
    }

    private void prepareListLike() {
        int initialCharIndex = charIndex;
        var typing = read();
        if (typing.token() instanceof SnbtToken.Text text && !text.quoted() && text.content().length() == 1) {
            var separatorCheck = read();
            if (separatorCheck.token() instanceof SnbtToken.ListTypeSeparator) {
                switch (text.content().charAt(0)) {
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
                    default -> throw new NbtParseException(errorPrefix() + "Invalid array type: " + text.content());
                }
                return;
            }
            readAgainStack.addFirst(separatorCheck);
        }
        readAgainStack.addFirst(typing);
        charIndex = initialCharIndex;

        stateStack.addLast(new State.InList());
        stateStack.addLast(new State.ReadValue(false));
        tokenQueue.addLast(new LinToken.ListStart());
    }

    private LinToken getTokenFor(String valueString) {
        // valueString is guaranteed to be non-empty, because this is always a simple value
        char last = valueString.charAt(valueString.length() - 1);
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
