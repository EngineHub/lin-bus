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
 * SipHash, ported from the CC0-licensed reference implementation at
 * <a href="https://github.com/veorq/SipHash">github.com/veorq/SipHash</a>.
 */
final class SipHash {
    /**
     * SipHash-{@code compressionRounds}-{@code finalizationRounds} over {@code data}'s chars, each fed as a
     * little-endian 16-bit unit so no intermediate {@code byte[]} is allocated.
     *
     * @param compressionRounds the number of compression rounds
     * @param finalizationRounds the number of finalization rounds
     * @param k0 the low half of the 128-bit key
     * @param k1 the high half of the 128-bit key
     * @param data the string to hash
     * @return the 64-bit hash
     */
    static long hash(int compressionRounds, int finalizationRounds, long k0, long k1, String data) {
        var v = new State(k0, k1);
        int length = data.length();
        int fullBlocks = length & ~3;
        for (int i = 0; i < fullBlocks; i += 4) {
            long m = (data.charAt(i) & 0xFFFFL)
                | (data.charAt(i + 1) & 0xFFFFL) << 16
                | (data.charAt(i + 2) & 0xFFFFL) << 32
                | (data.charAt(i + 3) & 0xFFFFL) << 48;
            v.v3 ^= m;
            v.compress(compressionRounds);
            v.v0 ^= m;
        }
        long b = ((long) length * 2) << 56;
        for (int i = fullBlocks; i < length; i++) {
            b |= (data.charAt(i) & 0xFFFFL) << ((i - fullBlocks) * 16);
        }
        v.v3 ^= b;
        v.compress(compressionRounds);
        v.v0 ^= b;
        v.v2 ^= 0xFF;
        v.compress(finalizationRounds);
        return v.v0 ^ v.v1 ^ v.v2 ^ v.v3;
    }

    /**
     * Internal state of the SipHash algorithm.
     *
     * <p>
     * Uses a class instead of an array, as it proved to inline better in benchmarks.
     * In the future, we can also convert it to a value class, which will inline even better.
     * </p>
     */
    private static final class State {
        long v0;
        long v1;
        long v2;
        long v3;

        State(long k0, long k1) {
            this.v0 = 0x736F6D6570736575L ^ k0;
            this.v1 = 0x646F72616E646F6DL ^ k1;
            this.v2 = 0x6C7967656E657261L ^ k0;
            this.v3 = 0x7465646279746573L ^ k1;
        }

        void compress(int rounds) {
            for (int r = 0; r < rounds; r++) {
                // See SIPROUND macro in the reference implementation.
                this.v0 += this.v1;
                this.v1 = Long.rotateLeft(this.v1, 13);
                this.v1 ^= this.v0;
                this.v0 = Long.rotateLeft(this.v0, 32);
                this.v2 += this.v3;
                this.v3 = Long.rotateLeft(this.v3, 16);
                this.v3 ^= this.v2;
                this.v0 += this.v3;
                this.v3 = Long.rotateLeft(this.v3, 21);
                this.v3 ^= this.v0;
                this.v2 += this.v1;
                this.v1 = Long.rotateLeft(this.v1, 17);
                this.v1 ^= this.v2;
                this.v2 = Long.rotateLeft(this.v2, 32);
            }
        }
    }

    private SipHash() {
    }
}
