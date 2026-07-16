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

import java.util.Map;

/**
 * An immutable, insertion-ordered map backing for {@link LinCompoundTag} that looks keys up with a
 * linear scan.
 */
final class CompoundValueLinearMap extends AbstractCompoundValueMap {
    /**
     * The largest map size for which a linear scan is used instead of {@link CompoundValueHashMap}.
     *
     * <p>
     * The crossover with the hashmap was measured (JMH, JDK 25) at around 26-28 entries,
     * using a slightly lower bound just in case.
     * </p>
     *
     * @implNote This should be updated whenever implementation details change.
     */
    static final int RECOMMENDED_MAX_LINEAR_SIZE = 24;

    CompoundValueLinearMap(Map<String, ? extends LinTag<?>> source) {
        super(source);
    }

    @Override
    int indexOf(@Nullable Object key) {
        if (!(key instanceof String stringKey)) {
            return -1;
        }
        String[] keys = this.keys;
        for (int i = 0; i < keys.length; i++) {
            if (keys[i].equals(stringKey)) {
                return i;
            }
        }
        return -1;
    }
}
