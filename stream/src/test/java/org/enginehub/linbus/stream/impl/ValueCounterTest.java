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

package org.enginehub.linbus.stream.impl;

import org.enginehub.linbus.stream.token.LinToken;
import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.util.List;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class ValueCounterTest {
    private final ValueCounter counter = new ValueCounter();

    @Test
    void countsSimpleValues() {
        var tokens = List.of(
            new LinToken.Byte((byte) 1),
            new LinToken.Double(1.0),
            new LinToken.Float(1.0f),
            new LinToken.Int(1),
            new LinToken.Long(1L),
            new LinToken.Short((short) 1),
            new LinToken.String("a")
        );
        assertThat(counter.count()).isEqualTo(0);
        assertThat(counter.isNested()).isFalse();
        for (int i = 0; i < tokens.size(); i++) {
            counter.add(tokens.get(i));
            assertThat(counter.count()).isEqualTo(i + 1);
            assertThat(counter.isNested()).isFalse();
        }
    }

    @Test
    void countsCompoundOnly() {
        counter.add(new LinToken.CompoundStart());
        assertThat(counter.count()).isEqualTo(0);
        assertThat(counter.isNested()).isTrue();
        counter.add(new LinToken.Int(0));
        assertThat(counter.count()).isEqualTo(0);
        counter.add(new LinToken.CompoundEnd());
        assertThat(counter.count()).isEqualTo(1);
        assertThat(counter.isNested()).isFalse();
    }

    @Test
    void countCompoundWithCompoundInside() {
        counter.add(new LinToken.CompoundStart());
        counter.add(new LinToken.CompoundStart());
        assertThat(counter.count()).isEqualTo(0);
        assertThat(counter.isNested()).isTrue();
        counter.add(new LinToken.Int(0));
        assertThat(counter.count()).isEqualTo(0);
        counter.add(new LinToken.CompoundEnd());
        assertThat(counter.isNested()).isTrue();
        counter.add(new LinToken.CompoundEnd());
        assertThat(counter.count()).isEqualTo(1);
        assertThat(counter.isNested()).isFalse();
    }

    @Test
    void countCompoundWithListInside() {
        counter.add(new LinToken.CompoundStart());
        counter.add(new LinToken.ListStart());
        assertThat(counter.count()).isEqualTo(0);
        assertThat(counter.isNested()).isTrue();
        counter.add(new LinToken.Int(0));
        assertThat(counter.count()).isEqualTo(0);
        counter.add(new LinToken.ListEnd());
        assertThat(counter.isNested()).isTrue();
        counter.add(new LinToken.CompoundEnd());
        assertThat(counter.count()).isEqualTo(1);
        assertThat(counter.isNested()).isFalse();
    }

    @Test
    void countMalformedCompound() {
        var ex = assertThrows(IllegalStateException.class, () ->
            counter.add(new LinToken.CompoundEnd())
        );
        assertThat(ex).hasMessageThat().isEqualTo("Compound end without start");
    }

    @Test
    void countList() {
        counter.add(new LinToken.ListStart());
        assertThat(counter.count()).isEqualTo(0);
        assertThat(counter.isNested()).isTrue();
        counter.add(new LinToken.Int(0));
        assertThat(counter.count()).isEqualTo(0);
        counter.add(new LinToken.ListEnd());
        assertThat(counter.count()).isEqualTo(1);
        assertThat(counter.isNested()).isFalse();
    }

    @Test
    void countListWithListInside() {
        counter.add(new LinToken.ListStart());
        counter.add(new LinToken.ListStart());
        assertThat(counter.count()).isEqualTo(0);
        assertThat(counter.isNested()).isTrue();
        counter.add(new LinToken.Int(0));
        assertThat(counter.count()).isEqualTo(0);
        counter.add(new LinToken.ListEnd());
        assertThat(counter.isNested()).isTrue();
        counter.add(new LinToken.ListEnd());
        assertThat(counter.count()).isEqualTo(1);
        assertThat(counter.isNested()).isFalse();
    }

    @Test
    void countListWithCompoundInside() {
        counter.add(new LinToken.ListStart());
        counter.add(new LinToken.CompoundStart());
        assertThat(counter.count()).isEqualTo(0);
        assertThat(counter.isNested()).isTrue();
        counter.add(new LinToken.Int(0));
        assertThat(counter.count()).isEqualTo(0);
        counter.add(new LinToken.CompoundEnd());
        assertThat(counter.isNested()).isTrue();
        counter.add(new LinToken.ListEnd());
        assertThat(counter.count()).isEqualTo(1);
        assertThat(counter.isNested()).isFalse();
    }

    @Test
    void countMalformedList() {
        var ex = assertThrows(IllegalStateException.class, () ->
            counter.add(new LinToken.ListEnd())
        );
        assertThat(ex).hasMessageThat().isEqualTo("List end without start");
    }

    @Test
    void countByteArray() {
        counter.add(new LinToken.ByteArrayStart());
        assertThat(counter.count()).isEqualTo(0);
        assertThat(counter.isNested()).isFalse();
        counter.add(new LinToken.ByteArrayContent(ByteBuffer.allocate(0).asReadOnlyBuffer()));
        counter.add(new LinToken.ByteArrayEnd());
        assertThat(counter.count()).isEqualTo(1);
        assertThat(counter.isNested()).isFalse();
    }

    @Test
    void countListWithByteArrayInside() {
        counter.add(new LinToken.ListStart());
        counter.add(new LinToken.ByteArrayStart());
        assertThat(counter.count()).isEqualTo(0);
        assertThat(counter.isNested()).isTrue();
        counter.add(new LinToken.ByteArrayContent(ByteBuffer.allocate(0).asReadOnlyBuffer()));
        counter.add(new LinToken.ByteArrayEnd());
        assertThat(counter.count()).isEqualTo(0);
        counter.add(new LinToken.ListEnd());
        assertThat(counter.count()).isEqualTo(1);
        assertThat(counter.isNested()).isFalse();
    }

    @Test
    void countMalformedByteArray() {
        var ex = assertThrows(IllegalStateException.class, () ->
            counter.add(new LinToken.ByteArrayEnd())
        );
        assertThat(ex).hasMessageThat().isEqualTo("Byte array end without start");
    }

    @Test
    void countIntArray() {
        counter.add(new LinToken.IntArrayStart());
        assertThat(counter.count()).isEqualTo(0);
        assertThat(counter.isNested()).isFalse();
        counter.add(new LinToken.IntArrayContent(IntBuffer.allocate(0).asReadOnlyBuffer()));
        counter.add(new LinToken.IntArrayEnd());
        assertThat(counter.count()).isEqualTo(1);
        assertThat(counter.isNested()).isFalse();
    }

    @Test
    void countListWithIntArrayInside() {
        counter.add(new LinToken.ListStart());
        counter.add(new LinToken.IntArrayStart());
        assertThat(counter.count()).isEqualTo(0);
        assertThat(counter.isNested()).isTrue();
        counter.add(new LinToken.IntArrayContent(IntBuffer.allocate(0).asReadOnlyBuffer()));
        counter.add(new LinToken.IntArrayEnd());
        assertThat(counter.count()).isEqualTo(0);
        counter.add(new LinToken.ListEnd());
        assertThat(counter.count()).isEqualTo(1);
        assertThat(counter.isNested()).isFalse();
    }

    @Test
    void countMalformedIntArray() {
        var ex = assertThrows(IllegalStateException.class, () ->
            counter.add(new LinToken.IntArrayEnd())
        );
        assertThat(ex).hasMessageThat().isEqualTo("Int array end without start");
    }

    @Test
    void countLongArray() {
        counter.add(new LinToken.LongArrayStart());
        assertThat(counter.count()).isEqualTo(0);
        assertThat(counter.isNested()).isFalse();
        counter.add(new LinToken.LongArrayContent(LongBuffer.allocate(0).asReadOnlyBuffer()));
        counter.add(new LinToken.LongArrayEnd());
        assertThat(counter.count()).isEqualTo(1);
        assertThat(counter.isNested()).isFalse();
    }

    @Test
    void countListWithLongArrayInside() {
        counter.add(new LinToken.ListStart());
        counter.add(new LinToken.LongArrayStart());
        assertThat(counter.count()).isEqualTo(0);
        assertThat(counter.isNested()).isTrue();
        counter.add(new LinToken.LongArrayContent(LongBuffer.allocate(0).asReadOnlyBuffer()));
        counter.add(new LinToken.LongArrayEnd());
        assertThat(counter.count()).isEqualTo(0);
        counter.add(new LinToken.ListEnd());
        assertThat(counter.count()).isEqualTo(1);
        assertThat(counter.isNested()).isFalse();
    }

    @Test
    void countMalformedLongArray() {
        var ex = assertThrows(IllegalStateException.class, () ->
            counter.add(new LinToken.LongArrayEnd())
        );
        assertThat(ex).hasMessageThat().isEqualTo("Long array end without start");
    }
}
