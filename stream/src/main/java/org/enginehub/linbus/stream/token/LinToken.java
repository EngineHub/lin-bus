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
import org.enginehub.linbus.common.internal.EmptyRecordShim;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;

/**
 * A token from an NBT stream.
 */
public sealed interface LinToken {
    /**
     * Represents compound tag names.
     *
     * @param name the name of the next tag
     * @param id the id of the next tag
     */
    record Name(java.lang.String name, LinTagId id) implements LinToken {
    }

    /**
     * Represents the start of a byte array.
     *
     * @param size the size of the array
     */
    record ByteArrayStart(int size) implements LinToken {
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
    }

    /**
     * Represents the end of a byte array. This could be inferred from the content, but this is consistent.
     */
    final class ByteArrayEnd extends EmptyRecordShim implements LinToken {
    }

    /**
     * A single byte.
     *
     * @param value the value of the byte
     */
    record Byte(byte value) implements LinToken {
    }

    /**
     * Represents the start of a compound tag.
     *
     * <p>
     * Until a {@link CompoundEnd} is encountered, the stream will contain key-value pairs: one {@link Name} and one or
     * more {@link LinToken LinTokens} representing the value.
     * </p>
     */
    final class CompoundStart extends EmptyRecordShim implements LinToken {
    }

    /**
     * Represents the end of a compound tag.
     */
    final class CompoundEnd extends EmptyRecordShim implements LinToken {
    }

    /**
     * A single double.
     *
     * @param value the value of the double
     */
    record Double(double value) implements LinToken {
    }

    /**
     * A single float.
     *
     * @param value the value of the float
     */
    record Float(float value) implements LinToken {
    }

    /**
     * Represents the start of an int array.
     *
     * @param size the size of the array
     */
    record IntArrayStart(int size) implements LinToken {
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
    }

    /**
     * Represents the end of an int array. This could be inferred from the content, but this is consistent.
     */
    final class IntArrayEnd extends EmptyRecordShim implements LinToken {
    }

    /**
     * A single int.
     *
     * @param value the value of the int
     */
    record Int(int value) implements LinToken {
    }

    /**
     * Represents the start of a list.
     *
     * <p>
     * Until a {@link ListEnd} is encountered, the stream will contain one or more {@link LinToken LinTokens}
     * representing the values.
     * </p>
     *
     * @param size the size of the list
     * @param elementId the type of the elements in the list
     */
    record ListStart(int size, LinTagId elementId) implements LinToken {
    }

    /**
     * Represents the end of a list. This could be inferred from the size, but this is consistent.
     */
    final class ListEnd extends EmptyRecordShim implements LinToken {
    }

    /**
     * Represents the start of a long array.
     *
     * @param size the size of the array
     */
    record LongArrayStart(int size) implements LinToken {
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
    }

    /**
     * Represents the end of a long array. This could be inferred from the content, but this is consistent.
     */
    final class LongArrayEnd extends EmptyRecordShim implements LinToken {
    }

    /**
     * A single long.
     *
     * @param value the value of the long
     */
    record Long(long value) implements LinToken {
    }

    /**
     * A single short.
     *
     * @param value the value of the short
     */
    record Short(short value) implements LinToken {
    }

    /**
     * A single string.
     *
     * @param value the value of the string
     */
    record String(java.lang.String value) implements LinToken {
    }
}
