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
import org.enginehub.linbus.tree.LinCompoundTag;
import org.enginehub.linbus.tree.LinEndTag;
import org.enginehub.linbus.tree.LinIntTag;
import org.enginehub.linbus.tree.LinListTag;
import org.enginehub.linbus.tree.LinStringTag;
import org.enginehub.linbus.tree.LinTag;
import org.enginehub.linbus.tree.LinTagType;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.google.common.truth.Truth.assertThat;
import static org.enginehub.linbus.dfu.DataResultSubject.assertThat;
import static org.enginehub.linbus.dfu.LinOpsTestUtil.mapLikeOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

class LinOpsMapTest {

    private static final LinOps OPS = LinOps.getInstance();

    private static MapLike<LinTag<?>> oneEntryMapLike() {
        return assertThat(OPS.getMap(LinCompoundTag.builder().putInt("a", 1).build())).resultOrFail();
    }

    private static MapLike<LinTag<?>> twoEntryMapLike() {
        return assertThat(OPS.getMap(LinCompoundTag.builder().putInt("a", 1).putInt("b", 2).build()))
            .resultOrFail();
    }

    private static Map<LinTag<?>, LinTag<?>> twoEntryMap() {
        Map<LinTag<?>, LinTag<?>> entries = new LinkedHashMap<>();
        entries.put(LinStringTag.of("a"), LinIntTag.of(1));
        entries.put(LinStringTag.of("b"), LinIntTag.of(2));
        return entries;
    }

    private static Map<LinTag<?>, LinTag<?>> singletonMap(LinTag<?> key, LinTag<?> value) {
        Map<LinTag<?>, LinTag<?>> entries = new LinkedHashMap<>();
        entries.put(key, value);
        return entries;
    }

    @Test
    void mergeToMapAddsEntry() {
        assertThat(OPS.mergeToMap(
            LinCompoundTag.builder().putString("existing", "v").build(), LinStringTag.of("k"), LinIntTag.of(1)
        )).hasResultThat().isEqualTo(LinCompoundTag.builder().putString("existing", "v").putInt("k", 1).build());
    }

    @Test
    void mergeToMapOntoEndMakesCompound() {
        assertThat(OPS.mergeToMap(LinEndTag.instance(), LinStringTag.of("k"), LinIntTag.of(1)))
            .hasResultThat().isEqualTo(LinCompoundTag.builder().putInt("k", 1).build());
    }

    @Test
    void mergeToMapRejectsNonStringKey() {
        assertThat(OPS.mergeToMap(LinCompoundTag.builder().build(), LinIntTag.of(1), LinIntTag.of(1)))
            .hasErrorWithMessageThat().startsWith("key is not a string: ");
    }

    @Test
    void mergeToMapRejectsNonMap() {
        assertThat(OPS.mergeToMap(LinListTag.empty(LinTagType.intTag()), LinStringTag.of("k"), LinIntTag.of(1)))
            .hasErrorWithMessageThat().startsWith("mergeToMap called with non-map: ");
    }

    @Test
    void mergeToMapRejectsEndValue() {
        assertThat(OPS.mergeToMap(LinEndTag.instance(), LinStringTag.of("k"), LinEndTag.instance()))
            .hasErrorWithMessageThat().startsWith("Cannot add END tag to compound tag");
    }

    @Test
    void mergeToMapCopiesMapLikeEntries() {
        assertThat(OPS.mergeToMap(LinEndTag.instance(), twoEntryMapLike()))
            .hasResultThat().isEqualTo(LinCompoundTag.builder().putInt("a", 1).putInt("b", 2).build());
    }

    @Test
    void mergeToMapRejectsNonStringKeysFromMapLike() {
        assertThat(OPS.mergeToMap(
            LinEndTag.instance(), mapLikeOf(singletonMap(LinIntTag.of(1), LinStringTag.of("v")))
        )).hasErrorWithMessageThat().startsWith("some keys are not strings: ");
    }

    @Test
    void mergeToMapRejectsEndValueFromMapLike() {
        assertThat(OPS.mergeToMap(
            LinEndTag.instance(), mapLikeOf(singletonMap(LinStringTag.of("k"), LinEndTag.instance()))
        )).hasErrorWithMessageThat().startsWith("Cannot add END tag to compound tag");
    }

    @Test
    void mergeToMapRejectsNonMapWithMapLike() {
        assertThat(OPS.mergeToMap(LinStringTag.of("x"), twoEntryMapLike()))
            .hasErrorWithMessageThat().startsWith("mergeToMap called with non-map: ");
    }

    @Test
    void mergeToMapCopiesMapEntries() {
        assertThat(OPS.mergeToMap(LinEndTag.instance(), twoEntryMap()))
            .hasResultThat().isEqualTo(LinCompoundTag.builder().putInt("a", 1).putInt("b", 2).build());
    }

    @Test
    void mergeToMapRejectsNonStringKeysFromMap() {
        assertThat(OPS.mergeToMap(LinEndTag.instance(), singletonMap(LinIntTag.of(1), LinIntTag.of(1))))
            .hasErrorWithMessageThat().startsWith("some keys are not strings: ");
    }

    @Test
    void mergeToMapRejectsEndValueFromMap() {
        assertThat(OPS.mergeToMap(
            LinEndTag.instance(), singletonMap(LinStringTag.of("k"), LinEndTag.instance())
        )).hasErrorWithMessageThat().startsWith("Cannot add END tag to compound tag");
    }

    @Test
    void mergeToMapRejectsNonMapWithMap() {
        assertThat(OPS.mergeToMap(LinStringTag.of("x"), twoEntryMap()))
            .hasErrorWithMessageThat().startsWith("mergeToMap called with non-map: ");
    }

    @Test
    void getMapValuesStreamsPairs() {
        assertThat(OPS.getMapValues(LinCompoundTag.builder().putInt("a", 1).putInt("b", 2).build()))
            .hasStreamResultThat().containsExactly(
                Pair.of(LinStringTag.of("a"), LinIntTag.of(1)),
                Pair.of(LinStringTag.of("b"), LinIntTag.of(2))
            ).inOrder();
    }

    @Test
    void getMapValuesRejectsNonMap() {
        assertThat(OPS.getMapValues(LinStringTag.of("x"))).hasErrorWithMessageThat().startsWith("Not a map: ");
    }

    @Test
    void getMapEntriesVisitsEntries() {
        Map<LinTag<?>, LinTag<?>> collected = new LinkedHashMap<>();
        assertThat(OPS.getMapEntries(LinCompoundTag.builder().putInt("a", 1).build()))
            .resultOrFail().accept(collected::put);
        assertThat(collected).containsExactly(LinStringTag.of("a"), LinIntTag.of(1));
    }

    @Test
    void getMapEntriesRejectsNonMap() {
        assertThat(OPS.getMapEntries(LinStringTag.of("x"))).hasErrorWithMessageThat().startsWith("Not a map: ");
    }

    @Test
    void getMapRejectsNonMap() {
        assertThat(OPS.getMap(LinStringTag.of("x"))).hasErrorWithMessageThat().startsWith("Not a map: ");
    }

    @Test
    void mapLikeGetsByStringKey() {
        MapLike<LinTag<?>> mapLike = oneEntryMapLike();
        assertThat(mapLike.get("a")).isEqualTo(LinIntTag.of(1));
    }

    @Test
    void mapLikeGetsByStringTagKey() {
        MapLike<LinTag<?>> mapLike = oneEntryMapLike();
        assertThat(mapLike.get(LinStringTag.of("a"))).isEqualTo(LinIntTag.of(1));
    }

    @Test
    void mapLikeGetIsNullForMissingKey() {
        MapLike<LinTag<?>> mapLike = oneEntryMapLike();
        assertThat(mapLike.get(LinStringTag.of("missing"))).isNull();
    }

    @Test
    void mapLikeRejectsNonStringTagKey() {
        MapLike<LinTag<?>> mapLike = oneEntryMapLike();
        assertThrows(UnsupportedOperationException.class, () -> mapLike.get(LinIntTag.of(1)));
    }

    @Test
    void mapLikeStreamsEntries() {
        MapLike<LinTag<?>> mapLike = oneEntryMapLike();
        assertThat(mapLike.entries().collect(Collectors.toList()))
            .containsExactly(Pair.of(LinStringTag.of("a"), LinIntTag.of(1)));
    }

    @Test
    void mapLikeToStringImplementation() {
        MapLike<LinTag<?>> mapLike = oneEntryMapLike();
        assertThat(mapLike.toString()).contains("MapLike");
    }

    @Test
    void createMapMakesCompound() {
        assertThat(OPS.createMap(Stream.of(
            Pair.of(LinStringTag.of("a"), LinIntTag.of(1)),
            Pair.of(LinStringTag.of("b"), LinIntTag.of(2))
        ))).isEqualTo(LinCompoundTag.builder().putInt("a", 1).putInt("b", 2).build());
    }

    @Test
    void createMapRejectsNonStringKey() {
        assertThrows(
            UnsupportedOperationException.class,
            () -> OPS.createMap(Stream.of(Pair.of(LinIntTag.of(1), LinIntTag.of(1))))
        );
    }

    @Test
    void createMapRejectsEndValue() {
        assertThrows(
            IllegalArgumentException.class,
            () -> OPS.createMap(Stream.of(Pair.of(LinStringTag.of("k"), LinEndTag.instance())))
        );
    }

    @Test
    void removeDropsKey() {
        assertThat(OPS.remove(LinCompoundTag.builder().putInt("a", 1).putInt("b", 2).build(), "a"))
            .isEqualTo(LinCompoundTag.builder().putInt("b", 2).build());
    }

    @Test
    void removeIgnoresNonCompound() {
        assertThat(OPS.remove(LinStringTag.of("x"), "a")).isEqualTo(LinStringTag.of("x"));
    }
}
