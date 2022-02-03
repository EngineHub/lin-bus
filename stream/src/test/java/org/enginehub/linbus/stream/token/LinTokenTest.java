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

package org.enginehub.linbus.stream.token;

import org.enginehub.linbus.common.LinTagId;
import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;

import static com.google.common.truth.Truth.assertThat;
import static com.google.common.truth.Truth8.assertThat;

public class LinTokenTest {
    @Test
    void checkSimpleValue() {
        assertThat(new LinToken.Name("foo").isSimpleValue()).isFalse();
        assertThat(new LinToken.ByteArrayStart().isSimpleValue()).isFalse();
        assertThat(new LinToken.ByteArrayContent(ByteBuffer.allocate(0).asReadOnlyBuffer()).isSimpleValue()).isFalse();
        assertThat(new LinToken.ByteArrayEnd().isSimpleValue()).isFalse();
        assertThat(new LinToken.Byte((byte) 1).isSimpleValue()).isTrue();
        assertThat(new LinToken.CompoundStart().isSimpleValue()).isFalse();
        assertThat(new LinToken.CompoundEnd().isSimpleValue()).isFalse();
        assertThat(new LinToken.Double(1).isSimpleValue()).isTrue();
        assertThat(new LinToken.Float(1).isSimpleValue()).isTrue();
        assertThat(new LinToken.IntArrayStart().isSimpleValue()).isFalse();
        assertThat(new LinToken.IntArrayContent(IntBuffer.allocate(0).asReadOnlyBuffer()).isSimpleValue()).isFalse();
        assertThat(new LinToken.IntArrayEnd().isSimpleValue()).isFalse();
        assertThat(new LinToken.Int(1).isSimpleValue()).isTrue();
        assertThat(new LinToken.LongArrayStart().isSimpleValue()).isFalse();
        assertThat(new LinToken.LongArrayContent(LongBuffer.allocate(0).asReadOnlyBuffer()).isSimpleValue()).isFalse();
        assertThat(new LinToken.LongArrayEnd().isSimpleValue()).isFalse();
        assertThat(new LinToken.Long(1).isSimpleValue()).isTrue();
        assertThat(new LinToken.Short((short) 1).isSimpleValue()).isTrue();
        assertThat(new LinToken.String("").isSimpleValue()).isTrue();
    }

    @Test
    void checkTagId() {
        assertThat(new LinToken.Name("foo").tagId()).isEmpty();
        assertThat(new LinToken.ByteArrayStart().tagId()).hasValue(LinTagId.BYTE_ARRAY);
        assertThat(new LinToken.ByteArrayContent(ByteBuffer.allocate(0).asReadOnlyBuffer()).tagId()).isEmpty();
        assertThat(new LinToken.ByteArrayEnd().tagId()).isEmpty();
        assertThat(new LinToken.Byte((byte) 1).tagId()).hasValue(LinTagId.BYTE);
        assertThat(new LinToken.CompoundStart().tagId()).hasValue(LinTagId.COMPOUND);
        assertThat(new LinToken.CompoundEnd().tagId()).isEmpty();
        assertThat(new LinToken.Double(1).tagId()).hasValue(LinTagId.DOUBLE);
        assertThat(new LinToken.Float(1).tagId()).hasValue(LinTagId.FLOAT);
        assertThat(new LinToken.IntArrayStart().tagId()).hasValue(LinTagId.INT_ARRAY);
        assertThat(new LinToken.IntArrayContent(IntBuffer.allocate(0).asReadOnlyBuffer()).tagId()).isEmpty();
        assertThat(new LinToken.IntArrayEnd().tagId()).isEmpty();
        assertThat(new LinToken.Int(1).tagId()).hasValue(LinTagId.INT);
        assertThat(new LinToken.LongArrayStart().tagId()).hasValue(LinTagId.LONG_ARRAY);
        assertThat(new LinToken.LongArrayContent(LongBuffer.allocate(0).asReadOnlyBuffer()).tagId()).isEmpty();
        assertThat(new LinToken.LongArrayEnd().tagId()).isEmpty();
        assertThat(new LinToken.Long(1).tagId()).hasValue(LinTagId.LONG);
        assertThat(new LinToken.Short((short) 1).tagId()).hasValue(LinTagId.SHORT);
        assertThat(new LinToken.String("").tagId()).hasValue(LinTagId.STRING);
    }
}
