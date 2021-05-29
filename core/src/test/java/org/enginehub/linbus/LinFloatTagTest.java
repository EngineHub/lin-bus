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

package org.enginehub.linbus;

import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.enginehub.linbus.TagTestUtil.assertRoundTrip;

public class LinFloatTagTest {
    @Test
    void roundTrip() throws IOException {
        assertRoundTrip(new LinFloatTag(0x01));
        assertRoundTrip(new LinFloatTag(0x09));
        assertRoundTrip(new LinFloatTag(Float.MIN_VALUE));
        assertRoundTrip(new LinFloatTag(Float.NaN));
        assertRoundTrip(new LinFloatTag(Float.NEGATIVE_INFINITY));
    }
}
