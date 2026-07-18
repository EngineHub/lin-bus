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

import org.enginehub.linbus.tree.LinCompoundTag;
import org.enginehub.linbus.tree.LinEndTag;
import org.enginehub.linbus.tree.LinIntTag;
import org.enginehub.linbus.tree.LinListTag;
import org.enginehub.linbus.tree.LinStringTag;
import org.enginehub.linbus.tree.LinTag;
import org.enginehub.linbus.tree.LinTagType;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static com.google.common.truth.Truth.assertThat;
import static org.enginehub.linbus.dfu.DataResultSubject.assertThat;

class LinOpsListTest {

    private static final LinOps OPS = LinOps.getInstance();

    private static LinCompoundTag wrap(LinTag<?> value) {
        return LinCompoundTag.of(Map.of("", value));
    }

    @Test
    void getStreamReadsList() {
        assertThat(OPS.getStream(
            LinListTag.of(LinTagType.intTag(), List.of(LinIntTag.of(1), LinIntTag.of(2)))
        )).hasStreamResultThat().containsExactly(LinIntTag.of(1), LinIntTag.of(2)).inOrder();
    }

    @Test
    void getStreamRejectsCompound() {
        assertThat(OPS.getStream(LinCompoundTag.builder().build())).hasErrorWithMessageThat().startsWith("Not a list");
    }

    @Test
    void getListVisitsElementsInOrder() {
        List<LinTag<?>> collected = new ArrayList<>();
        assertThat(OPS.getList(LinListTag.of(LinTagType.intTag(), List.of(LinIntTag.of(1), LinIntTag.of(2)))))
            .resultOrFail().accept(collected::add);
        assertThat(collected).containsExactly(LinIntTag.of(1), LinIntTag.of(2)).inOrder();
    }

    @Test
    void getListRejectsNonList() {
        assertThat(OPS.getList(LinStringTag.of("x"))).hasErrorWithMessageThat().startsWith("Not a list");
    }

    @Test
    void createListInfersElementTypeFromFirstElement() {
        assertThat(OPS.createList(Stream.of(LinIntTag.of(1), LinIntTag.of(2))))
            .isEqualTo(LinListTag.of(LinTagType.intTag(), List.of(LinIntTag.of(1), LinIntTag.of(2))));
    }

    @Test
    void createListOfNothingIsEndTypedList() {
        assertThat(OPS.createList(Stream.of())).isEqualTo(LinListTag.empty(LinTagType.endTag()));
    }

    @Test
    @NbtOpsBehavior
    void createListWrapsMixedElementTypes() {
        assertThat(OPS.createList(Stream.of(LinIntTag.of(1), LinStringTag.of("x"))))
            .isEqualTo(LinListTag.of(LinTagType.compoundTag(), List.of(
                wrap(LinIntTag.of(1)), wrap(LinStringTag.of("x"))
            )));
    }

    @Test
    @NbtOpsBehavior
    void getStreamUnwrapsMixedElementTypes() {
        LinTag<?> mixed = OPS.createList(Stream.of(LinIntTag.of(1), LinStringTag.of("x")));
        assertThat(OPS.getStream(mixed))
            .hasStreamResultThat().containsExactly(LinIntTag.of(1), LinStringTag.of("x")).inOrder();
    }

    @Test
    @NbtOpsBehavior
    void createListKeepsGenuineCompoundElements() {
        LinCompoundTag a = LinCompoundTag.builder().putInt("a", 1).build();
        LinCompoundTag b = LinCompoundTag.builder().putInt("b", 2).build();
        LinTag<?> list = OPS.createList(Stream.of(a, b));
        assertThat(list).isEqualTo(LinListTag.of(LinTagType.compoundTag(), List.of(a, b)));
        assertThat(OPS.getStream(list)).hasStreamResultThat().containsExactly(a, b).inOrder();
    }

    @Test
    @NbtOpsBehavior
    void createListWrapsOnlyNonCompoundElementsWhenMixedWithCompound() {
        LinCompoundTag a = LinCompoundTag.builder().putInt("a", 1).build();
        LinTag<?> list = OPS.createList(Stream.of(a, LinIntTag.of(2)));
        assertThat(list)
            .isEqualTo(LinListTag.of(LinTagType.compoundTag(), List.of(a, wrap(LinIntTag.of(2)))));
        assertThat(OPS.getStream(list)).hasStreamResultThat().containsExactly(a, LinIntTag.of(2)).inOrder();
    }

    @Test
    void mergeToListOntoEndTakesElementType() {
        assertThat(OPS.mergeToList(LinEndTag.instance(), LinIntTag.of(1)))
            .hasResultThat().isEqualTo(LinListTag.of(LinTagType.intTag(), List.of(LinIntTag.of(1))));
    }

    @Test
    void mergeToListOntoEmptyListTakesElementType() {
        assertThat(OPS.mergeToList(LinListTag.empty(LinTagType.intTag()), LinIntTag.of(1)))
            .hasResultThat().isEqualTo(LinListTag.of(LinTagType.intTag(), List.of(LinIntTag.of(1))));
    }

    @Test
    void mergeToListGrowsList() {
        assertThat(OPS.mergeToList(
            LinListTag.of(LinTagType.intTag(), List.of(LinIntTag.of(1))), LinIntTag.of(2)
        )).hasResultThat().isEqualTo(LinListTag.of(LinTagType.intTag(), List.of(LinIntTag.of(1), LinIntTag.of(2))));
    }

    @Test
    @NbtOpsBehavior
    void mergeToListWrapsMismatchedElement() {
        assertThat(OPS.mergeToList(
            LinListTag.of(LinTagType.intTag(), List.of(LinIntTag.of(1))), LinStringTag.of("x")
        )).hasResultThat().isEqualTo(LinListTag.of(LinTagType.compoundTag(), List.of(
            wrap(LinIntTag.of(1)), wrap(LinStringTag.of("x"))
        )));
    }

    @Test
    @NbtOpsBehavior
    void mergeToListGrowsWrappedList() {
        LinTag<?> mixed = OPS.mergeToList(
            LinListTag.of(LinTagType.intTag(), List.of(LinIntTag.of(1))), LinStringTag.of("x")
        ).result().orElseThrow();
        assertThat(OPS.mergeToList(mixed, LinIntTag.of(2)))
            .hasResultThat().isEqualTo(LinListTag.of(LinTagType.compoundTag(), List.of(
                wrap(LinIntTag.of(1)), wrap(LinStringTag.of("x")), wrap(LinIntTag.of(2))
            )));
    }

    @Test
    void mergeToListAppendsSeveralValues() {
        assertThat(OPS.mergeToList(
            LinEndTag.instance(), List.of(LinIntTag.of(1), LinIntTag.of(2))
        )).hasResultThat().isEqualTo(LinListTag.of(LinTagType.intTag(), List.of(LinIntTag.of(1), LinIntTag.of(2))));
    }

    @Test
    void mergeToListRejectsNonList() {
        assertThat(OPS.mergeToList(LinStringTag.of("x"), LinIntTag.of(1)))
            .hasErrorWithMessageThat().startsWith("mergeToList called with non-list: ");
        assertThat(OPS.mergeToList(LinCompoundTag.builder().build(), LinIntTag.of(1)))
            .hasErrorWithMessageThat().startsWith("mergeToList called with non-list: ");
    }

    @Test
    void mergeToListOfSeveralValuesRejectsNonList() {
        assertThat(OPS.mergeToList(LinStringTag.of("x"), List.of(LinIntTag.of(1))))
            .hasErrorWithMessageThat().startsWith("mergeToList called with non-list: ");
    }
}
