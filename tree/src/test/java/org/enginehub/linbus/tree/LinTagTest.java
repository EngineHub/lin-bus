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

package org.enginehub.linbus.tree;

import org.junit.jupiter.api.Test;

import static com.google.common.truth.Truth.assertThat;

public class LinTagTest {
    @Test
    void equalsAndHashCodeImplementation() {
        SimpleObjectVerifier.assertEqualsHashCodeImplementation(
            new LinIntTag(42),
            new LinIntTag(42),
            new LinIntTag(42),
            new LinIntTag(Integer.MAX_VALUE)
        );
    }

    @Test
    void toLinTagImplementation() {
        var tag = new LinIntTag(42);
        assertThat(tag.toLinTag()).isSameInstanceAs(tag);
    }

    @Test
    void coerceAsIntImplementation() {
        assertThat(new LinByteTag((byte) 42).coerceAsInt()).isEqualTo(42);
        assertThat(new LinShortTag((short) 42).coerceAsInt()).isEqualTo(42);
        assertThat(new LinIntTag(42).coerceAsInt()).isEqualTo(42);
        assertThat(new LinLongTag(42).coerceAsInt()).isEqualTo(42);
        assertThat(new LinFloatTag(42).coerceAsInt()).isEqualTo(42);
        assertThat(new LinDoubleTag(42).coerceAsInt()).isEqualTo(42);

        assertThat(new LinByteTag((byte) 255).coerceAsInt()).isEqualTo(-1);
        assertThat(new LinShortTag((short) 65535).coerceAsInt()).isEqualTo(-1);

        // Truncating is used.
        assertThat(new LinFloatTag(42.714F).coerceAsInt()).isEqualTo(42);
        assertThat(new LinDoubleTag(42.714).coerceAsInt()).isEqualTo(42);
        assertThat(new LinFloatTag(-42.714F).coerceAsInt()).isEqualTo(-42);
        assertThat(new LinDoubleTag(-42.714).coerceAsInt()).isEqualTo(-42);

        // Non-int values are zero.
        assertThat(new LinStringTag("42").coerceAsInt()).isEqualTo(0);
        assertThat(LinEndTag.instance().coerceAsInt()).isEqualTo(0);
    }
}
