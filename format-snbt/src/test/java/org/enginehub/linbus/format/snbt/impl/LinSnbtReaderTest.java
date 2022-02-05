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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import org.enginehub.linbus.format.snbt.util.TracedReader;
import org.enginehub.linbus.stream.token.LinToken;
import org.junit.jupiter.api.Test;

import java.io.EOFException;
import java.io.StringReader;
import java.io.UncheckedIOException;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class LinSnbtReaderTest {
    private static LinSnbtReader ezStringRead(String input) {
        return new LinSnbtReader(new TracedReader(new StringReader(input)));
    }

    @Test
    void tooShortInput() {
        var reader = ezStringRead("");
        var ex = assertThrows(UncheckedIOException.class, reader::next);
        assertThat(ex).hasCauseThat().isInstanceOf(EOFException.class);
        assertThat(ex).hasCauseThat().hasMessageThat().endsWith("Unexpected end of input");
    }

    @Test
    void mustHaveRootCompound() {
        var reader = ezStringRead("[]");
        var ex = assertThrows(IllegalStateException.class, reader::next);
        assertThat(ex).hasMessageThat().endsWith("Expected '{' but got '['");
    }

    @Test
    void invalidCharacterInSimpleValue() {
        var reader = ezStringRead("{a:@}");
        assertThat(reader.next()).isEqualTo(new LinToken.CompoundStart());
        assertThat(reader.next()).isEqualTo(new LinToken.Name("a"));
        var ex = assertThrows(IllegalStateException.class, reader::next);
        assertThat(ex).hasMessageThat().endsWith("Unexpected character: @");
    }

    @Test
    void simpleValueWithWhitespace() {
        var list = ImmutableList.copyOf(ezStringRead("{a:b      }"));
        assertThat(list).containsExactly(
            new LinToken.CompoundStart(),
            new LinToken.Name("a"),
            new LinToken.String("b"),
            new LinToken.CompoundEnd()
        ).inOrder();

        list = ImmutableList.copyOf(ezStringRead("{a:[b      , c ]}"));
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
    void nameWithWhitespace() {
        var list = ImmutableList.copyOf(ezStringRead("{ a :b,c  :d}"));
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
    void badName() {
        var reader = ezStringRead("{a@");
        assertThat(reader.next()).isEqualTo(new LinToken.CompoundStart());
        var ex = assertThrows(IllegalStateException.class, reader::next);
        assertThat(ex).hasMessageThat().endsWith("Unexpected character: @");
    }

    @Test
    void badNameEnd() {
        var reader = ezStringRead("{'a'!");
        assertThat(reader.next()).isEqualTo(new LinToken.CompoundStart());
        var ex = assertThrows(IllegalStateException.class, reader::next);
        assertThat(ex).hasMessageThat().endsWith("Unexpected character: !");
    }

    @Test
    void badCompoundEnd() {
        var reader = ezStringRead("{a:'@'!");
        assertThat(reader.next()).isEqualTo(new LinToken.CompoundStart());
        assertThat(reader.next()).isEqualTo(new LinToken.Name("a"));
        assertThat(reader.next()).isEqualTo(new LinToken.String("@"));
        var ex = assertThrows(IllegalStateException.class, reader::next);
        assertThat(ex).hasMessageThat().endsWith("Unexpected character: !");
    }

    @Test
    void badListEnd() {
        var reader = ezStringRead("{a:['@'!");
        assertThat(reader.next()).isEqualTo(new LinToken.CompoundStart());
        assertThat(reader.next()).isEqualTo(new LinToken.Name("a"));
        assertThat(reader.next()).isEqualTo(new LinToken.ListStart());
        assertThat(reader.next()).isEqualTo(new LinToken.String("@"));
        var ex = assertThrows(IllegalStateException.class, reader::next);
        assertThat(ex).hasMessageThat().endsWith("Unexpected character: !");
    }

    @Test
    void badByteArrayContent() {
        var reader = ezStringRead("{a:[B;lmao_gottem]}");
        assertThat(reader.next()).isEqualTo(new LinToken.CompoundStart());
        assertThat(reader.next()).isEqualTo(new LinToken.Name("a"));
        assertThat(reader.next()).isEqualTo(new LinToken.ByteArrayStart());
        var ex = assertThrows(IllegalStateException.class, reader::next);
        assertThat(ex).hasMessageThat().endsWith("Expected Byte token, got String[value=lmao_gottem]");
    }

    @Test
    void badByteArraySeparator() {
        var reader = ezStringRead("{a:[B;1b}");
        assertThat(reader.next()).isEqualTo(new LinToken.CompoundStart());
        assertThat(reader.next()).isEqualTo(new LinToken.Name("a"));
        assertThat(reader.next()).isEqualTo(new LinToken.ByteArrayStart());
        var ex = assertThrows(IllegalStateException.class, reader::next);
        assertThat(ex).hasMessageThat().endsWith("Unexpected character: }");
    }

    @Test
    void largeByteArray() {
        var reader = ezStringRead("{a:[B; " + String.join(",", Iterables.limit(
            Iterables.cycle("1b"), 100_000
        )) + "]}");
        assertThat(reader.next()).isEqualTo(new LinToken.CompoundStart());
        assertThat(reader.next()).isEqualTo(new LinToken.Name("a"));
        assertThat(reader.next()).isEqualTo(new LinToken.ByteArrayStart());
        var ex = assertThrows(IllegalStateException.class, reader::next);
        assertThat(ex).hasMessageThat().endsWith("Unexpected character: }");
    }
}
