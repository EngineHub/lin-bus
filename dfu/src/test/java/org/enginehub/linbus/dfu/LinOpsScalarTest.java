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

import org.enginehub.linbus.tree.LinByteTag;
import org.enginehub.linbus.tree.LinCompoundTag;
import org.enginehub.linbus.tree.LinDoubleTag;
import org.enginehub.linbus.tree.LinEndTag;
import org.enginehub.linbus.tree.LinFloatTag;
import org.enginehub.linbus.tree.LinIntTag;
import org.enginehub.linbus.tree.LinListTag;
import org.enginehub.linbus.tree.LinLongTag;
import org.enginehub.linbus.tree.LinShortTag;
import org.enginehub.linbus.tree.LinStringTag;
import org.enginehub.linbus.tree.LinTagType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.google.common.truth.Truth.assertThat;
import static org.enginehub.linbus.dfu.DataResultSubject.assertThat;

class LinOpsScalarTest {

    private static final LinOps OPS = LinOps.getInstance();

    @Test
    void emptyIsEndTag() {
        assertThat(OPS.empty()).isEqualTo(LinEndTag.instance());
    }

    @Test
    void emptyListIsEndTypedEmptyList() {
        assertThat(OPS.emptyList()).isEqualTo(LinListTag.empty(LinTagType.endTag()));
    }

    @Test
    void emptyMapIsEmptyCompound() {
        assertThat(OPS.emptyMap()).isEqualTo(LinCompoundTag.builder().build());
    }

    @Test
    void toStringIsLinBus() {
        assertThat(OPS.toString()).isEqualTo("lin-bus");
    }

    @Test
    void getNumberValueReadsNumberTags() {
        assertThat(OPS.getNumberValue(LinIntTag.of(5))).hasResultThat().isEqualTo(5);
    }

    @Test
    void getNumberValueRejectsNonNumber() {
        assertThat(OPS.getNumberValue(LinStringTag.of("x"))).hasErrorWithMessageThat().startsWith("Not a number");
    }

    @Test
    @DisplayName("createNumeric collapses any Number to a double tag, including integral input")
    void createNumericMakesDouble() {
        assertThat(OPS.createNumeric(3.5)).isEqualTo(LinDoubleTag.of(3.5));
        assertThat(OPS.createNumeric(5)).isEqualTo(LinDoubleTag.of(5.0));
    }

    @Test
    void typedNumberCreatorsMatchTagTypes() {
        assertThat(OPS.createByte((byte) 1)).isEqualTo(LinByteTag.of((byte) 1));
        assertThat(OPS.createShort((short) 2)).isEqualTo(LinShortTag.of((short) 2));
        assertThat(OPS.createInt(3)).isEqualTo(LinIntTag.of(3));
        assertThat(OPS.createLong(4L)).isEqualTo(LinLongTag.of(4L));
        assertThat(OPS.createFloat(5.5f)).isEqualTo(LinFloatTag.of(5.5f));
        assertThat(OPS.createDouble(6.5)).isEqualTo(LinDoubleTag.of(6.5));
    }

    @Test
    @DisplayName("getBooleanValue is true for any non-zero number — negative, fractional, or out of byte range")
    void getBooleanValueIsAnyNonZeroNumber() {
        assertThat(OPS.getBooleanValue(LinByteTag.of((byte) 0))).hasResultThat().isEqualTo(false);
        assertThat(OPS.getBooleanValue(LinByteTag.of((byte) 1))).hasResultThat().isEqualTo(true);
        assertThat(OPS.getBooleanValue(LinIntTag.of(256))).hasResultThat().isEqualTo(true);
        assertThat(OPS.getBooleanValue(LinIntTag.of(-1))).hasResultThat().isEqualTo(true);
        assertThat(OPS.getBooleanValue(LinDoubleTag.of(0.5))).hasResultThat().isEqualTo(true);
        assertThat(OPS.getBooleanValue(LinDoubleTag.of(0.0))).hasResultThat().isEqualTo(false);
    }

    @Test
    void getBooleanValueRejectsNonNumber() {
        assertThat(OPS.getBooleanValue(LinStringTag.of("x"))).hasErrorWithMessageThat().startsWith("Not a number");
    }

    @Test
    void createBooleanMakesByte() {
        assertThat(OPS.createBoolean(true)).isEqualTo(LinByteTag.of((byte) 1));
        assertThat(OPS.createBoolean(false)).isEqualTo(LinByteTag.of((byte) 0));
    }

    @Test
    void getStringValueReadsStringTag() {
        assertThat(OPS.getStringValue(LinStringTag.of("abc"))).hasResultThat().isEqualTo("abc");
    }

    @Test
    void getStringValueRejectsNonString() {
        assertThat(OPS.getStringValue(LinIntTag.of(1))).hasErrorWithMessageThat().startsWith("Not a string");
    }

    @Test
    void createStringMakesStringTag() {
        assertThat(OPS.createString("abc")).isEqualTo(LinStringTag.of("abc"));
    }
}
