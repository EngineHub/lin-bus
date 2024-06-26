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

import org.enginehub.linbus.common.LinTagId;
import org.enginehub.linbus.format.snbt.LinStringIO;
import org.enginehub.linbus.stream.LinStream;
import org.enginehub.linbus.stream.LinStreamable;
import org.enginehub.linbus.stream.exception.NbtWriteException;
import org.enginehub.linbus.stream.token.LinToken;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.util.ArrayDeque;
import java.util.Optional;
import java.util.OptionalInt;

/**
 * Implementation of {@link LinStringIO#write(Appendable, LinStreamable)}.
 */
public class LinSnbtWriter {
    private sealed interface WriteState {
        record List(int remainingValues) implements WriteState {
        }

        record Compound(boolean hasPrevious) implements WriteState {
        }

        record WritingArray() implements WriteState {
        }
    }

    private final ArrayDeque<WriteState> stateStack = new ArrayDeque<>();

    /**
     * Construct a new writer.
     */
    public LinSnbtWriter() {
    }

    /**
     * Write the tokens to the output.
     *
     * @param output the output
     * @param tokens the tokens
     * @throws IOException if an I/O error occurs
     */
    public void write(Appendable output, LinStream tokens) throws IOException {
        while (true) {
            var state = stateStack.peekLast();
            LinToken token = tokens.nextOrNull();
            if (token == null) {
                break;
            }
            switch (token) {
                case LinToken.Name(String name, Optional<LinTagId> id) -> {
                    if (!(state instanceof WriteState.Compound compound)) {
                        throw new NbtWriteException("Names can only appear inside compounds");
                    }
                    if (compound.hasPrevious) {
                        output.append(',');
                        // Kill the previous flag
                        replaceLast(new WriteState.Compound(false));
                    }
                    output.append(Elusion.escapeIfNeeded(name)).append(':');
                }
                case LinToken.ByteArrayStart byteArrayStart -> output.append("[B;");
                case LinToken.ByteArrayContent(ByteBuffer buffer) -> {
                    if (state instanceof WriteState.WritingArray) {
                        output.append(',');
                    } else {
                        stateStack.addLast(new WriteState.WritingArray());
                    }
                    while (buffer.hasRemaining()) {
                        output.append(String.valueOf(buffer.get())).append('B');
                        if (buffer.hasRemaining()) {
                            output.append(',');
                        }
                    }
                }
                case LinToken.ByteArrayEnd byteArrayEnd -> {
                    if (state instanceof WriteState.WritingArray) {
                        stateStack.removeLast();
                    }
                    output.append(']');

                    handleValueEnd(output);
                }
                case LinToken.Byte(byte value) -> {
                    output.append(String.valueOf(value)).append('B');

                    handleValueEnd(output);
                }
                case LinToken.CompoundStart compoundStart -> {
                    output.append('{');

                    stateStack.addLast(new WriteState.Compound(false));
                }
                case LinToken.CompoundEnd compoundEnd -> {
                    output.append('}');

                    stateStack.removeLast();
                    handleValueEnd(output);
                }
                case LinToken.Double(double value) -> {
                    output.append(String.valueOf(value)).append('D');

                    handleValueEnd(output);
                }
                case LinToken.Float(float value) -> {
                    output.append(String.valueOf(value)).append('F');

                    handleValueEnd(output);
                }
                case LinToken.IntArrayStart intArrayStart -> output.append("[I;");
                case LinToken.IntArrayContent(IntBuffer buffer) -> {
                    if (state instanceof WriteState.WritingArray) {
                        output.append(',');
                    } else {
                        stateStack.addLast(new WriteState.WritingArray());
                    }
                    while (buffer.hasRemaining()) {
                        output.append(String.valueOf(buffer.get()));
                        if (buffer.hasRemaining()) {
                            output.append(',');
                        }
                    }
                }
                case LinToken.IntArrayEnd intArrayEnd -> {
                    if (state instanceof WriteState.WritingArray) {
                        stateStack.removeLast();
                    }
                    output.append(']');

                    handleValueEnd(output);
                }
                case LinToken.Int(int value) -> {
                    output.append(String.valueOf(value));

                    handleValueEnd(output);
                }
                case LinToken.ListStart(OptionalInt size, Optional<LinTagId> elementId) -> {
                    output.append('[');

                    stateStack.addLast(new WriteState.List(size.orElseThrow()));
                }
                case LinToken.ListEnd listEnd -> {
                    output.append(']');

                    stateStack.removeLast();
                    handleValueEnd(output);
                }
                case LinToken.LongArrayStart longArrayStart -> output.append("[L;");
                case LinToken.LongArrayContent(LongBuffer buffer) -> {
                    if (state instanceof WriteState.WritingArray) {
                        output.append(',');
                    } else {
                        stateStack.addLast(new WriteState.WritingArray());
                    }
                    while (buffer.hasRemaining()) {
                        output.append(String.valueOf(buffer.get())).append('L');
                        if (buffer.hasRemaining()) {
                            output.append(',');
                        }
                    }
                }
                case LinToken.LongArrayEnd longArrayEnd -> {
                    if (state instanceof WriteState.WritingArray) {
                        stateStack.removeLast();
                    }
                    output.append(']');

                    handleValueEnd(output);
                }
                case LinToken.Long(long value) -> {
                    output.append(String.valueOf(value)).append('L');

                    handleValueEnd(output);
                }
                case LinToken.Short(short value) -> {
                    output.append(String.valueOf(value)).append('S');

                    handleValueEnd(output);
                }
                case LinToken.String(String value) -> {
                    output.append(Elusion.escapeIfNeeded(value));

                    handleValueEnd(output);
                }
                default -> throw new NbtWriteException("Unknown token: " + token);
            }
        }
    }

    private void handleValueEnd(Appendable output) throws IOException {
        var state = stateStack.pollLast();
        switch (state) {
            case null -> {
            }
            case WriteState.List(int remainingValues) -> {
                stateStack.addLast(new WriteState.List(remainingValues - 1));
                if (remainingValues - 1 > 0) {
                    output.append(',');
                }
            }
            case WriteState.Compound compound -> stateStack.addLast(new WriteState.Compound(true));
            default -> throw new NbtWriteException("Unexpected state: " + state);
        }
    }

    private void replaceLast(WriteState state) {
        stateStack.removeLast();
        stateStack.addLast(state);
    }
}
