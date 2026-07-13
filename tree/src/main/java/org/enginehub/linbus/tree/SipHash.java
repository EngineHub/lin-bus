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

/**
 * SipHash-2-4, ported from the CC0-licensed reference implementation at
 * <a href="https://github.com/veorq/SipHash">github.com/veorq/SipHash</a>.
 */
final class SipHash {

    private static final int COMPRESSION_ROUNDS = 2;
    private static final int FINALIZATION_ROUNDS = 4;

    /**
     * SipHash-2-4 over {@code data}'s chars, each fed as a little-endian 16-bit unit so no
     * intermediate {@code byte[]} is allocated.
     *
     * @param k0 the low half of the 128-bit key
     * @param k1 the high half of the 128-bit key
     * @param data the string to hash
     * @return the 64-bit hash
     */
    static long hash24(long k0, long k1, String data) {
        long[] v = {
            0x736F6D6570736575L ^ k0,
            0x646F72616E646F6DL ^ k1,
            0x6C7967656E657261L ^ k0,
            0x7465646279746573L ^ k1,
        };
        int length = data.length();
        int fullBlocks = length & ~3;
        for (int i = 0; i < fullBlocks; i += 4) {
            long m = (data.charAt(i) & 0xFFFFL)
                | (data.charAt(i + 1) & 0xFFFFL) << 16
                | (data.charAt(i + 2) & 0xFFFFL) << 32
                | (data.charAt(i + 3) & 0xFFFFL) << 48;
            v[3] ^= m;
            compress(v, COMPRESSION_ROUNDS);
            v[0] ^= m;
        }
        long b = ((long) length * 2) << 56;
        for (int i = fullBlocks; i < length; i++) {
            b |= (data.charAt(i) & 0xFFFFL) << ((i - fullBlocks) * 16);
        }
        v[3] ^= b;
        compress(v, COMPRESSION_ROUNDS);
        v[0] ^= b;
        v[2] ^= 0xFF;
        compress(v, FINALIZATION_ROUNDS);
        return v[0] ^ v[1] ^ v[2] ^ v[3];
    }

    private static void compress(long[] v, int rounds) {
        for (int r = 0; r < rounds; r++) {
            // See SIPROUND macro in the reference implementation.
            v[0] += v[1];
            v[1] = Long.rotateLeft(v[1], 13);
            v[1] ^= v[0];
            v[0] = Long.rotateLeft(v[0], 32);
            v[2] += v[3];
            v[3] = Long.rotateLeft(v[3], 16);
            v[3] ^= v[2];
            v[0] += v[3];
            v[3] = Long.rotateLeft(v[3], 21);
            v[3] ^= v[0];
            v[2] += v[1];
            v[1] = Long.rotateLeft(v[1], 17);
            v[1] ^= v[2];
            v[2] = Long.rotateLeft(v[2], 32);
        }
    }

    private SipHash() {
    }
}
