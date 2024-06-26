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

import org.enginehub.linbus.common.IOFunction;
import org.enginehub.linbus.common.LinTagId;
import org.enginehub.linbus.stream.exception.NbtWriteException;
import org.enginehub.linbus.stream.impl.LinNbtReader;
import org.enginehub.linbus.stream.token.LinToken;
import org.jspecify.annotations.Nullable;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.util.Optional;
import java.util.OptionalInt;

/**
 * Reads and writes NBT streams.
 */
public class LinBinaryIO {
    /**
     * Read a stream of NBT tokens from a {@link DataInput}.
     *
     * <p>
     * The input will not be closed by the iterator. The caller is responsible for managing the lifetime of the input.
     * </p>
     *
     * @param input the input to read from
     * @return the stream of NBT tokens
     */
    public static LinStream read(DataInput input) {
        return new LinNbtReader(input);
    }

    /**
     * Read a result using a stream of NBT tokens from a {@link DataInput}.
     *
     * <p>
     * The input will not be closed by this method. The caller is responsible for managing the lifetime of the input.
     * </p>
     *
     * @param input the input to read from
     * @param transform the function to transform the stream of NBT tokens into the result
     * @param <R> the type of the result
     * @return the result
     * @throws IOException if an I/O error occurs ({@link UncheckedIOException} is unwrapped)
     */
    public static <R extends @Nullable Object> R readUsing(DataInput input, IOFunction<? super LinStream, ? extends R> transform)
        throws IOException {
        return transform.apply(read(input));
    }

    /**
     * Write a stream of NBT tokens to a {@link DataOutput}.
     *
     * <p>
     * If optional information is not available, it will be calculated. See {@link LinStream#calculateOptionalInfo()}
     * for details on what that means for memory and speed.
     * </p>
     *
     * <p>
     * The output will not be closed by this method. The caller is responsible for managing the lifetime of the output.
     * </p>
     *
     * @param output the output to write to
     * @param tokens the stream of NBT tokens
     * @throws IOException if an I/O error occurs
     */
    public static void write(DataOutput output, LinStreamable tokens) throws IOException {
        // This is essentially free if the info is already there, so we can just do it.
        LinStream tokenStream = tokens.linStream().calculateOptionalInfo();
        boolean seenFirstName = false;
        // This also signals if we're in a compound tag or not.
        String nextName = null;
        LinToken token;
        while ((token = tokenStream.nextOrNull()) != null) {
            if (!seenFirstName) {
                if (token instanceof LinToken.Name) {
                    seenFirstName = true;
                } else {
                    // It's not legal to write without a name.
                    throw new NbtWriteException("Expected first token to be a name");
                }
            }
            switch (token) {
                case LinToken.Name(String name, Optional<LinTagId> id) ->
                    // We need to hold this until we print the id
                    nextName = name;
                case LinToken.ByteArrayStart(OptionalInt size) -> {
                    writeIdAndNameIfNeeded(output, LinTagId.BYTE_ARRAY, nextName);
                    nextName = null;

                    output.writeInt(size.orElseThrow());
                }
                case LinToken.ByteArrayContent(ByteBuffer buffer) -> {
                    var copy = new byte[buffer.remaining()];
                    buffer.get(copy);
                    output.write(copy);
                }
                case LinToken.ByteArrayEnd byteArrayEnd -> {
                    // Nothing to do
                }
                case LinToken.Byte(byte value) -> {
                    writeIdAndNameIfNeeded(output, LinTagId.BYTE, nextName);
                    nextName = null;

                    output.writeByte(value);
                }
                case LinToken.CompoundStart compoundStart -> {
                    writeIdAndNameIfNeeded(output, LinTagId.COMPOUND, nextName);
                    nextName = null;
                }
                case LinToken.CompoundEnd compoundEnd -> output.writeByte(LinTagId.END.id());
                case LinToken.Double(double value) -> {
                    writeIdAndNameIfNeeded(output, LinTagId.DOUBLE, nextName);
                    nextName = null;

                    output.writeDouble(value);
                }
                case LinToken.Float(float value) -> {
                    writeIdAndNameIfNeeded(output, LinTagId.FLOAT, nextName);
                    nextName = null;

                    output.writeFloat(value);
                }
                case LinToken.IntArrayStart(OptionalInt size) -> {
                    writeIdAndNameIfNeeded(output, LinTagId.INT_ARRAY, nextName);
                    nextName = null;

                    output.writeInt(size.orElseThrow());
                }
                case LinToken.IntArrayContent(IntBuffer buffer) -> {
                    while (buffer.hasRemaining()) {
                        output.writeInt(buffer.get());
                    }
                }
                case LinToken.IntArrayEnd intArrayEnd -> {
                    // Nothing to do
                }
                case LinToken.Int(int value) -> {
                    writeIdAndNameIfNeeded(output, LinTagId.INT, nextName);
                    nextName = null;

                    output.writeInt(value);
                }
                case LinToken.ListStart(OptionalInt size, Optional<LinTagId> elementId) -> {
                    writeIdAndNameIfNeeded(output, LinTagId.LIST, nextName);
                    nextName = null;

                    output.writeByte(elementId.orElseThrow().id());
                    output.writeInt(size.orElseThrow());
                }
                case LinToken.ListEnd listEnd -> {
                    // Nothing to do
                }
                case LinToken.LongArrayStart(OptionalInt size) -> {
                    writeIdAndNameIfNeeded(output, LinTagId.LONG_ARRAY, nextName);
                    nextName = null;

                    output.writeInt(size.orElseThrow());
                }
                case LinToken.LongArrayContent(LongBuffer buffer) -> {
                    while (buffer.hasRemaining()) {
                        output.writeLong(buffer.get());
                    }
                }
                case LinToken.LongArrayEnd longArrayEnd -> {
                    // Nothing to do
                }
                case LinToken.Long(long value) -> {
                    writeIdAndNameIfNeeded(output, LinTagId.LONG, nextName);
                    nextName = null;

                    output.writeLong(value);
                }
                case LinToken.Short(short value) -> {
                    writeIdAndNameIfNeeded(output, LinTagId.SHORT, nextName);
                    nextName = null;

                    output.writeShort(value);
                }
                case LinToken.String(String value) -> {
                    writeIdAndNameIfNeeded(output, LinTagId.STRING, nextName);
                    nextName = null;

                    output.writeUTF(value);
                }
                default -> throw new NbtWriteException("Unknown token: " + token);
            }
        }
    }

    private static void writeIdAndNameIfNeeded(DataOutput output, LinTagId id, @Nullable String name) throws IOException {
        if (name != null) {
            output.writeByte(id.id());
            output.writeUTF(name);
        }
    }

    private LinBinaryIO() {
    }
}
