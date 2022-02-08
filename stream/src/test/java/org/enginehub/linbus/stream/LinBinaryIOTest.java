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

import com.google.common.collect.ImmutableList;
import com.google.common.io.ByteStreams;
import org.enginehub.linbus.common.LinTagId;
import org.junit.jupiter.api.Test;

import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class LinBinaryIOTest {
    @Test
    void mustStartWithCompoundId() {
        var reader = LinBinaryIO.read(ByteStreams.newDataInput(new byte[]{(byte) LinTagId.BYTE.id()}));
        var ex = assertThrows(IllegalStateException.class, reader::next);
        assertThat(ex).hasMessageThat().isEqualTo("NBT stream does not start with a compound tag");
    }

    @Test
    void readUsing() throws IOException {
        var tokens = LinBinaryIO.readUsing(ByteStreams.newDataInput(new byte[]{
            (byte) LinTagId.COMPOUND.id(), // type id
            0, // name size (0)
            0,
            (byte) LinTagId.BYTE.id(), // type id
            0, // name size (0)
            0,
            1, // value
            0 // end tag
        }), ImmutableList::copyOf);
        assertThat(tokens).isNotNull();
        assertThat(tokens).isNotEmpty();
    }

    @Test
    void readUsingUnwrapsIoExceptions() {
        assertThrows(EOFException.class, () ->
            LinBinaryIO.readUsing(new DataInputStream(InputStream.nullInputStream()), Iterator::next)
        );
    }
}
