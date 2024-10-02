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
import org.enginehub.linbus.stream.LinReadOptions;
import org.enginehub.linbus.stream.LinStream;
import org.enginehub.linbus.stream.exception.NbtParseException;
import org.enginehub.linbus.stream.token.LinToken;
import org.jspecify.annotations.Nullable;

import java.io.ByteArrayInputStream;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CoderResult;
import java.nio.charset.StandardCharsets;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

/**
 * Reads a stream of tokens from a {@link DataInput}.
 */
public class LinNbtReader implements LinStream {

    /**
     * The start of a 2-byte null character in modified UTF-8.
     */
    private static final byte TWO_BYTE_NULL_START = (byte) 0b1100_0000;
    /**
     * The end of a 2-byte null character in modified UTF-8.
     */
    public static final byte TWO_BYTE_NULL_END = (byte) 0b10000000;
    private static final int TOP_5_BITS = 0b1111_1000;
    /**
     * The start of a 4-byte character in UTF-8 (top 5 bits).
     */
    private static final int FOUR_BYTE_START = 0b1111_0000;
    /**
     * The 3-byte start {@code 1110} plus {@code 1101}, the start of the surrogate indicator bits.
     */
    private static final byte THREE_BYTE_SURROGATE_START = (byte) 0b1110_1101;
    private static final int TOP_3_BITS = 0b1110_0000;
    /**
     * The continuation from {@link #THREE_BYTE_SURROGATE_START} for the surrogate indicator bits, with the
     * {@code 10} bits for the second byte of a 3-byte character (top 3 bits).
     */
    private static final int THREE_BYTE_SURROGATE_CONTINUATION = 0b1010_0000;

    private static StringEncoding getGuaranteedStringEncoding(ByteBuffer bytes) {
        // The differences between the modified UTF-8 format and the standard UTF-8 format are the following:
        // The null byte '\u0000' is encoded in 2-byte format rather than 1-byte, so that the encoded strings never have embedded nulls.
        // Only the 1-byte, 2-byte, and 3-byte formats are used.
        // Supplementary characters are represented in the form of surrogate pairs.

        // However, the DataInputStream will accept a null-byte.
        // So we can't use those as a definitive indicator of modified UTF-8 or not.
        boolean sawTwoByteNullStart = false;
        boolean sawThreeByteSurrogateStart = false;
        for (int i = 0; i < bytes.remaining(); i++) {
            byte b = bytes.get(i);
            if (b == TWO_BYTE_NULL_START) {
                sawTwoByteNullStart = true;
            } else if (sawTwoByteNullStart) {
                if (b == TWO_BYTE_NULL_END) {
                    return StringEncoding.MODIFIED_UTF_8;
                } else {
                    sawTwoByteNullStart = false;
                }
            }

            if ((b & TOP_5_BITS) == FOUR_BYTE_START) {
                // 4-byte start
                return StringEncoding.NORMAL_UTF_8;
            }

            if (b == THREE_BYTE_SURROGATE_START) {
                sawThreeByteSurrogateStart = true;
            } else if (sawThreeByteSurrogateStart) {
                if ((b & TOP_3_BITS) == THREE_BYTE_SURROGATE_CONTINUATION) {
                    // Assume this is a properly encoded surrogate, and that this is modified UTF-8
                    // Any errors will be caught by the UTF-8 decoder.
                    return StringEncoding.MODIFIED_UTF_8;
                } else {
                    sawThreeByteSurrogateStart = false;
                }
            }
        }
        return StringEncoding.UNKNOWN;
    }

    private sealed interface State {
        /**
         * We need to initialize and return the root name.
         */
        record Initial() implements State {
        }

        /**
         * We need to return {@link LinToken.CompoundStart}.
         */
        record CompoundStart() implements State {
        }

        /**
         * We need to give the name of the next entry. We'll load the ID here too.
         */
        record CompoundEntryName() implements State {
        }

        /**
         * This is a bit hacky, we don't want to fill the stack with entries equal to the size of the list. So we record
         * the remaining entries and the id of the elements, and replace the entry on the stack with a {@link ReadValue}
         * at the start of the loop.
         */
        record ListEntry(int remaining, LinTagId elementId) implements State {
        }

        /**
         * We need to read a value. Usually, we'll just return the value, and not push a new state, unless we need to
         * read a complex value such as a compound, list, or array.
         */
        record ReadValue(LinTagId id) implements State {
        }

        /**
         * We're currently reading a byte array. We'll emit content as needed.
         */
        record ReadByteArray(int remaining) implements State {
        }

        /**
         * We're currently reading an int array. We'll emit content as needed.
         */
        record ReadIntArray(int remaining) implements State {
        }

        /**
         * We're currently reading a long array. We'll emit content as needed.
         */
        record ReadLongArray(int remaining) implements State {
        }
    }

    private enum StringEncoding {
        MODIFIED_UTF_8,
        NORMAL_UTF_8,
        UNKNOWN,
    }

    private static final class NormalUtf8Decoder {
        private final CharsetDecoder decoder = StandardCharsets.UTF_8.newDecoder();
        // Default to some small allocation that is likely to cover most strings.
        private ByteBuffer sourceBuffer = ByteBuffer.allocate(128);
        private CharBuffer decodeBuffer = CharBuffer.allocate(128);

        void fill(DataInput input, int length) throws IOException {
            ensureSourceBufferCapacity(length);
            input.readFully(sourceBuffer.array(), 0, length);
            sourceBuffer.limit(length);
        }

        private void ensureSourceBufferCapacity(int requiredCapacity) {
            if (sourceBuffer.capacity() < requiredCapacity) {
                sourceBuffer = ByteBuffer.allocate(requiredCapacity);
            } else {
                sourceBuffer.clear();
            }
        }

        private void ensureCharBufferCapacity(int requiredCapacity) {
            if (decodeBuffer.capacity() < requiredCapacity) {
                decodeBuffer = CharBuffer.allocate(requiredCapacity);
            } else {
                decodeBuffer.clear();
            }
        }

        public String decode() throws CharacterCodingException {
            int n = (int) (sourceBuffer.remaining() * decoder.averageCharsPerByte());
            ensureCharBufferCapacity(n);

            if ((n == 0) && (sourceBuffer.remaining() == 0))
                return "";
            decoder.reset();
            for (; ; ) {
                CoderResult cr = sourceBuffer.hasRemaining()
                    ? decoder.decode(sourceBuffer, decodeBuffer, true)
                    : CoderResult.UNDERFLOW;
                if (cr.isUnderflow()) {
                    cr = decoder.flush(decodeBuffer);
                }

                if (cr.isUnderflow()) {
                    break;
                }
                if (cr.isOverflow()) {
                    // Ensure progress; n might be 0!
                    n += n / 2 + 1;
                    CharBuffer o = CharBuffer.allocate(n);
                    decodeBuffer.flip();
                    o.put(decodeBuffer);
                    decodeBuffer = o;
                    continue;
                }
                cr.throwException();
            }
            decodeBuffer.flip();
            return decodeBuffer.toString();
        }
    }

    private final DataInput input;
    /**
     * The state stack. We're currently on the one that's LAST.
     */
    private final Deque<State> stateStack;
    private StringEncoding stringEncoding;
    private @Nullable NormalUtf8Decoder decoder;

    /**
     * Creates a new reader.
     *
     * @param input the input to read from
     * @param options the options to use when reading
     */
    public LinNbtReader(DataInput input, LinReadOptions options) {
        this.input = input;
        this.stateStack = new ArrayDeque<>(List.of(new State.Initial()));
        // We only need to check strings if we're allowing JNBT encoding.
        this.stringEncoding = options.allowJnbtStringEncoding()
            ? StringEncoding.UNKNOWN : StringEncoding.MODIFIED_UTF_8;
    }

    @Override
    public @Nullable LinToken nextOrNull() throws IOException {
        var state = stateStack.pollLast();
        return switch (state) {
            case null -> null;
            case State.Initial initial -> {
                if (input.readUnsignedByte() != LinTagId.COMPOUND.id()) {
                    throw new NbtParseException("NBT stream does not start with a compound tag");
                }
                stateStack.addLast(new State.CompoundStart());
                yield new LinToken.Name(readUtf(), LinTagId.COMPOUND);
            }
            case State.CompoundStart compoundStart -> {
                stateStack.addLast(new State.CompoundEntryName());
                yield new LinToken.CompoundStart();
            }
            case State.CompoundEntryName compoundEntryName -> {
                var id = LinTagId.fromId(input.readUnsignedByte());
                if (id == LinTagId.END) {
                    yield new LinToken.CompoundEnd();
                }

                // After we read the value, we'll be back at reading the name.
                stateStack.addLast(new State.CompoundEntryName());
                stateStack.addLast(new State.ReadValue(id));
                yield new LinToken.Name(readUtf(), id);
            }
            case State.ReadValue(LinTagId id) -> handleReadValue(id);
            case State.ReadByteArray(int remaining) -> {
                if (remaining == 0) {
                    // We're done reading the array. Return the end token.
                    // This will also implicitly return to the state in the stack below the array.
                    yield new LinToken.ByteArrayEnd();
                }
                ByteBuffer buffer = ByteBuffer.allocate(Math.min(8192, remaining));
                input.readFully(buffer.array(), buffer.position(), buffer.remaining());
                stateStack.addLast(new State.ReadByteArray(remaining - buffer.remaining()));
                yield new LinToken.ByteArrayContent(buffer.asReadOnlyBuffer());
            }
            case State.ReadIntArray(int remaining) -> {
                if (remaining == 0) {
                    // We're done reading the array. Return the end token.
                    // This will also implicitly return to the state in the stack below the array.
                    yield new LinToken.IntArrayEnd();
                }
                ByteBuffer buffer = ByteBuffer.allocate(Math.min(8192, remaining * 4));
                input.readFully(buffer.array(), buffer.position(), buffer.remaining());
                stateStack.addLast(new State.ReadIntArray(remaining - buffer.remaining() / 4));
                yield new LinToken.IntArrayContent(buffer.asIntBuffer().asReadOnlyBuffer());
            }
            case State.ReadLongArray(int remaining) -> {
                if (remaining == 0) {
                    // We're done reading the array. Return the end token.
                    // This will also implicitly return to the state in the stack below the array.
                    yield new LinToken.LongArrayEnd();
                }
                ByteBuffer buffer = ByteBuffer.allocate(Math.min(8192, remaining * 8));
                input.readFully(buffer.array(), buffer.position(), buffer.remaining());
                stateStack.addLast(new State.ReadLongArray(remaining - buffer.remaining() / 8));
                yield new LinToken.LongArrayContent(buffer.asLongBuffer().asReadOnlyBuffer());
            }
            case State.ListEntry(int remaining, LinTagId elementId) -> {
                if (remaining == 0) {
                    yield new LinToken.ListEnd();
                }
                stateStack.addLast(new State.ListEntry(remaining - 1, elementId));
                yield handleReadValue(elementId);
            }
        };
    }

    private LinToken handleReadValue(LinTagId id) throws IOException {
        return switch (id) {
            case BYTE -> new LinToken.Byte(input.readByte());
            case SHORT -> new LinToken.Short(input.readShort());
            case INT -> new LinToken.Int(input.readInt());
            case LONG -> new LinToken.Long(input.readLong());
            case FLOAT -> new LinToken.Float(input.readFloat());
            case DOUBLE -> new LinToken.Double(input.readDouble());
            case BYTE_ARRAY -> {
                int size = input.readInt();
                stateStack.addLast(new State.ReadByteArray(size));
                yield new LinToken.ByteArrayStart(size);
            }
            case STRING -> new LinToken.String(readUtf());
            case LIST -> {
                var elementId = LinTagId.fromId(input.readUnsignedByte());
                int size = input.readInt();
                stateStack.addLast(new State.ListEntry(size, elementId));
                yield new LinToken.ListStart(size, elementId);
            }
            case COMPOUND -> {
                stateStack.addLast(new State.CompoundEntryName());
                yield new LinToken.CompoundStart();
            }
            case INT_ARRAY -> {
                int size = input.readInt();
                stateStack.addLast(new State.ReadIntArray(size));
                yield new LinToken.IntArrayStart(size);
            }
            case LONG_ARRAY -> {
                int size = input.readInt();
                stateStack.addLast(new State.ReadLongArray(size));
                yield new LinToken.LongArrayStart(size);
            }
            case END -> throw new NbtParseException("Invalid id: " + id);
        };
    }

    private NormalUtf8Decoder getNormalUtf8Decoder() {
        NormalUtf8Decoder decoder = this.decoder;
        if (decoder == null) {
            decoder = new NormalUtf8Decoder();
            this.decoder = decoder;
        }
        return decoder;
    }

    private String readUtf() throws IOException {
        return switch (stringEncoding) {
            case MODIFIED_UTF_8 -> input.readUTF();
            case NORMAL_UTF_8 -> {
                int length = input.readUnsignedShort();
                NormalUtf8Decoder decoder = getNormalUtf8Decoder();
                decoder.fill(input, length);
                yield decoder.decode();
            }
            case UNKNOWN -> {
                int length = input.readUnsignedShort();
                NormalUtf8Decoder decoder = getNormalUtf8Decoder();
                decoder.fill(input, length);
                StringEncoding knownEncoding = getGuaranteedStringEncoding(decoder.sourceBuffer);
                yield switch (knownEncoding) {
                    case MODIFIED_UTF_8 -> {
                        stringEncoding = knownEncoding;
                        byte[] withLength = new byte[length + 2];
                        withLength[0] = (byte) (length >> 8);
                        withLength[1] = (byte) length;
                        System.arraycopy(decoder.sourceBuffer.array(), 0, withLength, 2, length);
                        yield new DataInputStream(new ByteArrayInputStream(withLength)).readUTF();
                    }
                    case NORMAL_UTF_8 -> {
                        stringEncoding = knownEncoding;
                        yield decoder.decode();
                    }
                    // These are valid UTF-8 bytes that fit either encoding. Just read them as normal UTF-8,
                    // but don't change the encoding.
                    case UNKNOWN -> decoder.decode();
                };
            }
        };
    }
}
