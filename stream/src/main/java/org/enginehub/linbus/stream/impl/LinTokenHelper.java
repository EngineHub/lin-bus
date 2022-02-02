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
import org.enginehub.linbus.stream.token.LinToken;

public class LinTokenHelper {
    public static LinTagId assignElementId(LinToken token) {
        if (token instanceof LinToken.ByteArrayStart) {
            return LinTagId.BYTE_ARRAY;
        } else if (token instanceof LinToken.Byte) {
            return LinTagId.BYTE;
        } else if (token instanceof LinToken.CompoundStart) {
            return LinTagId.COMPOUND;
        } else if (token instanceof LinToken.Double) {
            return LinTagId.DOUBLE;
        } else if (token instanceof LinToken.Float) {
            return LinTagId.FLOAT;
        } else if (token instanceof LinToken.IntArrayStart) {
            return LinTagId.INT_ARRAY;
        } else if (token instanceof LinToken.Int) {
            return LinTagId.INT;
        } else if (token instanceof LinToken.ListStart) {
            return LinTagId.LIST;
        } else if (token instanceof LinToken.LongArrayStart) {
            return LinTagId.LONG_ARRAY;
        } else if (token instanceof LinToken.Long) {
            return LinTagId.LONG;
        } else if (token instanceof LinToken.Short) {
            return LinTagId.SHORT;
        } else if (token instanceof LinToken.String) {
            return LinTagId.STRING;
        }
        throw new IllegalArgumentException("Cannot translate this to a tag ID: " + token);
    }

    private LinTokenHelper() {
    }
}
