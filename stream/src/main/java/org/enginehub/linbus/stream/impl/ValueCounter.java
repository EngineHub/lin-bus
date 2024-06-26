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

import org.enginehub.linbus.stream.exception.NbtParseException;
import org.enginehub.linbus.stream.token.LinToken;

/**
 * Counts the number of values that have been read. Handles nesting and arrays.
 */
public class ValueCounter {
    private static final byte BYTE_ARRAY = 1;
    private static final byte INT_ARRAY = 2;
    private static final byte LONG_ARRAY = 3;

    private int count;
    private int compounds;
    private int lists;
    private byte arrayType;

    /**
     * Construct a new value counter.
     */
    public ValueCounter() {
    }

    /**
     * Add a token to this counter.
     *
     * @param token the token to add
     */
    public void add(LinToken token) {
        if (token.isSimpleValue() && !isNested()) {
            count++;
            return;
        }
        if (token instanceof LinToken.CompoundStart) {
            compounds++;
        } else if (token instanceof LinToken.CompoundEnd) {
            compounds--;
            if (compounds < 0) {
                throw new NbtParseException("Compound end without start");
            }
            if (!isNested()) {
                count++;
            }
        } else if (token instanceof LinToken.ListStart) {
            lists++;
        } else if (token instanceof LinToken.ListEnd) {
            lists--;
            if (lists < 0) {
                throw new NbtParseException("List end without start");
            }
            if (!isNested()) {
                count++;
            }
        } else if (token instanceof LinToken.ByteArrayStart) {
            arrayType = BYTE_ARRAY;
        } else if (token instanceof LinToken.ByteArrayEnd) {
            if (arrayType != BYTE_ARRAY) {
                throw new NbtParseException("Byte array end without start");
            }
            arrayType = 0;
            if (!isNested()) {
                count++;
            }
        } else if (token instanceof LinToken.IntArrayStart) {
            arrayType = INT_ARRAY;
        } else if (token instanceof LinToken.IntArrayEnd) {
            if (arrayType != INT_ARRAY) {
                throw new NbtParseException("Int array end without start");
            }
            arrayType = 0;
            if (!isNested()) {
                count++;
            }
        } else if (token instanceof LinToken.LongArrayStart) {
            arrayType = LONG_ARRAY;
        } else if (token instanceof LinToken.LongArrayEnd) {
            if (arrayType != LONG_ARRAY) {
                throw new NbtParseException("Long array end without start");
            }
            arrayType = 0;
            if (!isNested()) {
                count++;
            }
        }
    }

    /**
     * {@return the current count}
     */
    public int count() {
        return count;
    }

    /**
     * Check if this counter is currently tracking a nested value.
     *
     * @return {@code true} if this counter is currently tracking a nested value
     */
    public boolean isNested() {
        return lists > 0 || compounds > 0;
    }
}
