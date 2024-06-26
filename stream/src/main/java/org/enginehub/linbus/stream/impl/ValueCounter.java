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
        switch (token) {
            case LinToken.CompoundStart compoundStart -> compounds++;
            case LinToken.CompoundEnd compoundEnd -> {
                compounds--;
                if (compounds < 0) {
                    throw new NbtParseException("Compound end without start");
                }
                if (!isNested()) {
                    count++;
                }
            }
            case LinToken.ListStart listStart -> lists++;
            case LinToken.ListEnd listEnd -> {
                lists--;
                if (lists < 0) {
                    throw new NbtParseException("List end without start");
                }
                if (!isNested()) {
                    count++;
                }
            }
            case LinToken.ByteArrayStart byteArrayStart -> arrayType = BYTE_ARRAY;
            case LinToken.ByteArrayEnd byteArrayEnd -> {
                if (arrayType != BYTE_ARRAY) {
                    throw new NbtParseException("Byte array end without start");
                }
                arrayType = 0;
                if (!isNested()) {
                    count++;
                }
            }
            case LinToken.IntArrayStart intArrayStart -> arrayType = INT_ARRAY;
            case LinToken.IntArrayEnd intArrayEnd -> {
                if (arrayType != INT_ARRAY) {
                    throw new NbtParseException("Int array end without start");
                }
                arrayType = 0;
                if (!isNested()) {
                    count++;
                }
            }
            case LinToken.LongArrayStart longArrayStart -> arrayType = LONG_ARRAY;
            case LinToken.LongArrayEnd longArrayEnd -> {
                if (arrayType != LONG_ARRAY) {
                    throw new NbtParseException("Long array end without start");
                }
                arrayType = 0;
                if (!isNested()) {
                    count++;
                }
            }
            default -> {
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
