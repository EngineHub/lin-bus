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
import java.nio.ByteBuffer;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class LinByteArrayTagTest {
    @Test
    void roundTrip() throws IOException {
        TagTestUtil.assertRoundTrip(LinByteArrayTag.of());
        TagTestUtil.assertRoundTrip(LinByteArrayTag.of(new byte[]{0x01}));
        TagTestUtil.assertRoundTrip(LinByteArrayTag.of(new byte[]{0x01, 0x02, 0x03, 0x04}));
    }

    @Test
    void viewContentEqualsActualContent() {
        var tag = LinByteArrayTag.of(new byte[]{0x01, 0x02, 0x03, 0x04});
        assertEquals(ByteBuffer.wrap(tag.value()), tag.view());
    }

    @Test
    void equalsAndHashCodeImplementation() {
        SimpleObjectVerifier.assertEqualsHashCodeImplementation(
            LinByteArrayTag.of(new byte[]{0x01, 0x02, 0x03, 0x04}),
            LinByteArrayTag.of(new byte[]{0x01, 0x02, 0x03, 0x04}),
            LinByteArrayTag.of(new byte[]{0x01, 0x02, 0x03, 0x04}),
            LinByteArrayTag.of(new byte[]{0x01, 0x02, 0x03, 0x05})
        );
    }

    @Test
    void toStringImplementation() {
        assertEquals(
            "LinByteArrayTag[1, 2, 3, 4]",
            LinByteArrayTag.of(new byte[]{0x01, 0x02, 0x03, 0x04}).toString()
        );
    }
}
