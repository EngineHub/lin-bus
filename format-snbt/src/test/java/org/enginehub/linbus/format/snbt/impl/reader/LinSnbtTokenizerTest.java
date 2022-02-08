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
import org.junit.jupiter.api.Test;

import java.io.StringReader;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class LinSnbtTokenizerTest {
    private static LinSnbtTokenizer ezStringTokenize(String input) {
        return new LinSnbtTokenizer(new StringReader(input));
    }

    private static String atCharacterIndex(int charIndex) {
        return "At character index " + charIndex + ": ";
    }

    @Test
    void tooShortInput() {
        var tokens = ImmutableList.copyOf(ezStringTokenize(""));
        assertThat(tokens).isEmpty();
    }

    @Test
    void unfinishedQuotedText() {
        var reader = ezStringTokenize("'cringes at the lack of end quote");
        var ex = assertThrows(IllegalStateException.class, reader::next);
        assertThat(ex).hasMessageThat().isEqualTo(
            atCharacterIndex(32) + "Unexpected end of input in quoted value"
        );
    }

    @Test
    void invalidEscapesInQuotedText() {
        assertAll(() -> {
            var reader = ezStringTokenize("'what the \\fsck is this'");
            var ex = assertThrows(IllegalStateException.class, reader::next);
            assertThat(ex).hasMessageThat().isEqualTo(
                atCharacterIndex(11) + "Invalid escape: \\f"
            );
        }, () -> {
            var reader = ezStringTokenize("'what the \\fsck is this'");
            var ex = assertThrows(IllegalStateException.class, reader::next);
            assertThat(ex).hasMessageThat().isEqualTo(
                atCharacterIndex(11) + "Invalid escape: \\f"
            );
        });
    }

    @Test
    void mustHaveRootCompound() {
        var tokens = ImmutableList.copyOf(ezStringTokenize("[]"));
        assertThat(tokens).containsExactly(
            new SnbtTokenWithMetadata(SnbtToken.ListLikeStart.INSTANCE, 0),
            new SnbtTokenWithMetadata(SnbtToken.ListLikeEnd.INSTANCE, 1)
        ).inOrder();
    }

    @Test
    void invalidCharacterInSimpleValue() {
        var reader = ezStringTokenize("{a:@}");
        assertThat(reader.next()).isEqualTo(new SnbtTokenWithMetadata(SnbtToken.CompoundStart.INSTANCE, 0));
        assertThat(reader.next()).isEqualTo(new SnbtTokenWithMetadata(new SnbtToken.Text(false, "a"), 1));
        assertThat(reader.next()).isEqualTo(new SnbtTokenWithMetadata(SnbtToken.EntrySeparator.INSTANCE, 2));
        var ex = assertThrows(IllegalStateException.class, reader::next);
        assertThat(ex).hasMessageThat().isEqualTo(atCharacterIndex(3) + "Unexpected character: @");
    }

    @Test
    void simpleValueWithWhitespace() {
        var list = ImmutableList.copyOf(ezStringTokenize("{a:b      }"));
        assertThat(list).containsExactly(
            new SnbtTokenWithMetadata(SnbtToken.CompoundStart.INSTANCE, 0),
            new SnbtTokenWithMetadata(new SnbtToken.Text(false, "a"), 1),
            new SnbtTokenWithMetadata(SnbtToken.EntrySeparator.INSTANCE, 2),
            new SnbtTokenWithMetadata(new SnbtToken.Text(false, "b"), 3),
            new SnbtTokenWithMetadata(SnbtToken.CompoundEnd.INSTANCE, 10)
        ).inOrder();

        list = ImmutableList.copyOf(ezStringTokenize("{a:[b      , c ]}"));
        assertThat(list).containsExactly(
            new SnbtTokenWithMetadata(SnbtToken.CompoundStart.INSTANCE, 0),
            new SnbtTokenWithMetadata(new SnbtToken.Text(false, "a"), 1),
            new SnbtTokenWithMetadata(SnbtToken.EntrySeparator.INSTANCE, 2),
            new SnbtTokenWithMetadata(SnbtToken.ListLikeStart.INSTANCE, 3),
            new SnbtTokenWithMetadata(new SnbtToken.Text(false, "b"), 4),
            new SnbtTokenWithMetadata(SnbtToken.Separator.INSTANCE, 11),
            new SnbtTokenWithMetadata(new SnbtToken.Text(false, "c"), 13),
            new SnbtTokenWithMetadata(SnbtToken.ListLikeEnd.INSTANCE, 15),
            new SnbtTokenWithMetadata(SnbtToken.CompoundEnd.INSTANCE, 16)
        ).inOrder();
    }

    @Test
    void invalidWhitespaceInSimpleValue() {
        var reader = ezStringTokenize("{a:space goes there}");
        assertThat(reader.next()).isEqualTo(new SnbtTokenWithMetadata(SnbtToken.CompoundStart.INSTANCE, 0));
        assertThat(reader.next()).isEqualTo(new SnbtTokenWithMetadata(new SnbtToken.Text(false, "a"), 1));
        assertThat(reader.next()).isEqualTo(new SnbtTokenWithMetadata(SnbtToken.EntrySeparator.INSTANCE, 2));
        var ex = assertThrows(IllegalStateException.class, reader::next);
        assertThat(ex).hasMessageThat().endsWith("Found non-terminator after whitespace");
    }

    @Test
    void quotedName() {
        var list = ImmutableList.copyOf(ezStringTokenize("{'it is a me, mario': true}"));
        assertThat(list).containsExactly(
            new SnbtTokenWithMetadata(SnbtToken.CompoundStart.INSTANCE, 0),
            new SnbtTokenWithMetadata(new SnbtToken.Text(true, "it is a me, mario"), 1),
            new SnbtTokenWithMetadata(SnbtToken.EntrySeparator.INSTANCE, 20),
            new SnbtTokenWithMetadata(new SnbtToken.Text(false, "true"), 22),
            new SnbtTokenWithMetadata(SnbtToken.CompoundEnd.INSTANCE, 26)
        ).inOrder();
    }

    @Test
    void quotedNameWithEscapes() {
        var list = ImmutableList.copyOf(ezStringTokenize("{'\\'twice the nesting, double the quotes\\'': 'and \\\\ escapes in your escapes'}"));
        assertThat(list).containsExactly(
            new SnbtTokenWithMetadata(SnbtToken.CompoundStart.INSTANCE, 0),
            new SnbtTokenWithMetadata(new SnbtToken.Text(true, "'twice the nesting, double the quotes'"), 1),
            new SnbtTokenWithMetadata(SnbtToken.EntrySeparator.INSTANCE, 43),
            new SnbtTokenWithMetadata(new SnbtToken.Text(true, "and \\ escapes in your escapes"), 45),
            new SnbtTokenWithMetadata(SnbtToken.CompoundEnd.INSTANCE, 77)
        ).inOrder();
    }

    @Test
    void nameWithAllValidChars() {
        var list = ImmutableList.copyOf(ezStringTokenize("1aA_.+-"));
        assertThat(list).containsExactly(
            new SnbtTokenWithMetadata(new SnbtToken.Text(false, "1aA_.+-"), 0)
        ).inOrder();
    }

    @Test
    void nameWithWhitespace() {
        var list = ImmutableList.copyOf(ezStringTokenize("{ a :b,c  :d}"));
        assertThat(list).containsExactly(
            new SnbtTokenWithMetadata(SnbtToken.CompoundStart.INSTANCE, 0),
            new SnbtTokenWithMetadata(new SnbtToken.Text(false, "a"), 2),
            new SnbtTokenWithMetadata(SnbtToken.EntrySeparator.INSTANCE, 4),
            new SnbtTokenWithMetadata(new SnbtToken.Text(false, "b"), 5),
            new SnbtTokenWithMetadata(SnbtToken.Separator.INSTANCE, 6),
            new SnbtTokenWithMetadata(new SnbtToken.Text(false, "c"), 7),
            new SnbtTokenWithMetadata(SnbtToken.EntrySeparator.INSTANCE, 10),
            new SnbtTokenWithMetadata(new SnbtToken.Text(false, "d"), 11),
            new SnbtTokenWithMetadata(SnbtToken.CompoundEnd.INSTANCE, 12)
        ).inOrder();
    }

    @Test
    void badName() {
        assertAll(() -> {
            var reader = ezStringTokenize("{a@");
            assertThat(reader.next()).isEqualTo(new SnbtTokenWithMetadata(SnbtToken.CompoundStart.INSTANCE, 0));
            var ex = assertThrows(IllegalStateException.class, reader::next);
            assertThat(ex).hasMessageThat().isEqualTo(atCharacterIndex(2) + "Unexpected character: @");
        }, () -> {
            var reader = ezStringTokenize("{A|");
            assertThat(reader.next()).isEqualTo(new SnbtTokenWithMetadata(SnbtToken.CompoundStart.INSTANCE, 0));
            var ex = assertThrows(IllegalStateException.class, reader::next);
            assertThat(ex).hasMessageThat().isEqualTo(atCharacterIndex(2) + "Unexpected character: |");
        });
    }

    @Test
    void badNameEnd() {
        var reader = ezStringTokenize("{'a'!");
        assertThat(reader.next()).isEqualTo(new SnbtTokenWithMetadata(SnbtToken.CompoundStart.INSTANCE, 0));
        assertThat(reader.next()).isEqualTo(new SnbtTokenWithMetadata(new SnbtToken.Text(true, "a"), 1));
        var ex = assertThrows(IllegalStateException.class, reader::next);
        assertThat(ex).hasMessageThat().isEqualTo(atCharacterIndex(4) + "Unexpected character: !");
    }

    @Test
    void badCompoundEnd() {
        var reader = ezStringTokenize("{a:'@'!");
        assertThat(reader.next()).isEqualTo(new SnbtTokenWithMetadata(SnbtToken.CompoundStart.INSTANCE, 0));
        assertThat(reader.next()).isEqualTo(new SnbtTokenWithMetadata(new SnbtToken.Text(false, "a"), 1));
        assertThat(reader.next()).isEqualTo(new SnbtTokenWithMetadata(SnbtToken.EntrySeparator.INSTANCE, 2));
        assertThat(reader.next()).isEqualTo(new SnbtTokenWithMetadata(new SnbtToken.Text(true, "@"), 3));
        var ex = assertThrows(IllegalStateException.class, reader::next);
        assertThat(ex).hasMessageThat().isEqualTo(atCharacterIndex(6) + "Unexpected character: !");
    }

    @Test
    void badListEnd() {
        var reader = ezStringTokenize("{a:['@'!");
        assertThat(reader.next()).isEqualTo(new SnbtTokenWithMetadata(SnbtToken.CompoundStart.INSTANCE, 0));
        assertThat(reader.next()).isEqualTo(new SnbtTokenWithMetadata(new SnbtToken.Text(false, "a"), 1));
        assertThat(reader.next()).isEqualTo(new SnbtTokenWithMetadata(SnbtToken.EntrySeparator.INSTANCE, 2));
        assertThat(reader.next()).isEqualTo(new SnbtTokenWithMetadata(SnbtToken.ListLikeStart.INSTANCE, 3));
        assertThat(reader.next()).isEqualTo(new SnbtTokenWithMetadata(new SnbtToken.Text(true, "@"), 4));
        var ex = assertThrows(IllegalStateException.class, reader::next);
        assertThat(ex).hasMessageThat().isEqualTo(atCharacterIndex(7) + "Unexpected character: !");
    }

    @Test
    void badByteArrayContent() {
        var tokens = ImmutableList.copyOf(ezStringTokenize("{a:[B;lmao_gottem]}"));
        assertThat(tokens).containsExactly(
            new SnbtTokenWithMetadata(SnbtToken.CompoundStart.INSTANCE, 0),
            new SnbtTokenWithMetadata(new SnbtToken.Text(false, "a"), 1),
            new SnbtTokenWithMetadata(SnbtToken.EntrySeparator.INSTANCE, 2),
            new SnbtTokenWithMetadata(SnbtToken.ListLikeStart.INSTANCE, 3),
            new SnbtTokenWithMetadata(new SnbtToken.Text(false, "B"), 4),
            new SnbtTokenWithMetadata(SnbtToken.ListTypeSeparator.INSTANCE, 5),
            new SnbtTokenWithMetadata(new SnbtToken.Text(false, "lmao_gottem"), 6),
            new SnbtTokenWithMetadata(SnbtToken.ListLikeEnd.INSTANCE, 17),
            new SnbtTokenWithMetadata(SnbtToken.CompoundEnd.INSTANCE, 18)
        ).inOrder();
    }

    @Test
    void badByteArraySeparator() {
        var tokens = ImmutableList.copyOf(ezStringTokenize("{a:[B;1b}2b]}"));
        assertThat(tokens).containsExactly(
            new SnbtTokenWithMetadata(SnbtToken.CompoundStart.INSTANCE, 0),
            new SnbtTokenWithMetadata(new SnbtToken.Text(false, "a"), 1),
            new SnbtTokenWithMetadata(SnbtToken.EntrySeparator.INSTANCE, 2),
            new SnbtTokenWithMetadata(SnbtToken.ListLikeStart.INSTANCE, 3),
            new SnbtTokenWithMetadata(new SnbtToken.Text(false, "B"), 4),
            new SnbtTokenWithMetadata(SnbtToken.ListTypeSeparator.INSTANCE, 5),
            new SnbtTokenWithMetadata(new SnbtToken.Text(false, "1b"), 6),
            new SnbtTokenWithMetadata(SnbtToken.CompoundEnd.INSTANCE, 8),
            new SnbtTokenWithMetadata(new SnbtToken.Text(false, "2b"), 9),
            new SnbtTokenWithMetadata(SnbtToken.ListLikeEnd.INSTANCE, 11),
            new SnbtTokenWithMetadata(SnbtToken.CompoundEnd.INSTANCE, 12)
        ).inOrder();
    }

    @Test
    void largeByteArray() {
        var reader = ezStringTokenize("{a:[B;" + String.join(",", Iterables.limit(
            Iterables.cycle("1b"), 100_000
        )) + "]}");
        assertThat(reader.next()).isEqualTo(new SnbtTokenWithMetadata(SnbtToken.CompoundStart.INSTANCE, 0));
        assertThat(reader.next()).isEqualTo(new SnbtTokenWithMetadata(new SnbtToken.Text(false, "a"), 1));
        assertThat(reader.next()).isEqualTo(new SnbtTokenWithMetadata(SnbtToken.EntrySeparator.INSTANCE, 2));
        assertThat(reader.next()).isEqualTo(new SnbtTokenWithMetadata(SnbtToken.ListLikeStart.INSTANCE, 3));
        assertThat(reader.next()).isEqualTo(new SnbtTokenWithMetadata(new SnbtToken.Text(false, "B"), 4));
        assertThat(reader.next()).isEqualTo(new SnbtTokenWithMetadata(SnbtToken.ListTypeSeparator.INSTANCE, 5));
        final int lengthPerIteration = 3;
        for (int i = 0; i < 100_000; i++) {
            int base = 6 + i * lengthPerIteration;
            assertThat(reader.next()).isEqualTo(new SnbtTokenWithMetadata(new SnbtToken.Text(false, "1b"), base));
            if (i < 99_999) {
                assertThat(reader.next()).isEqualTo(new SnbtTokenWithMetadata(SnbtToken.Separator.INSTANCE, base + 2));
            }
        }
        assertThat(reader.next()).isEqualTo(new SnbtTokenWithMetadata(SnbtToken.ListLikeEnd.INSTANCE, 6 + 99_999 * lengthPerIteration + 2));
        assertThat(reader.next()).isEqualTo(new SnbtTokenWithMetadata(SnbtToken.CompoundEnd.INSTANCE, 6 + 99_999 * lengthPerIteration + 3));
    }
}
