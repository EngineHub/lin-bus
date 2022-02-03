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

package org.enginehub.linbus.common.internal;

import com.google.common.collect.ImmutableList;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.Collectors;

import static com.google.common.truth.Truth.assertThat;

public class IteratorsTest {
    @Test
    void combineVarargs() {
        var result = ImmutableList.copyOf(Iterators.combine(
            List.of("a", "b", "c").iterator(),
            List.of("1", "2", "3").iterator()
        ));
        assertThat(result).containsExactly("a", "b", "c", "1", "2", "3").inOrder();
    }

    @Test
    void combineIterator() {
        var result = ImmutableList.copyOf(Iterators.combine(
            List.of(
                List.of("a", "b", "c").iterator(),
                List.of("1", "2", "3").iterator()
            ).iterator()
        ));
        assertThat(result).containsExactly("a", "b", "c", "1", "2", "3").inOrder();
    }

    @Test
    void singletonIterator() {
        var result = ImmutableList.copyOf(Iterators.of("a"));
        assertThat(result).containsExactly("a").inOrder();
        result = ImmutableList.copyOf(Iterators.of("b"));
        assertThat(result).containsExactly("b").inOrder();
    }

    @Test
    void collect() {
        var result = Iterators.collect(List.of("a", "b", "c").iterator(), Collectors.toList());
        assertThat(result).containsExactly("a", "b", "c").inOrder();
    }

    @Test
    void collectComplex() {
        var result = Iterators.collect(List.of("a", "b", "c").iterator(), Collectors.joining());
        assertThat(result).isEqualTo("abc");
    }
}
