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
import org.enginehub.linbus.tree.LinIntArrayTag;
import org.enginehub.linbus.tree.LinIntTag;
import org.enginehub.linbus.tree.LinListTag;
import org.enginehub.linbus.tree.LinLongTag;
import org.enginehub.linbus.tree.LinTag;
import org.enginehub.linbus.tree.LinTagType;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import static com.google.common.truth.Truth.assertThat;
import static org.enginehub.linbus.dfu.DataResultSubject.assertThat;

class LinOpsIntArrayTest extends AbstractLinOpsPrimitiveArrayTest {

    @Override
    LinTag<?> element(int value) {
        return LinIntTag.of(value);
    }

    @Override
    LinTag<?> arrayTag(int... values) {
        return LinIntArrayTag.of(values);
    }

    @Override
    LinTag<?> listOfElementTag(int... values) {
        List<LinIntTag> elements = new ArrayList<>();
        for (int value : values) {
            elements.add(LinIntTag.of(value));
        }
        return LinListTag.of(LinTagType.intTag(), elements);
    }

    @Override
    LinTag<?> foreignElement() {
        return LinLongTag.of(3);
    }

    @Override
    DataResult<?> readSequence(LinTag<?> input) {
        return OPS.getIntStream(input);
    }

    @Test
    void getIntStreamReadsIntArrayTag() {
        assertThat(OPS.getIntStream(LinIntArrayTag.of(1, 2, 3))).hasIntStreamResultThat()
            .containsExactly(1, 2, 3).inOrder();
    }

    @Test
    void getIntStreamReadsIntList() {
        assertThat(OPS.getIntStream(
            LinListTag.of(LinTagType.intTag(), List.of(LinIntTag.of(4), LinIntTag.of(5)))
        )).hasIntStreamResultThat().containsExactly(4, 5).inOrder();
    }

    @Test
    void createIntListMakesIntArrayTag() {
        assertThat(OPS.createIntList(IntStream.of(4, 5))).isEqualTo(LinIntArrayTag.of(4, 5));
    }
}
