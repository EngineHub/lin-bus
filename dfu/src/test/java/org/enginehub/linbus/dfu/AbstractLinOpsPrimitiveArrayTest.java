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

import com.mojang.serialization.DataResult;
import org.enginehub.linbus.tree.LinCompoundTag;
import org.enginehub.linbus.tree.LinListTag;
import org.enginehub.linbus.tree.LinStringTag;
import org.enginehub.linbus.tree.LinTag;
import org.enginehub.linbus.tree.LinTagType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.enginehub.linbus.dfu.DataResultSubject.assertThat;

abstract class AbstractLinOpsPrimitiveArrayTest {

    static final LinOps OPS = LinOps.getInstance();

    abstract LinTag<?> element(int value);

    abstract LinTag<?> arrayTag(int... values);

    abstract LinTag<?> listOfElementTag(int... values);

    abstract LinTag<?> foreignElement();

    abstract DataResult<?> readSequence(LinTag<?> input);

    @Test
    void getStreamReadsArrayTag() {
        assertThat(OPS.getStream(arrayTag(1, 2))).hasStreamResultThat()
            .containsExactly(element(1), element(2)).inOrder();
    }

    @Test
    void readSequenceRejectsWrongTypedList() {
        assertThat(readSequence(LinListTag.of(LinTagType.stringTag(), List.of(LinStringTag.of("a"))))).isError();
    }

    @Test
    void readSequenceRejectsNonList() {
        assertThat(readSequence(LinCompoundTag.builder().build())).isError();
    }

    @Test
    @NbtOpsBehavior
    @DisplayName("merging onto an empty byte/int/long array yields a list tag, not an array tag")
    void mergeToListOntoEmptyArrayMakesListTag() {
        assertThat(OPS.mergeToList(arrayTag(), element(5))).hasResultThat().isEqualTo(listOfElementTag(5));
    }

    @Test
    void mergeToListRepacksArrayTag() {
        assertThat(OPS.mergeToList(arrayTag(1, 2), element(3))).hasResultThat().isEqualTo(arrayTag(1, 2, 3));
    }

    @Test
    void mergeToListRejectsMismatchedElement() {
        assertThat(OPS.mergeToList(arrayTag(1, 2), foreignElement()))
            .hasErrorWithMessageThat().startsWith("Element is not of type ");
    }

    @Test
    void mergeToListAppendsSeveralValuesOntoArrayTag() {
        assertThat(OPS.mergeToList(arrayTag(1, 2), List.of(element(3), element(4))))
            .hasResultThat().isEqualTo(arrayTag(1, 2, 3, 4));
    }
}
