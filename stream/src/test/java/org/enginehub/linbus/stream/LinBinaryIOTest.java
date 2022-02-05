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

package org.enginehub.linbus.stream;

import com.google.common.io.ByteStreams;
import org.enginehub.linbus.common.LinTagId;
import org.junit.jupiter.api.Test;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class LinBinaryIOTest {
    @Test
    void mustStartWithCompoundId() {
        var reader = LinBinaryIO.read(ByteStreams.newDataInput(new byte[]{(byte) LinTagId.BYTE.id()}));
        var ex = assertThrows(IllegalStateException.class, reader::next);
        assertThat(ex).hasMessageThat().isEqualTo("NBT stream does not start with a compound tag");
    }
}
