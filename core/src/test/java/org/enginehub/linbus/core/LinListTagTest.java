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

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Arrays;

public class LinListTagTest {
    @Test
    void roundTrip() throws IOException {
        TagTestUtil.assertRoundTrip(new LinListTag<>(LinTagType.stringTag(), Arrays.asList(
            new LinStringTag("Hello"),
            new LinStringTag("World!")
        )));
        TagTestUtil.assertRoundTrip(new LinListTag<>(LinTagType.doubleTag(), Arrays.asList(
            new LinDoubleTag(Double.POSITIVE_INFINITY),
            new LinDoubleTag(0.0),
            new LinDoubleTag(-0.0)
        )));
    }
}
