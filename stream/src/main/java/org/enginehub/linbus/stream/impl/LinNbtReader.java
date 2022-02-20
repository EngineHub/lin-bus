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
import org.enginehub.linbus.stream.LinStream;
import org.enginehub.linbus.stream.token.LinToken;
import org.jetbrains.annotations.Nullable;

import java.io.DataInput;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

/**
 * Reads a stream of tokens from a {@link DataInput}.
 */
public class LinNbtReader implements LinStream {
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

    private final DataInput input;
    /**
     * The state stack. We're currently on the one that's LAST.
     */
    private final Deque<State> stateStack;

    /**
     * Creates a new reader.
     *
     * @param input the input to read from
     */
    public LinNbtReader(DataInput input) {
        this.input = input;
        this.stateStack = new ArrayDeque<>(List.of(new State.Initial()));
    }

    @Override
    public @Nullable LinToken nextOrNull() throws IOException {
        var state = stateStack.pollLast();
        if (state == null) {
            return null;
        }
        if (state instanceof State.ListEntry entry) {
            if (entry.remaining == 0) {
                return new LinToken.ListEnd();
            }
            stateStack.addLast(new State.ListEntry(entry.remaining - 1, entry.elementId));
            state = new State.ReadValue(entry.elementId);
        }
        if (state instanceof State.Initial) {
            if (input.readUnsignedByte() != LinTagId.COMPOUND.id()) {
                throw new IllegalStateException("NBT stream does not start with a compound tag");
            }
            stateStack.addLast(new State.CompoundStart());
            return new LinToken.Name(input.readUTF(), LinTagId.COMPOUND);
        } else if (state instanceof State.CompoundStart) {
            stateStack.addLast(new State.CompoundEntryName());
            return new LinToken.CompoundStart();
        } else if (state instanceof State.CompoundEntryName) {
            var id = LinTagId.fromId(input.readUnsignedByte());
            if (id == LinTagId.END) {
                return new LinToken.CompoundEnd();
            }

            // After we read the value, we'll be back at reading the name.
            stateStack.addLast(new State.CompoundEntryName());
            stateStack.addLast(new State.ReadValue(id));
            return new LinToken.Name(input.readUTF(), id);
        } else if (state instanceof State.ReadValue rv) {
            return switch (rv.id()) {
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
                case STRING -> new LinToken.String(input.readUTF());
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
                case END -> throw new IllegalStateException("Invalid id: " + rv.id());
            };
        } else if (state instanceof State.ReadByteArray rba) {
            if (rba.remaining == 0) {
                // We're done reading the array. Return the end token.
                // This will also implicitly return to the state in the stack below the array.
                return new LinToken.ByteArrayEnd();
            }
            ByteBuffer buffer = ByteBuffer.allocate(Math.min(8192, rba.remaining));
            input.readFully(buffer.array(), buffer.position(), buffer.remaining());
            stateStack.addLast(new State.ReadByteArray(rba.remaining - buffer.remaining()));
            return new LinToken.ByteArrayContent(buffer.asReadOnlyBuffer());
        } else if (state instanceof State.ReadIntArray ria) {
            if (ria.remaining == 0) {
                // We're done reading the array. Return the end token.
                // This will also implicitly return to the state in the stack below the array.
                return new LinToken.IntArrayEnd();
            }
            ByteBuffer buffer = ByteBuffer.allocate(Math.min(8192, ria.remaining * 4));
            input.readFully(buffer.array(), buffer.position(), buffer.remaining());
            stateStack.addLast(new State.ReadIntArray(ria.remaining - buffer.remaining() / 4));
            return new LinToken.IntArrayContent(buffer.asIntBuffer().asReadOnlyBuffer());
        } else if (state instanceof State.ReadLongArray rla) {
            if (rla.remaining == 0) {
                // We're done reading the array. Return the end token.
                // This will also implicitly return to the state in the stack below the array.
                return new LinToken.LongArrayEnd();
            }
            ByteBuffer buffer = ByteBuffer.allocate(Math.min(8192, rla.remaining * 8));
            input.readFully(buffer.array(), buffer.position(), buffer.remaining());
            stateStack.addLast(new State.ReadLongArray(rla.remaining - buffer.remaining() / 8));
            return new LinToken.LongArrayContent(buffer.asLongBuffer().asReadOnlyBuffer());
        }
        throw new AssertionError("Missing state handler (switch patterns wen)");
    }
}
