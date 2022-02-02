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

package org.enginehub.linbus.stream.token;

import org.enginehub.linbus.common.LinTagId;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.util.Optional;
import java.util.OptionalInt;

/**
 * A token from an NBT stream.
 */
public sealed interface LinToken {
    /**
     * Check if this token represents a whole value on its own, i.e. there are no following tokens.
     *
     * @return {@code true} if this token represents a whole value on its own
     */
    boolean isSimpleValue();

    /**
     * If possible, return the {@link LinTagId} that best represents this token.
     *
     * <p>
     * For example, a name token will return nothing. Only the array start tokens will return {@link
     * LinTagId#BYTE_ARRAY}.
     * </p>
     *
     * @return the {@link LinTagId} that best represents this token, or nothing if this token cannot be represented
     */
    default Optional<LinTagId> tagId() {
        return Optional.empty();
    }

    /**
     * Represents compound tag names.
     *
     * @param name the name of the next tag
     * @param id the id of the next tag
     */
    record Name(java.lang.String name, Optional<LinTagId> id) implements LinToken {
        public Name(java.lang.String name) {
            this(name, Optional.empty());
        }

        public Name(java.lang.String name, LinTagId id) {
            this(name, Optional.of(id));
        }

        @Override
        public boolean isSimpleValue() {
            return false;
        }
    }

    /**
     * Represents the start of a byte array.
     *
     * @param size the size of the array
     */
    record ByteArrayStart(OptionalInt size) implements LinToken {
        public ByteArrayStart() {
            this(OptionalInt.empty());
        }

        public ByteArrayStart(int size) {
            this(OptionalInt.of(size));
        }

        @Override
        public boolean isSimpleValue() {
            return false;
        }

        @Override
        public Optional<LinTagId> tagId() {
            return Optional.of(LinTagId.BYTE_ARRAY);
        }
    }

    /**
     * Represents a portion of a byte array.
     *
     * <p>
     * The buffer will always be read-only.
     * </p>
     *
     * @param buffer the buffer containing the data
     */
    record ByteArrayContent(ByteBuffer buffer) implements LinToken {
        /**
         * Creates a new {@link LinToken.ByteArrayContent} with the given buffer.
         *
         * @param buffer the buffer containing the data
         */
        public ByteArrayContent {
            if (!buffer.isReadOnly()) {
                throw new IllegalArgumentException("buffer must be read-only");
            }
        }

        @Override
        public boolean isSimpleValue() {
            return false;
        }
    }

    /**
     * Represents the end of a byte array.
     */
    record ByteArrayEnd() implements LinToken {
        @Override
        public boolean isSimpleValue() {
            return false;
        }
    }

    /**
     * A single byte.
     *
     * @param value the value of the byte
     */
    record Byte(byte value) implements LinToken {
        @Override
        public boolean isSimpleValue() {
            return true;
        }

        @Override
        public Optional<LinTagId> tagId() {
            return Optional.of(LinTagId.BYTE);
        }
    }

    /**
     * Represents the start of a compound tag.
     *
     * <p>
     * Until a {@link CompoundEnd} is encountered, the stream will contain key-value pairs: one {@link Name} and one or
     * more {@link LinToken LinTokens} representing the value.
     * </p>
     */
    record CompoundStart() implements LinToken {
        @Override
        public boolean isSimpleValue() {
            return false;
        }

        @Override
        public Optional<LinTagId> tagId() {
            return Optional.of(LinTagId.COMPOUND);
        }
    }

    /**
     * Represents the end of a compound tag.
     */
    record CompoundEnd() implements LinToken {
        @Override
        public boolean isSimpleValue() {
            return false;
        }
    }

    /**
     * A single double.
     *
     * @param value the value of the double
     */
    record Double(double value) implements LinToken {
        @Override
        public boolean isSimpleValue() {
            return true;
        }

        @Override
        public Optional<LinTagId> tagId() {
            return Optional.of(LinTagId.DOUBLE);
        }
    }

    /**
     * A single float.
     *
     * @param value the value of the float
     */
    record Float(float value) implements LinToken {
        @Override
        public boolean isSimpleValue() {
            return true;
        }

        @Override
        public Optional<LinTagId> tagId() {
            return Optional.of(LinTagId.FLOAT);
        }
    }

    /**
     * Represents the start of an int array.
     *
     * @param size the size of the array
     */
    record IntArrayStart(OptionalInt size) implements LinToken {
        public IntArrayStart() {
            this(OptionalInt.empty());
        }

        public IntArrayStart(int size) {
            this(OptionalInt.of(size));
        }

        @Override
        public boolean isSimpleValue() {
            return false;
        }

        @Override
        public Optional<LinTagId> tagId() {
            return Optional.of(LinTagId.INT_ARRAY);
        }
    }

    /**
     * Represents a portion of an int array.
     *
     * <p>
     * The buffer will always be read-only.
     * </p>
     *
     * @param buffer the buffer containing the data
     */
    record IntArrayContent(IntBuffer buffer) implements LinToken {
        /**
         * Creates a new {@link LinToken.IntArrayContent} with the given buffer.
         *
         * @param buffer the buffer containing the data
         */
        public IntArrayContent {
            if (!buffer.isReadOnly()) {
                throw new IllegalArgumentException("buffer must be read-only");
            }
        }

        @Override
        public boolean isSimpleValue() {
            return false;
        }
    }

    /**
     * Represents the end of an int array.
     */
    record IntArrayEnd() implements LinToken {
        @Override
        public boolean isSimpleValue() {
            return false;
        }
    }

    /**
     * A single int.
     *
     * @param value the value of the int
     */
    record Int(int value) implements LinToken {
        @Override
        public boolean isSimpleValue() {
            return true;
        }

        @Override
        public Optional<LinTagId> tagId() {
            return Optional.of(LinTagId.INT);
        }
    }

    /**
     * Represents the start of a list.
     *
     * <p>
     * Until a {@link ListEnd} is encountered, the stream will contain one or more {@link LinToken LinTokens}
     * representing the values.
     * </p>
     *
     * @param size the size of the list, if known
     * @param elementId the type of the elements in the list, if known
     */
    record ListStart(OptionalInt size, Optional<LinTagId> elementId) implements LinToken {
        public ListStart() {
            this(OptionalInt.empty(), Optional.empty());
        }

        public ListStart(int size, LinTagId elementId) {
            this(OptionalInt.of(size), Optional.of(elementId));
        }

        @Override
        public boolean isSimpleValue() {
            return false;
        }

        @Override
        public Optional<LinTagId> tagId() {
            return Optional.of(LinTagId.LIST);
        }
    }

    /**
     * Represents the end of a list.
     */
    record ListEnd() implements LinToken {
        @Override
        public boolean isSimpleValue() {
            return false;
        }
    }

    /**
     * Represents the start of a long array.
     *
     * @param size the size of the array
     */
    record LongArrayStart(OptionalInt size) implements LinToken {
        public LongArrayStart() {
            this(OptionalInt.empty());
        }

        public LongArrayStart(int size) {
            this(OptionalInt.of(size));
        }

        @Override
        public boolean isSimpleValue() {
            return false;
        }

        @Override
        public Optional<LinTagId> tagId() {
            return Optional.of(LinTagId.LONG_ARRAY);
        }
    }

    /**
     * Represents a portion of a long array.
     *
     * <p>
     * The buffer will always be read-only.
     * </p>
     *
     * @param buffer the buffer containing the data
     */
    record LongArrayContent(LongBuffer buffer) implements LinToken {
        /**
         * Creates a new {@link LongArrayContent} with the given buffer.
         *
         * @param buffer the buffer containing the data
         */
        public LongArrayContent {
            if (!buffer.isReadOnly()) {
                throw new IllegalArgumentException("buffer must be read-only");
            }
        }

        @Override
        public boolean isSimpleValue() {
            return false;
        }
    }

    /**
     * Represents the end of a long array.
     */
    record LongArrayEnd() implements LinToken {
        @Override
        public boolean isSimpleValue() {
            return false;
        }
    }

    /**
     * A single long.
     *
     * @param value the value of the long
     */
    record Long(long value) implements LinToken {
        @Override
        public boolean isSimpleValue() {
            return true;
        }

        @Override
        public Optional<LinTagId> tagId() {
            return Optional.of(LinTagId.LONG);
        }
    }

    /**
     * A single short.
     *
     * @param value the value of the short
     */
    record Short(short value) implements LinToken {
        @Override
        public boolean isSimpleValue() {
            return true;
        }

        @Override
        public Optional<LinTagId> tagId() {
            return Optional.of(LinTagId.SHORT);
        }
    }

    /**
     * A single string.
     *
     * @param value the value of the string
     */
    record String(java.lang.String value) implements LinToken {
        @Override
        public boolean isSimpleValue() {
            return true;
        }

        @Override
        public Optional<LinTagId> tagId() {
            return Optional.of(LinTagId.STRING);
        }
    }
}
