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
import org.enginehub.linbus.tree.LinIntTag;
import org.enginehub.linbus.tree.LinListTag;
import org.enginehub.linbus.tree.LinLongArrayTag;
import org.enginehub.linbus.tree.LinLongTag;
import org.enginehub.linbus.tree.LinTag;
import org.enginehub.linbus.tree.LinTagType;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.LongStream;

import static com.google.common.truth.Truth.assertThat;
import static org.enginehub.linbus.dfu.DataResultSubject.assertThat;

class LinOpsLongArrayTest extends AbstractLinOpsPrimitiveArrayTest {

    @Override
    LinTag<?> element(int value) {
        return LinLongTag.of(value);
    }

    @Override
    LinTag<?> arrayTag(int... values) {
        long[] array = new long[values.length];
        for (int i = 0; i < values.length; i++) {
            array[i] = values[i];
        }
        return LinLongArrayTag.of(array);
    }

    @Override
    LinTag<?> listOfElementTag(int... values) {
        List<LinLongTag> elements = new ArrayList<>();
        for (int value : values) {
            elements.add(LinLongTag.of(value));
        }
        return LinListTag.of(LinTagType.longTag(), elements);
    }

    @Override
    LinTag<?> foreignElement() {
        return LinIntTag.of(3);
    }

    @Override
    DataResult<?> readSequence(LinTag<?> input) {
        return OPS.getLongStream(input);
    }

    @Test
    void getLongStreamReadsLongArrayTag() {
        assertThat(OPS.getLongStream(LinLongArrayTag.of(1L, 2L))).hasLongStreamResultThat()
            .containsExactly(1L, 2L).inOrder();
    }

    @Test
    void getLongStreamReadsLongList() {
        assertThat(OPS.getLongStream(
            LinListTag.of(LinTagType.longTag(), List.of(LinLongTag.of(3L), LinLongTag.of(4L)))
        )).hasLongStreamResultThat().containsExactly(3L, 4L).inOrder();
    }

    @Test
    void createLongListMakesLongArrayTag() {
        assertThat(OPS.createLongList(LongStream.of(6L, 7L))).isEqualTo(LinLongArrayTag.of(6L, 7L));
    }
}
