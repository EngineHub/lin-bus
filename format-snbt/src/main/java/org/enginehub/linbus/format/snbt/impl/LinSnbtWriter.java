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

import org.enginehub.linbus.format.snbt.LinStringIO;
import org.enginehub.linbus.stream.LinStream;
import org.enginehub.linbus.stream.LinStreamable;
import org.enginehub.linbus.stream.token.LinToken;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.util.ArrayDeque;

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
     * Write the tokens to the output.
     *
     * @param output the output
     * @param tokens the tokens
     * @throws IOException if an I/O error occurs
     */
    public void write(@NotNull Appendable output, @NotNull LinStream tokens) throws IOException {
        while (true) {
            var state = stateStack.peekLast();
            LinToken token = tokens.nextOrNull();
            if (token == null) {
                break;
            }
            if (token instanceof LinToken.Name name) {
                if (!(state instanceof WriteState.Compound compound)) {
                    throw new IllegalStateException("Names can only appear inside compounds");
                }
                if (compound.hasPrevious) {
                    output.append(',');
                    // Kill the previous flag
                    replaceLast(new WriteState.Compound(false));
                }
                output.append(Elusion.escapeIfNeeded(name.name())).append(':');
            } else if (token instanceof LinToken.ByteArrayStart byteArrayStart) {
                output.append("[B;");
            } else if (token instanceof LinToken.ByteArrayContent byteArrayContent) {
                if (state instanceof WriteState.WritingArray) {
                    output.append(',');
                } else {
                    stateStack.addLast(new WriteState.WritingArray());
                }
                ByteBuffer buffer = byteArrayContent.buffer();
                while (buffer.hasRemaining()) {
                    output.append(String.valueOf(buffer.get())).append('B');
                    if (buffer.hasRemaining()) {
                        output.append(',');
                    }
                }
            } else if (token instanceof LinToken.ByteArrayEnd) {
                if (state instanceof WriteState.WritingArray) {
                    stateStack.removeLast();
                }
                output.append(']');

                handleValueEnd(output);
            } else if (token instanceof LinToken.Byte byteValue) {
                output.append(String.valueOf(byteValue.value())).append('B');

                handleValueEnd(output);
            } else if (token instanceof LinToken.CompoundStart) {
                output.append('{');

                stateStack.addLast(new WriteState.Compound(false));
            } else if (token instanceof LinToken.CompoundEnd) {
                output.append('}');

                stateStack.removeLast();
                handleValueEnd(output);
            } else if (token instanceof LinToken.Double doubleValue) {
                output.append(String.valueOf(doubleValue.value())).append('D');

                handleValueEnd(output);
            } else if (token instanceof LinToken.Float floatValue) {
                output.append(String.valueOf(floatValue.value())).append('F');

                handleValueEnd(output);
            } else if (token instanceof LinToken.IntArrayStart intArrayStart) {
                output.append("[I;");
            } else if (token instanceof LinToken.IntArrayContent intArrayContent) {
                if (state instanceof WriteState.WritingArray) {
                    output.append(',');
                } else {
                    stateStack.addLast(new WriteState.WritingArray());
                }
                IntBuffer buffer = intArrayContent.buffer();
                while (buffer.hasRemaining()) {
                    output.append(String.valueOf(buffer.get()));
                    if (buffer.hasRemaining()) {
                        output.append(',');
                    }
                }
            } else if (token instanceof LinToken.IntArrayEnd) {
                if (state instanceof WriteState.WritingArray) {
                    stateStack.removeLast();
                }
                output.append(']');

                handleValueEnd(output);
            } else if (token instanceof LinToken.Int intValue) {
                output.append(String.valueOf(intValue.value()));

                handleValueEnd(output);
            } else if (token instanceof LinToken.ListStart listStart) {
                output.append('[');

                stateStack.addLast(new WriteState.List(listStart.size().orElseThrow()));
            } else if (token instanceof LinToken.ListEnd) {
                output.append(']');

                stateStack.removeLast();
                handleValueEnd(output);
            } else if (token instanceof LinToken.LongArrayStart longArrayStart) {
                output.append("[L;");
            } else if (token instanceof LinToken.LongArrayContent longArrayContent) {
                if (state instanceof WriteState.WritingArray) {
                    output.append(',');
                } else {
                    stateStack.addLast(new WriteState.WritingArray());
                }
                LongBuffer buffer = longArrayContent.buffer();
                while (buffer.hasRemaining()) {
                    output.append(String.valueOf(buffer.get())).append('L');
                    if (buffer.hasRemaining()) {
                        output.append(',');
                    }
                }
            } else if (token instanceof LinToken.LongArrayEnd) {
                if (state instanceof WriteState.WritingArray) {
                    stateStack.removeLast();
                }
                output.append(']');

                handleValueEnd(output);
            } else if (token instanceof LinToken.Long longValue) {
                output.append(String.valueOf(longValue.value())).append('L');

                handleValueEnd(output);
            } else if (token instanceof LinToken.Short shortValue) {
                output.append(String.valueOf(shortValue.value())).append('S');

                handleValueEnd(output);
            } else if (token instanceof LinToken.String stringValue) {
                output.append(Elusion.escapeIfNeeded(stringValue.value()));

                handleValueEnd(output);
            } else {
                // switch patterns wen
                throw new IllegalArgumentException("Unknown token: " + token);
            }
        }
    }

    private void handleValueEnd(Appendable output) throws IOException {
        var state = stateStack.pollLast();
        if (state == null) {
            return;
        }
        if (state instanceof WriteState.List list) {
            int remainingValues = list.remainingValues - 1;
            stateStack.addLast(new WriteState.List(remainingValues));
            if (remainingValues > 0) {
                output.append(',');
            }
        } else if (state instanceof WriteState.Compound) {
            stateStack.addLast(new WriteState.Compound(true));
        } else {
            throw new IllegalStateException("Unexpected state: " + state);
        }
    }

    private void replaceLast(WriteState state) {
        stateStack.removeLast();
        stateStack.addLast(state);
    }
}
