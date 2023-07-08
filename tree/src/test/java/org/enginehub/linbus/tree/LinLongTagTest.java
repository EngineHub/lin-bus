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

import java.io.IOException;

import static com.google.common.truth.Truth.assertThat;
import static org.enginehub.linbus.tree.TagTestUtil.assertRoundTrip;

public class LinLongTagTest {
    @Test
    void roundTrip() throws IOException {
        assertRoundTrip(LinLongTag.of(0x01));
        assertRoundTrip(LinLongTag.of(0x09));
        assertRoundTrip(LinLongTag.of(Integer.MIN_VALUE));
    }

    @Test
    void valueAsLong() {
        assertThat(LinLongTag.of(0x01).valueAsLong()).isEqualTo(0x01);
    }
}
