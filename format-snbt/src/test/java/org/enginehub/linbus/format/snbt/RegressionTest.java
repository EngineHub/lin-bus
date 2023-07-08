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

package org.enginehub.linbus.format.snbt;

import org.enginehub.linbus.tree.LinCompoundTag;
import org.enginehub.linbus.tree.LinListTag;
import org.enginehub.linbus.tree.LinStringTag;
import org.enginehub.linbus.tree.LinTagType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class RegressionTest {
    // https://github.com/EngineHub/lin-bus/issues/2
    @Test
    void readCompoundFromString() {
        var compound = LinStringIO.readFromStringUsing("{'CustomName':'cake'}", LinCompoundTag::readFrom);
        assertEquals(
            LinCompoundTag.builder()
                .putString("CustomName", "cake")
                .build(),
            compound
        );
    }

    // https://github.com/EngineHub/lin-bus/issues/3
    @Test
    void readListFromString() {
        var compound = LinStringIO.readFromStringUsing("{'messages':['a']}", LinCompoundTag::readFrom);
        assertEquals(
            LinCompoundTag.builder()
                .put("messages", LinListTag.builder(LinTagType.stringTag())
                    .add(LinStringTag.of("a"))
                    .build())
                .build(),
            compound
        );
    }
}
