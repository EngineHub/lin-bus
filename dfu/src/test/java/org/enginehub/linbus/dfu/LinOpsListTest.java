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
import java.util.stream.Stream;

import static com.google.common.truth.Truth.assertThat;
import static org.enginehub.linbus.dfu.DataResultSubject.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class LinOpsListTest {

    private static final LinOps OPS = LinOps.getInstance();

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
    void createListRejectsMixedElementTypes() {
        assertThrows(
            IllegalArgumentException.class,
            () -> OPS.createList(Stream.of(LinIntTag.of(1), LinStringTag.of("x")))
        );
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
    void mergeToListRejectsMismatchedElementInList() {
        assertThat(OPS.mergeToList(
            LinListTag.of(LinTagType.intTag(), List.of(LinIntTag.of(1))), LinStringTag.of("x")
        )).hasErrorWithMessageThat().startsWith("Element is not of type ");
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
