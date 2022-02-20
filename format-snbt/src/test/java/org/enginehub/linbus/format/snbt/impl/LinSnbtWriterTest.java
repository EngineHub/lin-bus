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

package org.enginehub.linbus.format.snbt.impl;

import org.enginehub.linbus.common.LinTagId;
import org.enginehub.linbus.stream.LinStream;
import org.enginehub.linbus.stream.LinStreamable;
import org.enginehub.linbus.stream.token.LinToken;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.util.List;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class LinSnbtWriterTest {
    private static String ezToString(LinStreamable tokens) {
        var builder = new StringBuilder();
        try {
            new LinSnbtWriter().write(builder, tokens.linStream());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return builder.toString();
    }

    @Test
    void writeWithoutPrefixedCompound() {
        var ex = assertThrows(IllegalStateException.class, () -> ezToString(LinStream.of(new LinToken.Name("foo"))));
        assertThat(ex).hasMessageThat().isEqualTo("Names can only appear inside compounds");
    }

    @Test
    void writeNameInList() {
        var ex = assertThrows(IllegalStateException.class, () -> ezToString(LinStream.of(
            new LinToken.ListStart(1, LinTagId.STRING),
            new LinToken.Name("foo")
        )));
        assertThat(ex).hasMessageThat().isEqualTo("Names can only appear inside compounds");
    }

    @Test
    void emptyByteArrayOutput() {
        var output = ezToString(LinStream.of(
            new LinToken.CompoundStart(),
            new LinToken.Name("foo"),
            new LinToken.ByteArrayStart(),
            new LinToken.ByteArrayEnd(),
            new LinToken.CompoundEnd()
        ));
        assertThat(output).isEqualTo("{foo:[B;]}");
    }

    @Test
    void byteArrayOutput() {
        var output = ezToString(LinStream.of(
            new LinToken.CompoundStart(),
            new LinToken.Name("foo"),
            new LinToken.ByteArrayStart(),
            new LinToken.ByteArrayContent(ByteBuffer.wrap(new byte[]{1, 2, 3}).asReadOnlyBuffer()),
            new LinToken.ByteArrayEnd(),
            new LinToken.CompoundEnd()
        ));
        assertThat(output).isEqualTo("{foo:[B;1B,2B,3B]}");
    }

    @Test
    void byteArrayOutputSplit() {
        var output = ezToString(LinStream.of(
            new LinToken.CompoundStart(),
            new LinToken.Name("foo"),
            new LinToken.ByteArrayStart(),
            new LinToken.ByteArrayContent(ByteBuffer.wrap(new byte[]{1, 2, 3}).asReadOnlyBuffer()),
            new LinToken.ByteArrayContent(ByteBuffer.wrap(new byte[]{4, 5, 6}).asReadOnlyBuffer()),
            new LinToken.ByteArrayEnd(),
            new LinToken.CompoundEnd()
        ));
        assertThat(output).isEqualTo("{foo:[B;1B,2B,3B,4B,5B,6B]}");
    }

    @Test
    void emptyIntArrayOutput() {
        var output = ezToString(LinStream.of(
            new LinToken.CompoundStart(),
            new LinToken.Name("foo"),
            new LinToken.IntArrayStart(),
            new LinToken.IntArrayEnd(),
            new LinToken.CompoundEnd()
        ));
        assertThat(output).isEqualTo("{foo:[I;]}");
    }

    @Test
    void intArrayOutput() {
        var output = ezToString(LinStream.of(
            new LinToken.CompoundStart(),
            new LinToken.Name("foo"),
            new LinToken.IntArrayStart(),
            new LinToken.IntArrayContent(IntBuffer.wrap(new int[]{1, 2, 3}).asReadOnlyBuffer()),
            new LinToken.IntArrayEnd(),
            new LinToken.CompoundEnd()
        ));
        assertThat(output).isEqualTo("{foo:[I;1,2,3]}");
    }

    @Test
    void intArrayOutputSplit() {
        var output = ezToString(LinStream.of(
            new LinToken.CompoundStart(),
            new LinToken.Name("foo"),
            new LinToken.IntArrayStart(),
            new LinToken.IntArrayContent(IntBuffer.wrap(new int[]{1, 2, 3}).asReadOnlyBuffer()),
            new LinToken.IntArrayContent(IntBuffer.wrap(new int[]{4, 5, 6}).asReadOnlyBuffer()),
            new LinToken.IntArrayEnd(),
            new LinToken.CompoundEnd()
        ));
        assertThat(output).isEqualTo("{foo:[I;1,2,3,4,5,6]}");
    }

    @Test
    void emptyLongArrayOutput() {
        var output = ezToString(LinStream.of(
            new LinToken.CompoundStart(),
            new LinToken.Name("foo"),
            new LinToken.LongArrayStart(),
            new LinToken.LongArrayEnd(),
            new LinToken.CompoundEnd()
        ));
        assertThat(output).isEqualTo("{foo:[L;]}");
    }

    @Test
    void longArrayOutput() {
        var output = ezToString(LinStream.of(
            new LinToken.CompoundStart(),
            new LinToken.Name("foo"),
            new LinToken.LongArrayStart(),
            new LinToken.LongArrayContent(LongBuffer.wrap(new long[]{1, 2, 3}).asReadOnlyBuffer()),
            new LinToken.LongArrayEnd(),
            new LinToken.CompoundEnd()
        ));
        assertThat(output).isEqualTo("{foo:[L;1L,2L,3L]}");
    }

    @Test
    void longArrayOutputSplit() {
        var output = ezToString(LinStream.of(
            new LinToken.CompoundStart(),
            new LinToken.Name("foo"),
            new LinToken.LongArrayStart(),
            new LinToken.LongArrayContent(LongBuffer.wrap(new long[]{1, 2, 3}).asReadOnlyBuffer()),
            new LinToken.LongArrayContent(LongBuffer.wrap(new long[]{4, 5, 6}).asReadOnlyBuffer()),
            new LinToken.LongArrayEnd(),
            new LinToken.CompoundEnd()
        ));
        assertThat(output).isEqualTo("{foo:[L;1L,2L,3L,4L,5L,6L]}");
    }
}
