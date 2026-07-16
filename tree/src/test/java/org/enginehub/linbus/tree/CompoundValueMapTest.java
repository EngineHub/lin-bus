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

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.google.common.truth.Truth.assertThat;
import static com.google.common.truth.Truth.assertWithMessage;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class CompoundValueMapTest {

    private static Map<String, LinTag<?>> valueOf(Map<String, ? extends LinTag<?>> entries) {
        return LinCompoundTag.of(entries).value();
    }

    @Test
    void getContainsAndNullTolerance() {
        var map = valueOf(Map.of(
            "Hello", LinStringTag.of("World!"),
            "Count", LinIntTag.of(3)
        ));
        assertThat(map.get("Hello")).isEqualTo(LinStringTag.of("World!"));
        assertThat(map.containsKey("Count")).isTrue();
        assertThat(map.get("missing")).isNull();
        assertThat(map.containsKey("missing")).isFalse();
        // Foreign / null keys must not throw (cross-type Map.equals relies on this).
        assertThat(map.get(null)).isNull();
        assertThat(map.containsKey(null)).isFalse();
        Object foreignKey = 42;
        assertThat(map.get(foreignKey)).isNull();
        assertThat(map.containsKey(foreignKey)).isFalse();
    }

    @Test
    void preservesInsertionOrder() {
        var source = new LinkedHashMap<String, LinTag<?>>();
        source.put("z", LinIntTag.of(0));
        source.put("a", LinIntTag.of(1));
        source.put("m", LinIntTag.of(2));
        source.put("b", LinIntTag.of(3));
        var map = valueOf(source);
        assertThat(map.keySet()).containsExactly("z", "a", "m", "b").inOrder();
    }

    @Test
    void isImmutable() {
        var map = valueOf(Map.of("Hello", LinStringTag.of("World!")));
        assertThrows(UnsupportedOperationException.class, () -> map.put("x", LinIntTag.of(1)));
        assertThrows(UnsupportedOperationException.class, () -> map.remove("Hello"));
        assertThrows(UnsupportedOperationException.class, () -> map.remove("absent"));
        assertThrows(UnsupportedOperationException.class, map::clear);
        assertThrows(UnsupportedOperationException.class, () -> map.putAll(Map.of("y", LinIntTag.of(2))));
        var entry = map.entrySet().iterator().next();
        assertThrows(UnsupportedOperationException.class, () -> entry.setValue(LinIntTag.of(9)));
        assertThrows(UnsupportedOperationException.class, () -> map.entrySet().iterator().remove());
    }

    @Test
    void equalsAndHashCodeMatchLinkedHashMapRegardlessOfOrder() {
        var map = valueOf(Map.of(
            "Hello", LinStringTag.of("World!"),
            "Count", LinIntTag.of(3)
        ));
        var reordered = new LinkedHashMap<String, LinTag<?>>();
        reordered.put("Count", LinIntTag.of(3));
        reordered.put("Hello", LinStringTag.of("World!"));
        assertThat(map).isEqualTo(reordered);
        assertThat(reordered).isEqualTo(map);
        assertThat(map.hashCode()).isEqualTo(reordered.hashCode());
    }

    @Test
    void entrySetContainsChecksKeyAndValue() {
        var map = valueOf(Map.of(
            "Hello", LinStringTag.of("World!"),
            "Count", LinIntTag.of(3)
        ));
        var entrySet = map.entrySet();
        assertThat(entrySet.contains(Map.entry("Hello", LinStringTag.of("World!")))).isTrue();
        assertThat(entrySet.contains(Map.entry("Hello", LinStringTag.of("wrong")))).isFalse();
        assertThat(entrySet.contains(Map.entry("missing", LinStringTag.of("World!")))).isFalse();
        assertThat(entrySet.contains((Object) "not an entry")).isFalse();
    }

    @Test
    void handlesHashCodeCollisions() {
        // Every key built from "Aa"/"BB" pairs shares the same String.hashCode(), so a raw-hashCode
        // index would pile them into one probe cluster. They must still all resolve, in order.
        var source = new LinkedHashMap<String, LinTag<?>>();
        var expectedOrder = new java.util.ArrayList<String>();
        for (int i = 0; i < 256; i++) {
            var key = new StringBuilder();
            for (int bit = 0; bit < 8; bit++) {
                key.append((i & (1 << bit)) == 0 ? "Aa" : "BB");
            }
            source.put(key.toString(), LinIntTag.of(i));
            expectedOrder.add(key.toString());
        }
        assertThat(expectedOrder.stream().map(String::hashCode).distinct().count()).isEqualTo(1);
        var map = valueOf(source);
        assertThat(map).hasSize(256);
        for (int i = 0; i < 256; i++) {
            assertThat(map.get(expectedOrder.get(i))).isEqualTo(LinIntTag.of(i));
        }
        assertThat(List.copyOf(map.keySet())).isEqualTo(expectedOrder);
    }

    private static Map<String, LinTag<?>> sizedSource(int size) {
        var source = new LinkedHashMap<String, LinTag<?>>();
        for (int i = 0; i < size; i++) {
            source.put("entry_" + i, LinIntTag.of(i));
        }
        return source;
    }

    @Test
    void selectsLinearBelowThresholdAndHashAbove() {
        assertThat(valueOf(sizedSource(2)))
            .isInstanceOf(CompoundValueLinearMap.class);
        assertThat(valueOf(sizedSource(CompoundValueLinearMap.RECOMMENDED_MAX_LINEAR_SIZE)))
            .isInstanceOf(CompoundValueLinearMap.class);
        assertThat(valueOf(sizedSource(CompoundValueLinearMap.RECOMMENDED_MAX_LINEAR_SIZE + 1)))
            .isInstanceOf(CompoundValueHashMap.class);
    }

    @Test
    void behaviorIsConsistentAcrossBackingMaps() {
        for (int size : new int[] {
            2,
            CompoundValueLinearMap.RECOMMENDED_MAX_LINEAR_SIZE,
            CompoundValueLinearMap.RECOMMENDED_MAX_LINEAR_SIZE + 1,
            100,
        }) {
            Map<String, LinTag<?>> source = sizedSource(size);
            var map = valueOf(source);
            assertWithMessage("size %s", size).that(map).hasSize(size);
            for (int i = 0; i < size; i++) {
                assertWithMessage("size %s key entry_%s", size, i)
                    .that(map.get("entry_" + i)).isEqualTo(LinIntTag.of(i));
            }
            assertWithMessage("size %s miss", size).that(map.get("entry_" + size)).isNull();
            assertWithMessage("size %s order", size)
                .that(List.copyOf(map.keySet())).isEqualTo(List.copyOf(source.keySet()));
        }
    }

    @Test
    void largeMapLookupsAndOrder() {
        var source = new LinkedHashMap<String, LinTag<?>>();
        var expectedOrder = new java.util.ArrayList<String>();
        for (int i = 0; i < 200; i++) {
            String key = "key-" + i;
            source.put(key, LinIntTag.of(i));
            expectedOrder.add(key);
        }
        var map = valueOf(source);
        assertThat(map).hasSize(200);
        for (int i = 0; i < 200; i++) {
            assertThat(map.get("key-" + i)).isEqualTo(LinIntTag.of(i));
        }
        assertThat(map.get("key-200")).isNull();
        assertThat(List.copyOf(map.keySet())).isEqualTo(expectedOrder);
    }
}
