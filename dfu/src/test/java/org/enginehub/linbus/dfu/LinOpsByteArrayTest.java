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
import org.enginehub.linbus.tree.LinByteArrayTag;
import org.enginehub.linbus.tree.LinByteTag;
import org.enginehub.linbus.tree.LinIntTag;
import org.enginehub.linbus.tree.LinListTag;
import org.enginehub.linbus.tree.LinTag;
import org.enginehub.linbus.tree.LinTagType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import static com.google.common.truth.Truth.assertThat;
import static org.enginehub.linbus.dfu.DataResultSubject.assertThat;

class LinOpsByteArrayTest extends AbstractLinOpsPrimitiveArrayTest {

    @Override
    LinTag<?> element(int value) {
        return LinByteTag.of((byte) value);
    }

    @Override
    LinTag<?> arrayTag(int... values) {
        byte[] array = new byte[values.length];
        for (int i = 0; i < values.length; i++) {
            array[i] = (byte) values[i];
        }
        return LinByteArrayTag.of(array);
    }

    @Override
    LinTag<?> listOfElementTag(int... values) {
        List<LinByteTag> elements = new ArrayList<>();
        for (int value : values) {
            elements.add(LinByteTag.of((byte) value));
        }
        return LinListTag.of(LinTagType.byteTag(), elements);
    }

    @Override
    LinTag<?> foreignElement() {
        return LinIntTag.of(3);
    }

    @Override
    DataResult<?> readSequence(LinTag<?> input) {
        return OPS.getByteBuffer(input);
    }

    @Test
    void getByteBufferReadsByteArrayTag() {
        assertThat(OPS.getByteBuffer(LinByteArrayTag.of((byte) 1, (byte) 2)))
            .hasResultThat().isEqualTo(ByteBuffer.wrap(new byte[]{1, 2}));
    }

    @Test
    void getByteBufferReadsByteList() {
        assertThat(OPS.getByteBuffer(
            LinListTag.of(LinTagType.byteTag(), List.of(LinByteTag.of((byte) 3), LinByteTag.of((byte) 4)))
        )).hasResultThat().isEqualTo(ByteBuffer.wrap(new byte[]{3, 4}));
    }

    @Test
    void createByteListMakesByteArrayTag() {
        assertThat(OPS.createByteList(ByteBuffer.wrap(new byte[]{1, 2, 3})))
            .isEqualTo(LinByteArrayTag.of((byte) 1, (byte) 2, (byte) 3));
    }

    @Test
    @NbtOpsBehavior
    @DisplayName("createByteList copies the whole backing buffer, ignoring position and limit")
    void createByteListIgnoresBufferPosition() {
        ByteBuffer buffer = ByteBuffer.wrap(new byte[]{1, 2, 3}).position(1).limit(2);
        assertThat(OPS.createByteList(buffer)).isEqualTo(LinByteArrayTag.of((byte) 1, (byte) 2, (byte) 3));
    }
}
