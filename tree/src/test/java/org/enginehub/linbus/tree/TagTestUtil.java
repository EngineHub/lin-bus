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

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import org.enginehub.linbus.stream.LinBinaryIO;

import java.io.IOException;
import java.util.Map;

import static org.enginehub.linbus.tree.truth.LinTagSubject.assertThat;

class TagTestUtil {
    private static final String NESTING_KEY = "inner";

    static <T extends LinTag<?>> void assertRoundTrip(T input) throws IOException {
        ByteArrayDataOutput dataOutput = ByteStreams.newDataOutput();
        // It's not legal to use bare streams, so we wrap in a root entry and compound.
        LinBinaryIO.write(
            dataOutput,
            new LinRootEntry("", new LinCompoundTag(Map.of(NESTING_KEY, input)))
        );
        @SuppressWarnings("unchecked")
        T recreated = LinBinaryIO.readUsing(
            ByteStreams.newDataInput(dataOutput.toByteArray()),
            LinRootEntry::readFrom
        ).value().getTag(NESTING_KEY, (LinTagType<T>) input.type());
        assertThat(recreated).isEqualTo(input);
    }

    private TagTestUtil() {
    }
}
