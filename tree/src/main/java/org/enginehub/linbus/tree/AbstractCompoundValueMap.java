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

import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Set;

/**
 * Shared base for the immutable, insertion-ordered map backings of {@link LinCompoundTag}. Entries
 * are held in parallel {@code keys}/{@code values} arrays in insertion order; subclasses only supply
 * the key lookup strategy via {@link #indexOf(Object)}.
 */
abstract class AbstractCompoundValueMap extends AbstractMap<String, LinTag<?>> {
    final String[] keys;
    final LinTag<?>[] values;

    AbstractCompoundValueMap(Map<String, ? extends LinTag<?>> source) {
        int size = source.size();
        this.keys = new String[size];
        this.values = new LinTag<?>[size];
        int i = 0;
        for (Map.Entry<String, ? extends LinTag<?>> entry : source.entrySet()) {
            this.keys[i] = Objects.requireNonNull(entry.getKey(), "compound key is null");
            this.values[i] = Objects.requireNonNull(entry.getValue(), "compound value is null");
            i++;
        }
    }

    /**
     * {@return the entry index of the given key, or {@code -1} if it is absent}
     *
     * @param key the key to look up
     */
    abstract int indexOf(@Nullable Object key);

    @Override
    public final @Nullable LinTag<?> get(@Nullable Object key) {
        int index = indexOf(key);
        return index < 0 ? null : this.values[index];
    }

    @Override
    public final boolean containsKey(@Nullable Object key) {
        return indexOf(key) >= 0;
    }

    @Override
    public final int size() {
        return this.keys.length;
    }

    @Override
    public final @Nullable LinTag<?> put(String key, LinTag<?> value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public final @Nullable LinTag<?> remove(Object key) {
        throw new UnsupportedOperationException();
    }

    @Override
    public final void putAll(Map<? extends String, ? extends LinTag<?>> m) {
        throw new UnsupportedOperationException();
    }

    @Override
    public final void clear() {
        throw new UnsupportedOperationException();
    }

    @Override
    public final Set<Map.Entry<String, LinTag<?>>> entrySet() {
        return new EntrySet();
    }

    private final class EntrySet extends AbstractSet<Map.Entry<String, LinTag<?>>> {
        @Override
        public boolean contains(Object o) {
            if (!(o instanceof Map.Entry<?, ?> entry)) {
                return false;
            }
            int index = indexOf(entry.getKey());
            return index >= 0 && AbstractCompoundValueMap.this.values[index].equals(entry.getValue());
        }

        @Override
        public Iterator<Map.Entry<String, LinTag<?>>> iterator() {
            return new Iterator<>() {
                private int cursor;

                @Override
                public boolean hasNext() {
                    return this.cursor < AbstractCompoundValueMap.this.keys.length;
                }

                @Override
                public Map.Entry<String, LinTag<?>> next() {
                    if (this.cursor >= AbstractCompoundValueMap.this.keys.length) {
                        throw new NoSuchElementException();
                    }
                    int index = this.cursor++;
                    return new SimpleImmutableEntry<>(
                        AbstractCompoundValueMap.this.keys[index],
                        AbstractCompoundValueMap.this.values[index]
                    );
                }
            };
        }

        @Override
        public int size() {
            return AbstractCompoundValueMap.this.keys.length;
        }
    }
}
