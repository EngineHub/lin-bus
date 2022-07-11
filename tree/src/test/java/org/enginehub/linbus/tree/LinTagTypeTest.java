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

import org.enginehub.linbus.common.LinTagId;
import org.junit.jupiter.api.Test;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class LinTagTypeTest {
    @Test
    void equalsAndHashCodeImplementation() {
        SimpleObjectVerifier.assertEqualsHashCodeImplementation(
            LinTagType.longTag(),
            LinTagType.longTag(),
            LinTagType.longTag(),
            LinTagType.longArrayTag()
        );
    }

    @Test
    void toStringImplementation() {
        assertThat(LinTagType.longTag().toString()).isEqualTo(LinTagId.LONG.toString());
        assertThat(LinTagType.longArrayTag().toString()).isEqualTo(LinTagId.LONG_ARRAY.toString());
    }

    @Test
    void castImplementation() {
        var item = LinLongTag.of(1L);
        assertThat(LinTagType.longTag().cast(item)).isSameInstanceAs(item);
        var thrown = assertThrows(
            IllegalArgumentException.class,
            () -> LinTagType.longTag().cast(LinStringTag.of("foo"))
        );
        assertThat(thrown).hasMessageThat().contains("Tag is a STRING, not a LONG");
    }
}
