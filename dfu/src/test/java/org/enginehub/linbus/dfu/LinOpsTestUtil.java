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

package org.enginehub.linbus.dfu;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.MapLike;
import org.enginehub.linbus.tree.LinTag;
import org.jspecify.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Stream;

class LinOpsTestUtil {

    static MapLike<LinTag<?>> mapLikeOf(Map<LinTag<?>, LinTag<?>> entries) {
        Map<LinTag<?>, LinTag<?>> copy = new LinkedHashMap<>(entries);
        return new MapLike<>() {
            @Override
            public @Nullable LinTag<?> get(LinTag<?> key) {
                return null;
            }

            @Override
            public @Nullable LinTag<?> get(String key) {
                return null;
            }

            @Override
            public Stream<Pair<LinTag<?>, LinTag<?>>> entries() {
                return copy.entrySet().stream().map(entry -> Pair.of(entry.getKey(), entry.getValue()));
            }
        };
    }

    private LinOpsTestUtil() {
    }
}
