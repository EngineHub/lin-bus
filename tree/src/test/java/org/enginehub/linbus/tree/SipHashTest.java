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

package org.enginehub.linbus.tree;

import org.junit.jupiter.api.Test;

import static com.google.common.truth.Truth.assertThat;

class SipHashTest {

    // The little-endian outputs from vectors_sip64, for the message lengths in MESSAGE_LENGTHS.
    // https://github.com/veorq/SipHash/blob/32d067603b93b47828700880649198e0bfbbcffa/vectors.h
    private static final int[] MESSAGE_LENGTHS = {0, 2, 4, 6, 8, 16};
    private static final int[][] VECTORS = {
        {
            0x31,
            0x0E,
            0x0E,
            0xDD,
            0x47,
            0xDB,
            0x6F,
            0x72,
        },
        {
            0x5A,
            0x4F,
            0xA9,
            0xD9,
            0x09,
            0x80,
            0x6C,
            0x0D,
        },
        {
            0xB7,
            0x87,
            0x71,
            0x27,
            0xE0,
            0x94,
            0x27,
            0xCF,
        },
        {
            0xCE,
            0xE3,
            0xFE,
            0x58,
            0x6E,
            0x46,
            0xC9,
            0xCB,
        },
        {
            0x62,
            0x24,
            0x93,
            0x9A,
            0x79,
            0xF5,
            0xF5,
            0x93,
        },
        {
            0xDB,
            0x9B,
            0xC2,
            0x57,
            0x7F,
            0xCC,
            0x2A,
            0x3F,
        },
    };

    private static int[] littleEndianBytes(long value) {
        var bytes = new int[Long.BYTES];
        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = (int) (value >>> (8 * i)) & 0xFF;
        }
        return bytes;
    }

    /**
     * Creates a reference message matching the reference input vectors from the original SipHash implementation.
     */
    private static String referenceMessage(int byteLength) {
        var chars = new char[byteLength / 2];
        for (int i = 0; i < chars.length; i++) {
            chars[i] = (char) ((2 * i) | ((2 * i + 1) << 8));
        }
        return new String(chars);
    }

    @Test
    void matchesReferenceVectors() {
        long k0 = 0x07_06_05_04_03_02_01_00L;
        long k1 = 0x0F_0E_0D_0C_0B_0A_09_08L;
        for (int i = 0; i < MESSAGE_LENGTHS.length; i++) {
            long hash = SipHash.hash(2, 4, k0, k1, referenceMessage(MESSAGE_LENGTHS[i]));
            assertThat(littleEndianBytes(hash)).isEqualTo(VECTORS[i]);
        }
    }
}
