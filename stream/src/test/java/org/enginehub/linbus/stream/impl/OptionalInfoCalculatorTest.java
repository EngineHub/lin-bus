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

import com.google.common.collect.ImmutableList;
import org.enginehub.linbus.common.LinTagId;
import org.enginehub.linbus.stream.LinStream;
import org.enginehub.linbus.stream.exception.NbtParseException;
import org.enginehub.linbus.stream.token.LinToken;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.util.Optional;
import java.util.OptionalInt;

import static com.google.common.truth.Truth.assertThat;
import static org.enginehub.linbus.stream.StreamTestUtil.convertNbtStream;
import static org.enginehub.linbus.stream.StreamTestUtil.streamFromIterator;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class OptionalInfoCalculatorTest {
    @Test
    void passthrough() throws IOException {
        var originalTokens = convertNbtStream("bigtest.nbt.gz", s -> ImmutableList.copyOf(s.asIterator()));
        var result = ImmutableList.copyOf(streamFromIterator(originalTokens.iterator()).calculateOptionalInfo().asIterator());
        assertThat(result).containsExactlyElementsIn(originalTokens).inOrder();
    }

    @Test
    void failToFill() {
        var ex = assertThrows(NbtParseException.class, () ->
            LinStream.of(new LinToken.Name("foo")).calculateOptionalInfo().nextOrNull()
        );
        assertThat(ex).hasMessageThat().isEqualTo("Optional value not filled by the end of token stream");
    }

    @Test
    void fillName() {
        var result = ImmutableList.copyOf(LinStream.of(
            new LinToken.Name("foo"), new LinToken.Int(1)
        ).calculateOptionalInfo().asIterator());
        assertThat(result).containsExactly(new LinToken.Name("foo", LinTagId.INT), new LinToken.Int(1)).inOrder();
    }

    @Test
    void fillNameInvalidFollowingValue() {
        var iterator = LinStream.of(
            new LinToken.Name("foo"),
            new LinToken.ByteArrayEnd()
        ).calculateOptionalInfo();
        var ex = assertThrows(NbtParseException.class, iterator::nextOrNull);
        assertThat(ex).hasMessageThat().isEqualTo("Token doesn't represent a tag directly: " + new LinToken.ByteArrayEnd());
    }

    @Test
    void fillListStart() {
        var result = ImmutableList.copyOf(LinStream.of(
            new LinToken.ListStart(),
            new LinToken.Int(1),
            new LinToken.ListEnd()
        ).calculateOptionalInfo().asIterator());
        assertThat(result).containsExactly(
            new LinToken.ListStart(1, LinTagId.INT),
            new LinToken.Int(1),
            new LinToken.ListEnd()
        ).inOrder();
    }

    @Test
    void fillListStartEmpty() {
        var result = ImmutableList.copyOf(LinStream.of(
            new LinToken.ListStart(),
            new LinToken.ListEnd()
        ).calculateOptionalInfo().asIterator());
        assertThat(result).containsExactly(
            new LinToken.ListStart(0, LinTagId.END),
            new LinToken.ListEnd()
        ).inOrder();
    }

    @Test
    void fillListStartSizeGiven() {
        var result = ImmutableList.copyOf(LinStream.of(
            new LinToken.ListStart(OptionalInt.of(1), Optional.empty()),
            new LinToken.Int(1)
        ).calculateOptionalInfo().asIterator());
        assertThat(result).containsExactly(
            new LinToken.ListStart(1, LinTagId.INT),
            new LinToken.Int(1)
        ).inOrder();
    }

    @Test
    void fillListStartElementIdGiven() {
        var result = ImmutableList.copyOf(LinStream.of(
            new LinToken.ListStart(OptionalInt.empty(), Optional.of(LinTagId.INT)),
            new LinToken.Int(1),
            new LinToken.ListEnd()
        ).calculateOptionalInfo().asIterator());
        assertThat(result).containsExactly(
            new LinToken.ListStart(1, LinTagId.INT),
            new LinToken.Int(1),
            new LinToken.ListEnd()
        ).inOrder();
    }

    @Test
    void fillListStartInvalidFollowingValue() {
        var iterator = LinStream.of(
            new LinToken.ListStart(OptionalInt.of(1), Optional.empty()),
            new LinToken.ByteArrayEnd()
        ).calculateOptionalInfo();
        var ex = assertThrows(NbtParseException.class, iterator::nextOrNull);
        assertThat(ex).hasMessageThat().isEqualTo("Token doesn't represent a tag directly: " + new LinToken.ByteArrayEnd());
    }

    @Test
    void fillByteArrayStart() {
        var buffer = ByteBuffer.allocate(1024).asReadOnlyBuffer();
        var result = ImmutableList.copyOf(LinStream.of(
            new LinToken.ByteArrayStart(),
            new LinToken.ByteArrayContent(buffer),
            new LinToken.ByteArrayEnd()
        ).calculateOptionalInfo().asIterator());
        assertThat(result).containsExactly(
            new LinToken.ByteArrayStart(buffer.capacity()),
            new LinToken.ByteArrayContent(buffer),
            new LinToken.ByteArrayEnd()
        ).inOrder();
    }

    @Test
    void fillByteArrayStartEmpty() {
        var result = ImmutableList.copyOf(LinStream.of(
            new LinToken.ByteArrayStart(),
            new LinToken.ByteArrayEnd()
        ).calculateOptionalInfo().asIterator());
        assertThat(result).containsExactly(
            new LinToken.ByteArrayStart(0),
            new LinToken.ByteArrayEnd()
        ).inOrder();
    }

    @Test
    void fillByteArrayStartWithJunk() {
        LinToken.String junkToken = new LinToken.String("junk 😎");
        var iterator = LinStream.of(
            new LinToken.ByteArrayStart(),
            junkToken
        ).calculateOptionalInfo();

        var ex = assertThrows(NbtParseException.class, iterator::nextOrNull);
        assertThat(ex).hasMessageThat().isEqualTo("Unexpected token: " + junkToken);
    }

    @Test
    void fillIntArrayStart() {
        var buffer = IntBuffer.allocate(1024).asReadOnlyBuffer();
        var result = ImmutableList.copyOf(LinStream.of(
            new LinToken.IntArrayStart(),
            new LinToken.IntArrayContent(buffer),
            new LinToken.IntArrayEnd()
        ).calculateOptionalInfo().asIterator());
        assertThat(result).containsExactly(
            new LinToken.IntArrayStart(buffer.capacity()),
            new LinToken.IntArrayContent(buffer),
            new LinToken.IntArrayEnd()
        ).inOrder();
    }

    @Test
    void fillIntArrayStartEmpty() {
        var result = ImmutableList.copyOf(LinStream.of(
            new LinToken.IntArrayStart(),
            new LinToken.IntArrayEnd()
        ).calculateOptionalInfo().asIterator());
        assertThat(result).containsExactly(
            new LinToken.IntArrayStart(0),
            new LinToken.IntArrayEnd()
        ).inOrder();
    }

    @Test
    void fillIntArrayStartWithJunk() {
        LinToken.String junkToken = new LinToken.String("junk 😎");
        var iterator = LinStream.of(
            new LinToken.IntArrayStart(),
            junkToken
        ).calculateOptionalInfo();

        var ex = assertThrows(NbtParseException.class, iterator::nextOrNull);
        assertThat(ex).hasMessageThat().isEqualTo("Unexpected token: " + junkToken);
    }

    @Test
    void fillLongArrayStart() {
        var buffer = LongBuffer.allocate(1024).asReadOnlyBuffer();
        var result = ImmutableList.copyOf(LinStream.of(
            new LinToken.LongArrayStart(),
            new LinToken.LongArrayContent(buffer),
            new LinToken.LongArrayEnd()
        ).calculateOptionalInfo().asIterator());
        assertThat(result).containsExactly(
            new LinToken.LongArrayStart(buffer.capacity()),
            new LinToken.LongArrayContent(buffer),
            new LinToken.LongArrayEnd()
        ).inOrder();
    }

    @Test
    void fillLongArrayStartEmpty() {
        var result = ImmutableList.copyOf(LinStream.of(
            new LinToken.LongArrayStart(),
            new LinToken.LongArrayEnd()
        ).calculateOptionalInfo().asIterator());
        assertThat(result).containsExactly(
            new LinToken.LongArrayStart(0),
            new LinToken.LongArrayEnd()
        ).inOrder();
    }

    @Test
    void fillLongArrayStartWithJunk() {
        LinToken.String junkToken = new LinToken.String("junk 😎");
        var iterator = LinStream.of(
            new LinToken.LongArrayStart(),
            junkToken
        ).calculateOptionalInfo();

        var ex = assertThrows(NbtParseException.class, iterator::nextOrNull);
        assertThat(ex).hasMessageThat().isEqualTo("Unexpected token: " + junkToken);
    }

    @Test
    void fillRecursive() {
        var result = ImmutableList.copyOf(LinStream.of(
            new LinToken.ListStart(),
            new LinToken.ListStart(),
            new LinToken.Int(1),
            new LinToken.ListEnd(),
            new LinToken.ListStart(),
            new LinToken.Int(2),
            new LinToken.ListEnd(),
            new LinToken.ListStart(),
            new LinToken.Int(3),
            new LinToken.ListEnd(),
            new LinToken.ListEnd()
        ).calculateOptionalInfo().asIterator());
        assertThat(result).containsExactly(
            new LinToken.ListStart(3, LinTagId.LIST),
            new LinToken.ListStart(1, LinTagId.INT),
            new LinToken.Int(1),
            new LinToken.ListEnd(),
            new LinToken.ListStart(1, LinTagId.INT),
            new LinToken.Int(2),
            new LinToken.ListEnd(),
            new LinToken.ListStart(1, LinTagId.INT),
            new LinToken.Int(3),
            new LinToken.ListEnd(),
            new LinToken.ListEnd()
        ).inOrder();
    }
}
