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

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class EmptyRecordShimTest {
    @Test
    void simpleTest() {
        class Shimmed extends EmptyRecordShim {
        }
        var shimmed = new Shimmed();

        assertEquals(new Shimmed(), shimmed);

        assertEquals("Shimmed", shimmed.toString());

        assertEquals(new Shimmed().hashCode(), shimmed.hashCode());

        // It doesn't actually give any record components, though
        assertNull(Shimmed.class.getRecordComponents());
    }
}
