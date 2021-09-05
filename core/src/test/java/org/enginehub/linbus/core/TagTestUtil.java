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

package org.enginehub.linbus.core;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;

import java.io.IOException;

import static com.google.common.truth.Truth.assertThat;

class TagTestUtil {
    static <T extends LinTag<?, ?>> void assertRoundTrip(T input) throws IOException {
        ByteArrayDataOutput dataOutput = ByteStreams.newDataOutput();
        input.writeTo(dataOutput);
        @SuppressWarnings("unchecked")
        T recreated = (T) input.type().readFrom(
            ByteStreams.newDataInput(dataOutput.toByteArray())
        );
        assertThat(recreated).isEqualTo(input);
    }

    private TagTestUtil() {
    }
}
