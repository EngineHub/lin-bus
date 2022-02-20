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

package org.enginehub.linbus.stream.internal;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterators;
import org.enginehub.linbus.stream.LinStream;
import org.enginehub.linbus.stream.token.LinToken;
import org.junit.jupiter.api.Test;

import static com.google.common.truth.Truth.assertThat;

public class FlatteningLinStreamTest {
    @Test
    void flattens() {
        var tokens = ImmutableList.copyOf(
            new FlatteningLinStream(Iterators.forArray(
                LinStream.of(new LinToken.String("a"), new LinToken.String("b")),
                LinStream.of(new LinToken.String("c"), new LinToken.String("d"))
            )).asIterator()
        );
        assertThat(tokens).containsExactly(
            new LinToken.String("a"),
            new LinToken.String("b"),
            new LinToken.String("c"),
            new LinToken.String("d")
        ).inOrder();
    }
}
