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
import org.enginehub.linbus.stream.exception.NbtParseException;
import org.enginehub.linbus.stream.impl.LinNbtReader;
import org.enginehub.linbus.stream.token.LinToken;
import org.jetbrains.annotations.NotNull;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.IntBuffer;
import java.nio.LongBuffer;

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
    public static LinStream read(@NotNull DataInput input) {
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
    public static <R> R readUsing(@NotNull DataInput input, @NotNull IOFunction<? super LinStream, ? extends R> transform)
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
    public static void write(@NotNull DataOutput output, @NotNull LinStreamable tokens) throws IOException {
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
                    throw new NbtParseException("Expected first token to be a name");
                }
            }
            if (token instanceof LinToken.Name name) {
                // We need to hold this until we print the id
                nextName = name.name();
            } else if (token instanceof LinToken.ByteArrayStart byteArrayStart) {
                writeIdAndNameIfNeeded(output, LinTagId.BYTE_ARRAY, nextName);
                nextName = null;

                output.writeInt(byteArrayStart.size().orElseThrow());
            } else if (token instanceof LinToken.ByteArrayContent byteArrayContent) {
                var copy = new byte[byteArrayContent.buffer().remaining()];
                byteArrayContent.buffer().get(copy);
                output.write(copy);
            } else if (token instanceof LinToken.ByteArrayEnd) {
                // Nothing to do
            } else if (token instanceof LinToken.Byte byteValue) {
                writeIdAndNameIfNeeded(output, LinTagId.BYTE, nextName);
                nextName = null;

                output.writeByte(byteValue.value());
            } else if (token instanceof LinToken.CompoundStart) {
                writeIdAndNameIfNeeded(output, LinTagId.COMPOUND, nextName);
                nextName = null;
            } else if (token instanceof LinToken.CompoundEnd) {
                output.writeByte(LinTagId.END.id());
            } else if (token instanceof LinToken.Double doubleValue) {
                writeIdAndNameIfNeeded(output, LinTagId.DOUBLE, nextName);
                nextName = null;

                output.writeDouble(doubleValue.value());
            } else if (token instanceof LinToken.Float floatValue) {
                writeIdAndNameIfNeeded(output, LinTagId.FLOAT, nextName);
                nextName = null;

                output.writeFloat(floatValue.value());
            } else if (token instanceof LinToken.IntArrayStart intArrayStart) {
                writeIdAndNameIfNeeded(output, LinTagId.INT_ARRAY, nextName);
                nextName = null;

                output.writeInt(intArrayStart.size().orElseThrow());
            } else if (token instanceof LinToken.IntArrayContent intArrayContent) {
                IntBuffer buffer = intArrayContent.buffer();
                while (buffer.hasRemaining()) {
                    output.writeInt(buffer.get());
                }
            } else if (token instanceof LinToken.IntArrayEnd) {
                // Nothing to do
            } else if (token instanceof LinToken.Int intValue) {
                writeIdAndNameIfNeeded(output, LinTagId.INT, nextName);
                nextName = null;

                output.writeInt(intValue.value());
            } else if (token instanceof LinToken.ListStart listStart) {
                writeIdAndNameIfNeeded(output, LinTagId.LIST, nextName);
                nextName = null;

                output.writeByte(listStart.elementId().orElseThrow().id());
                output.writeInt(listStart.size().orElseThrow());
            } else if (token instanceof LinToken.ListEnd) {
                // Nothing to do
            } else if (token instanceof LinToken.LongArrayStart longArrayStart) {
                writeIdAndNameIfNeeded(output, LinTagId.LONG_ARRAY, nextName);
                nextName = null;

                output.writeInt(longArrayStart.size().orElseThrow());
            } else if (token instanceof LinToken.LongArrayContent longArrayContent) {
                LongBuffer buffer = longArrayContent.buffer();
                while (buffer.hasRemaining()) {
                    output.writeLong(buffer.get());
                }
            } else if (token instanceof LinToken.LongArrayEnd) {
                // Nothing to do
            } else if (token instanceof LinToken.Long longValue) {
                writeIdAndNameIfNeeded(output, LinTagId.LONG, nextName);
                nextName = null;

                output.writeLong(longValue.value());
            } else if (token instanceof LinToken.Short shortValue) {
                writeIdAndNameIfNeeded(output, LinTagId.SHORT, nextName);
                nextName = null;

                output.writeShort(shortValue.value());
            } else if (token instanceof LinToken.String stringValue) {
                writeIdAndNameIfNeeded(output, LinTagId.STRING, nextName);
                nextName = null;

                output.writeUTF(stringValue.value());
            } else {
                // switch patterns wen
                throw new IllegalArgumentException("Unknown token: " + token);
            }
        }
    }

    private static void writeIdAndNameIfNeeded(DataOutput output, LinTagId id, String name) throws IOException {
        if (name != null) {
            output.writeByte(id.id());
            output.writeUTF(name);
        }
    }

    private LinBinaryIO() {
    }
}
