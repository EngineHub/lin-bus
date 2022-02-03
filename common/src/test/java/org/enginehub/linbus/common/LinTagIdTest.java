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

package org.enginehub.linbus.common;

import org.junit.jupiter.api.Test;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class LinTagIdTest {
    @Test
    void fromId() {
        for (var value : LinTagId.values()) {
            assertThat(LinTagId.fromId(value.id())).isEqualTo(value);
        }
        var ex = assertThrows(IllegalArgumentException.class, () -> LinTagId.fromId(-1));
        assertThat(ex).hasMessageThat().isEqualTo("Invalid NBT ID: -1");

        ex = assertThrows(IllegalArgumentException.class, () -> LinTagId.fromId(LinTagId.values().length));
        assertThat(ex).hasMessageThat().isEqualTo("Invalid NBT ID: " + LinTagId.values().length);
    }

    @Test
    void idIsOrdinal() {
        for (var value : LinTagId.values()) {
            assertThat(value.id()).isEqualTo(value.ordinal());
        }
    }

    @Test
    void toStringIsNameAndId() {
        for (var value : LinTagId.values()) {
            assertThat(value.toString()).isEqualTo(value.name() + "[id=" + value.id() + "]");
        }
    }
}
