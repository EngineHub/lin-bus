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
import java.nio.IntBuffer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class LinIntArrayTagTest {
    @Test
    void roundTrip() throws IOException {
        TagTestUtil.assertRoundTrip(new LinIntArrayTag());
        TagTestUtil.assertRoundTrip(new LinIntArrayTag(0x01));
        TagTestUtil.assertRoundTrip(new LinIntArrayTag(0x01, 0x02, 0x03, 0x04));
    }

    @Test
    void throwsIfImproperlyConstructed() {
        assertThrows(
            IllegalArgumentException.class,
            () -> new LinIntArrayTag(new int[0], false)
        );
    }

    @Test
    void viewContentEqualsActualContent() {
        var tag = new LinIntArrayTag(0x01, 0x02, 0x03, 0x04);
        assertEquals(IntBuffer.wrap(tag.value()), tag.view());
    }

    @Test
    void equalsAndHashCodeImplementation() {
        SimpleObjectVerifier.assertEqualsHashCodeImplementation(
            new LinIntArrayTag(0x01, 0x02, 0x03, 0x04),
            new LinIntArrayTag(0x01, 0x02, 0x03, 0x04),
            new LinIntArrayTag(0x01, 0x02, 0x03, 0x04),
            new LinIntArrayTag(0x01, 0x02, 0x03, 0x05)
        );
    }

    @Test
    void toStringImplementation() {
        assertEquals(
            "LinIntArrayTag[1, 2, 3, 4]",
            new LinIntArrayTag(0x01, 0x02, 0x03, 0x04).toString()
        );
    }
}
