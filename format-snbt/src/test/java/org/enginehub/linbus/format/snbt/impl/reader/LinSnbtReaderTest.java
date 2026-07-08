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

package org.enginehub.linbus.format.snbt.impl.reader;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import org.enginehub.linbus.stream.exception.NbtParseException;
import org.enginehub.linbus.stream.token.LinToken;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.StringReader;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;

public class LinSnbtReaderTest {
    private static LinSnbtReader ezStringRead(String input) {
        return new LinSnbtReader(new LinSnbtTokenizer(new StringReader(input)));
    }

    private static String atCharacterIndex(int charIndex) {
        return "At character index " + charIndex + ": ";
    }

    @Test
    void tooShortInput() {
        var reader = ezStringRead("");
        var ex = assertThrows(NbtParseException.class, reader::nextOrNull);
        assertThat(ex).hasMessageThat().isEqualTo(atCharacterIndex(0) + "Unexpected end of input");
    }

    @Test
    void mustHaveRootCompound() {
        var reader = ezStringRead("[]");
        var ex = assertThrows(NbtParseException.class, reader::nextOrNull);
        assertThat(ex).hasMessageThat().isEqualTo(atCharacterIndex(0) + "Unexpected token: '[', expected '{'");
    }

    @Test
    void invalidCharacterInSimpleValue() throws IOException {
        var reader = ezStringRead("{a:@}");
        assertThat(reader.nextOrNull()).isEqualTo(new LinToken.CompoundStart());
        assertThat(reader.nextOrNull()).isEqualTo(new LinToken.Name("a"));
        var ex = assertThrows(NbtParseException.class, reader::nextOrNull);
        assertThat(ex).hasMessageThat().isEqualTo(atCharacterIndex(3) + "Unexpected character: @");
    }

    @Test
    void invalidCharacterAfterSimpleValue() throws IOException {
        var reader = ezStringRead("{a:;}");
        assertThat(reader.nextOrNull()).isEqualTo(new LinToken.CompoundStart());
        assertThat(reader.nextOrNull()).isEqualTo(new LinToken.Name("a"));
        var ex = assertThrows(NbtParseException.class, reader::nextOrNull);
        assertThat(ex).hasMessageThat().isEqualTo(atCharacterIndex(3) + "Unexpected token: ';'");
    }

    @Test
    void emptyRootCompound() {
        var list = ImmutableList.copyOf(ezStringRead("{}").asIterator());
        assertThat(list).containsExactly(new LinToken.CompoundStart(), new LinToken.CompoundEnd()).inOrder();

        list = ImmutableList.copyOf(ezStringRead("{         }").asIterator());
        assertThat(list).containsExactly(new LinToken.CompoundStart(), new LinToken.CompoundEnd()).inOrder();
    }

    @Test
    void emptyNestedCompound() {
        var list = ImmutableList.copyOf(ezStringRead("{nested:{}}").asIterator());
        assertThat(list).containsExactly(
            new LinToken.CompoundStart(),
            new LinToken.Name("nested"),
                new LinToken.CompoundStart(),
                new LinToken.CompoundEnd(),
            new LinToken.CompoundEnd()
        ).inOrder();
    }

    @Test
    void simpleValueWithWhitespace() {
        var list = ImmutableList.copyOf(ezStringRead("{a:b      }").asIterator());
        assertThat(list).containsExactly(
            new LinToken.CompoundStart(),
            new LinToken.Name("a"),
            new LinToken.String("b"),
            new LinToken.CompoundEnd()
        ).inOrder();

        list = ImmutableList.copyOf(ezStringRead("{a:[b      , c ]}").asIterator());
        assertThat(list).containsExactly(
            new LinToken.CompoundStart(),
            new LinToken.Name("a"),
            new LinToken.ListStart(),
            new LinToken.String("b"),
            new LinToken.String("c"),
            new LinToken.ListEnd(),
            new LinToken.CompoundEnd()
        ).inOrder();
    }

    @Test
    void stringThatLooksLikeALong() {
        var list = ImmutableList.copyOf(ezStringRead("{a:AL}").asIterator());
        assertThat(list).containsExactly(
            new LinToken.CompoundStart(),
            new LinToken.Name("a"),
            new LinToken.String("AL"),
            new LinToken.CompoundEnd()
        ).inOrder();
    }

    @Test
    void stringThatLooksLikeAFloat() {
        var list = ImmutableList.copyOf(ezStringRead("{a:AF}").asIterator());
        assertThat(list).containsExactly(
            new LinToken.CompoundStart(),
            new LinToken.Name("a"),
            new LinToken.String("AF"),
            new LinToken.CompoundEnd()
        ).inOrder();
    }

    @Test
    void implicitDoubleValue() {
        var list = ImmutableList.copyOf(ezStringRead("{a:1.0}").asIterator());
        assertThat(list).containsExactly(
            new LinToken.CompoundStart(),
            new LinToken.Name("a"),
            new LinToken.Double(1.0),
            new LinToken.CompoundEnd()
        ).inOrder();
    }

    @Test
    void booleanValue() {
        assertAll(() -> {
            var list = ImmutableList.copyOf(ezStringRead("{a:true}").asIterator());
            assertThat(list).containsExactly(
                new LinToken.CompoundStart(),
                new LinToken.Name("a"),
                new LinToken.Byte((byte) 1),
                new LinToken.CompoundEnd()
            ).inOrder();
        }, () -> {
            var list = ImmutableList.copyOf(ezStringRead("{a:false}").asIterator());
            assertThat(list).containsExactly(
                new LinToken.CompoundStart(),
                new LinToken.Name("a"),
                new LinToken.Byte((byte) 0),
                new LinToken.CompoundEnd()
            ).inOrder();
        });
    }

    @Test
    void nameWithWhitespace() {
        var list = ImmutableList.copyOf(ezStringRead("{ a :b,c  :d}").asIterator());
        assertThat(list).containsExactly(
            new LinToken.CompoundStart(),
            new LinToken.Name("a"),
            new LinToken.String("b"),
            new LinToken.Name("c"),
            new LinToken.String("d"),
            new LinToken.CompoundEnd()
        ).inOrder();
    }

    @Test
    void badName() throws IOException {
        var reader = ezStringRead("{;");
        assertThat(reader.nextOrNull()).isEqualTo(new LinToken.CompoundStart());
        var ex = assertThrows(NbtParseException.class, reader::nextOrNull);
        assertThat(ex).hasMessageThat().isEqualTo(atCharacterIndex(1) + "Unexpected token: ';'");
    }

    @Test
    void badNameEnd() throws IOException {
        var reader = ezStringRead("{'a';");
        assertThat(reader.nextOrNull()).isEqualTo(new LinToken.CompoundStart());
        var ex = assertThrows(NbtParseException.class, reader::nextOrNull);
        assertThat(ex).hasMessageThat().isEqualTo(atCharacterIndex(4) + "Unexpected token: ';', expected ':'");
    }

    @Test
    void badCompoundEnd() throws IOException {
        var reader = ezStringRead("{a:'@'b");
        assertThat(reader.nextOrNull()).isEqualTo(new LinToken.CompoundStart());
        assertThat(reader.nextOrNull()).isEqualTo(new LinToken.Name("a"));
        assertThat(reader.nextOrNull()).isEqualTo(new LinToken.String("@"));
        var ex = assertThrows(NbtParseException.class, reader::nextOrNull);
        assertThat(ex).hasMessageThat().isEqualTo(atCharacterIndex(6) + "Unexpected end of input");
    }

    @Test
    void badListEnd() throws IOException {
        var reader = ezStringRead("{a:['@'}}");
        assertThat(reader.nextOrNull()).isEqualTo(new LinToken.CompoundStart());
        assertThat(reader.nextOrNull()).isEqualTo(new LinToken.Name("a"));
        assertThat(reader.nextOrNull()).isEqualTo(new LinToken.ListStart());
        assertThat(reader.nextOrNull()).isEqualTo(new LinToken.String("@"));
        var ex = assertThrows(NbtParseException.class, reader::nextOrNull);
        assertThat(ex).hasMessageThat().isEqualTo(atCharacterIndex(7) + "Unexpected token: '}'");
    }

    @Test
    void badArrayType() throws IOException {
        var reader = ezStringRead("{a:[f;]}");
        assertThat(reader.nextOrNull()).isEqualTo(new LinToken.CompoundStart());
        assertThat(reader.nextOrNull()).isEqualTo(new LinToken.Name("a"));
        var ex = assertThrows(NbtParseException.class, reader::nextOrNull);
        assertThat(ex).hasMessageThat().isEqualTo(atCharacterIndex(5) + "Invalid array type: f");
    }

    @Test
    void badByteArrayContent() throws IOException {
        var reader = ezStringRead("{a:[B;lmao_gottem]}");
        assertThat(reader.nextOrNull()).isEqualTo(new LinToken.CompoundStart());
        assertThat(reader.nextOrNull()).isEqualTo(new LinToken.Name("a"));
        assertThat(reader.nextOrNull()).isEqualTo(new LinToken.ByteArrayStart());
        var ex = assertThrows(NbtParseException.class, reader::nextOrNull);
        assertThat(ex).hasMessageThat().isEqualTo(atCharacterIndex(6) + "Expected Byte token, got String[value=lmao_gottem]");
    }

    @Test
    void badByteArrayContentByLowLevelToken() throws IOException {
        var reader = ezStringRead("{a:[B;;]}");
        assertThat(reader.nextOrNull()).isEqualTo(new LinToken.CompoundStart());
        assertThat(reader.nextOrNull()).isEqualTo(new LinToken.Name("a"));
        assertThat(reader.nextOrNull()).isEqualTo(new LinToken.ByteArrayStart());
        var ex = assertThrows(NbtParseException.class, reader::nextOrNull);
        assertThat(ex).hasMessageThat().isEqualTo(atCharacterIndex(6) + "Unexpected token: ';', expected Text");
    }

    @Test
    void badByteArraySeparator() throws IOException {
        var reader = ezStringRead("{a:[B;1b}2b]}");
        assertThat(reader.nextOrNull()).isEqualTo(new LinToken.CompoundStart());
        assertThat(reader.nextOrNull()).isEqualTo(new LinToken.Name("a"));
        assertThat(reader.nextOrNull()).isEqualTo(new LinToken.ByteArrayStart());
        var ex = assertThrows(NbtParseException.class, reader::nextOrNull);
        assertThat(ex).hasMessageThat().isEqualTo(atCharacterIndex(8) + "Unexpected token: '}'");
    }

    @Test
    void largeByteArray() throws IOException {
        var reader = ezStringRead("{a:[B; " + String.join(",", Iterables.limit(
            Iterables.cycle("1b"), 100_000
        )) + "]}");
        assertThat(reader.nextOrNull()).isEqualTo(new LinToken.CompoundStart());
        assertThat(reader.nextOrNull()).isEqualTo(new LinToken.Name("a"));
        assertThat(reader.nextOrNull()).isEqualTo(new LinToken.ByteArrayStart());
        int size = 0;
        while (true) {
            var next = reader.nextOrNull();
            assertNotNull(next, "Expected more tokens");
            if (next instanceof LinToken.ByteArrayEnd) {
                break;
            }
            if (!(next instanceof LinToken.ByteArrayContent content)) {
                fail("Expected ByteArrayContent, got " + next);
                return;
            }
            size += content.buffer().remaining();
            var bytes = new byte[content.buffer().remaining()];
            content.buffer().get(bytes);
            for (var b : bytes) {
                assertThat(b).isEqualTo((byte) 1);
            }
        }
        assertThat(size).isEqualTo(100_000);
    }
}
