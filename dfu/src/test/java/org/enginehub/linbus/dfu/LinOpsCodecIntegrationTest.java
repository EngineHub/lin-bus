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

package org.enginehub.linbus.dfu;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import org.enginehub.linbus.tree.LinCompoundTag;
import org.junit.jupiter.api.Test;

import static org.enginehub.linbus.dfu.DataResultSubject.assertThat;

class LinOpsCodecIntegrationTest {

    private record Point(int x, int y) {
        static final Codec<Point> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.INT.fieldOf("x").forGetter(Point::x),
                Codec.INT.fieldOf("y").forGetter(Point::y)
            ).apply(instance, Point::new)
        );
    }

    private static final LinOps OPS = LinOps.getInstance();

    private static final Point POINT = new Point(3, 4);
    private static final LinCompoundTag POINT_AS_COMPOUND = LinCompoundTag.builder()
        .putInt("x", 3)
        .putInt("y", 4)
        .build();

    @Test
    void encodesRecordToCompound() {
        assertThat(Point.CODEC.encodeStart(OPS, POINT)).hasResultThat().isEqualTo(POINT_AS_COMPOUND);
    }

    @Test
    void parsesRecordFromCompound() {
        assertThat(Point.CODEC.parse(OPS, POINT_AS_COMPOUND)).hasResultThat().isEqualTo(POINT);
    }
}
