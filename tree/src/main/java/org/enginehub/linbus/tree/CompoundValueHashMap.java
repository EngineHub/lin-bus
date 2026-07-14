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

import org.jspecify.annotations.Nullable;

import java.security.SecureRandom;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * An immutable, insertion-ordered map backing for {@link LinCompoundTag}, using an open-addressed
 * hash table for lookups.
 *
 * <p>
 * This is used to avoid the overhead of wrapping a {@link LinkedHashMap} in an unmodifiable view,
 * and the cache-unfriendly design of it due to storing 2 extra pointers per entry.
 * </p>
 *
 * <p>
 * For small maps the extra hash is more expensive than just loading the whole array, so
 * {@link CompoundValueLinearMap} is recommended instead below
 * {@link CompoundValueLinearMap#RECOMMENDED_MAX_LINEAR_SIZE} entries.
 * </p>
 */
final class CompoundValueHashMap extends AbstractCompoundValueMap {
    // There's potential for optimization of memory further here by having dedicated subclasses
    // for specific sizes that uses a byte[] or short[] for the table instead.
    // I'm unsure if it would affect CPU performance, and it's certainly a lot more code,
    // so I'm leaving it as-is for now.

    // K0 and K1 are exposed here for JMH benchmarking, so that a copy of this class can be made that uses the same
    // values, so they don't differ between the two classes and affect the benchmark results.
    static final long K0;
    static final long K1;

    static {
        var seed = new SecureRandom();
        K0 = seed.nextLong();
        K1 = seed.nextLong();
    }

    private static int tableSizeFor(int size) {
        if (size <= 0) {
            return 1;
        }
        if (size == 1) {
            return 2;
        }
        // The target size is 1.5x the number of entries, for a load factor of 0.67. Chosen because that's what
        // Python's dict uses, and because it's funny.
        long target = ((long) size) + (size >>> 1);
        int capacity = 1;
        // Calculate closest power of two >= target, but not exceeding 2^30 (the max capacity for an int[]).
        while (capacity < target && capacity < (1 << 30)) {
            capacity <<= 1;
        }
        if (capacity <= size) {
            throw new IllegalStateException("Map too large: " + size);
        }
        return capacity;
    }

    // This is trivial enough that JIT will reliably inline it, and hoist it out of loops,
    // so it doesn't need caching in even a variable.
    private static int mask(int[] table) {
        return table.length - 1;
    }

    /**
     * Try to fill the table with the string hash, and return false if it takes too many probes to insert an entry.
     *
     * @return {@code true} if successfully filled
     */
    private static boolean fillWithStringHash(String[] keys, int[] table) {
        // Bound the number of probes by 8 * log2(len). In practice most maps should never go above 3 * log2(len).
        int maxSafeProbe = Math.max(16, 8 * Integer.numberOfTrailingZeros(table.length));
        for (int i = 0; i < keys.length; i++) {
            int slot = stringHash(keys[i]) & mask(table);
            int probes = 0;
            while (table[slot] != 0) {
                slot = (slot + 1) & mask(table);
                probes++;
                if (probes > maxSafeProbe) {
                    return false;
                }
            }
            table[slot] = i + 1;
        }
        return true;
    }

    private static int stringHash(String key) {
        // Spread the high bits down, like HashMap, since we mask with a power-of-two table.
        int h = key.hashCode();
        return h ^ (h >>> 16);
    }

    private static void fillWithSipHash(String[] keys, int[] table) {
        for (int i = 0; i < keys.length; i++) {
            int slot = sipHash(keys[i]) & mask(table);
            while (table[slot] != 0) {
                slot = (slot + 1) & mask(table);
            }
            table[slot] = i + 1;
        }
    }

    private static int sipHash(String key) {
        // SipHash avoids hash flooding attacks, just in case untrusted input is used as keys
        // (perhaps if a web tool is used to edit NBT/schematics? or open access schematic folders?).
        long h = SipHash.hash(1, 3, K0, K1, key);
        return (int) h ^ (int) (h >>> 32);
    }

    // Open-addressed index: table[slot] holds entryIndex + 1, with 0 meaning empty.
    private final int[] table;
    private final boolean usingSipHash;

    CompoundValueHashMap(Map<String, ? extends LinTag<?>> source) {
        super(source);
        int[] table = new int[tableSizeFor(this.keys.length)];
        boolean notHashFlooded = fillWithStringHash(this.keys, table);
        if (!notHashFlooded) {
            table = new int[table.length];
            fillWithSipHash(this.keys, table);
        }
        this.table = table;
        this.usingSipHash = !notHashFlooded;
    }

    @Override
    int indexOf(@Nullable Object key) {
        if (!(key instanceof String stringKey) || this.keys.length == 0) {
            return -1;
        }
        int slot = (this.usingSipHash ? sipHash(stringKey) : stringHash(stringKey)) & mask(this.table);
        int probed;
        while ((probed = this.table[slot]) != 0) {
            int index = probed - 1;
            if (this.keys[index].equals(stringKey)) {
                return index;
            }
            slot = (slot + 1) & mask(this.table);
        }
        return -1;
    }
}
