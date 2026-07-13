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
import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Set;

/**
 * An immutable, insertion-ordered map backing for {@link LinCompoundTag}.
 *
 * <p>
 * This is used to avoid the overhead of wrapping a {@link LinkedHashMap} in an unmodifiable view,
 * and the cache-unfriendly design of it due to storing 2 extra pointers per entry.
 * </p>
 */
final class CompoundValueMap extends AbstractMap<String, LinTag<?>> {
    // There's potential for optimization of memory further here by having dedicated subclasses
    // for specific sizes that uses a byte[] or short[] for the table instead.
    // I'm unsure if it would affect CPU performance, and it's certainly a lot more code,
    // so I'm leaving it as-is for now.

    private static final long K0;
    private static final long K1;

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

    private static int hash(String key) {
        // We use SipHash here to avoid hash flooding attacks, just in case untrusted input is used as keys
        // (perhaps if a web tool is used to edit NBT/schematics? or open access schematic folders?).
        // As a bonus, this also significantly reduces the likelyhood of hash collisions, which allows us to use
        // a larger load factor and reduce memory usage.
        long h = SipHash.hash24(K0, K1, key);
        return (int) h ^ (int) (h >>> 32);
    }

    private final String[] keys;
    private final LinTag<?>[] values;
    // Open-addressed index: table[slot] holds entryIndex + 1, with 0 meaning empty.
    private final int[] table;
    private final int mask;

    CompoundValueMap(Map<String, ? extends LinTag<?>> source) {
        int size = source.size();
        this.keys = new String[size];
        this.values = new LinTag<?>[size];
        int capacity = tableSizeFor(size);
        this.table = new int[capacity];
        this.mask = capacity - 1;
        int i = 0;
        for (Map.Entry<String, ? extends LinTag<?>> entry : source.entrySet()) {
            String key = Objects.requireNonNull(entry.getKey(), "compound key is null");
            LinTag<?> value = Objects.requireNonNull(entry.getValue(), "compound value is null");
            this.keys[i] = key;
            this.values[i] = value;
            insertIndex(key, i);
            i++;
        }
    }

    private void insertIndex(String key, int entryIndex) {
        int slot = hash(key) & this.mask;
        while (this.table[slot] != 0) {
            slot = (slot + 1) & this.mask;
        }
        this.table[slot] = entryIndex + 1;
    }

    private int indexOf(@Nullable Object key) {
        if (!(key instanceof String stringKey) || this.keys.length == 0) {
            return -1;
        }
        int slot = hash(stringKey) & this.mask;
        int probed;
        while ((probed = this.table[slot]) != 0) {
            int index = probed - 1;
            if (this.keys[index].equals(stringKey)) {
                return index;
            }
            slot = (slot + 1) & this.mask;
        }
        return -1;
    }

    @Override
    public @Nullable LinTag<?> get(@Nullable Object key) {
        int index = indexOf(key);
        return index < 0 ? null : this.values[index];
    }

    @Override
    public boolean containsKey(@Nullable Object key) {
        return indexOf(key) >= 0;
    }

    @Override
    public int size() {
        return this.keys.length;
    }

    @Override
    public @Nullable LinTag<?> put(String key, LinTag<?> value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public @Nullable LinTag<?> remove(Object key) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void putAll(Map<? extends String, ? extends LinTag<?>> m) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Set<Map.Entry<String, LinTag<?>>> entrySet() {
        return new EntrySet();
    }

    private final class EntrySet extends AbstractSet<Map.Entry<String, LinTag<?>>> {
        @Override
        public boolean contains(Object o) {
            if (!(o instanceof Map.Entry<?, ?> entry)) {
                return false;
            }
            int index = indexOf(entry.getKey());
            return index >= 0 && CompoundValueMap.this.values[index].equals(entry.getValue());
        }

        @Override
        public Iterator<Map.Entry<String, LinTag<?>>> iterator() {
            return new Iterator<>() {
                private int cursor;

                @Override
                public boolean hasNext() {
                    return this.cursor < CompoundValueMap.this.keys.length;
                }

                @Override
                public Map.Entry<String, LinTag<?>> next() {
                    if (this.cursor >= CompoundValueMap.this.keys.length) {
                        throw new NoSuchElementException();
                    }
                    int index = this.cursor++;
                    return new SimpleImmutableEntry<>(
                        CompoundValueMap.this.keys[index],
                        CompoundValueMap.this.values[index]
                    );
                }
            };
        }

        @Override
        public int size() {
            return CompoundValueMap.this.keys.length;
        }
    }
}
