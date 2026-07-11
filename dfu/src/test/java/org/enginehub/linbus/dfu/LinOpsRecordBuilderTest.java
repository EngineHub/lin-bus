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

import com.mojang.serialization.DataResult;
import org.enginehub.linbus.tree.LinCompoundTag;
import org.enginehub.linbus.tree.LinEndTag;
import org.enginehub.linbus.tree.LinIntTag;
import org.enginehub.linbus.tree.LinStringTag;
import org.enginehub.linbus.tree.LinTag;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.enginehub.linbus.dfu.DataResultSubject.assertThat;

class LinOpsRecordBuilderTest {

    private static final LinOps OPS = LinOps.getInstance();

    @Test
    void buildOntoEndPrefixMakesCompound() {
        assertThat(OPS.mapBuilder().add("a", LinIntTag.of(1)).build(OPS.empty()))
            .hasResultThat().isEqualTo(LinCompoundTag.builder().putInt("a", 1).build());
    }

    @Test
    void buildOntoEmptyMapPrefixMakesCompound() {
        assertThat(OPS.mapBuilder().add("a", LinIntTag.of(1)).build(OPS.emptyMap()))
            .hasResultThat().isEqualTo(LinCompoundTag.builder().putInt("a", 1).build());
    }

    @Test
    void buildOntoCompoundPrefixKeepsExistingKeys() {
        assertThat(OPS.mapBuilder().add("a", LinIntTag.of(1))
            .build(LinCompoundTag.builder().putString("keep", "v").build()))
            .hasResultThat().isEqualTo(LinCompoundTag.builder().putString("keep", "v").putInt("a", 1).build());
    }

    @Test
    void buildOntoCompoundPrefixOverwritesExistingKeys() {
        assertThat(OPS.mapBuilder().add("keep", LinIntTag.of(9))
            .build(LinCompoundTag.builder().putString("keep", "v").build()))
            .hasResultThat().isEqualTo(LinCompoundTag.builder().putInt("keep", 9).build());
    }

    @Test
    void buildRejectsNonMapPrefix() {
        assertThat(OPS.mapBuilder().add("a", LinIntTag.of(1)).build(LinStringTag.of("x")))
            .hasErrorWithMessageThat().startsWith("mergeToMap called with non-map: ");
    }

    @Test
    @DisplayName("a null prefix builds a fresh compound, exactly as an END prefix does")
    void buildOntoNullPrefixMakesCompound() {
        assertThat(OPS.mapBuilder().add("a", LinIntTag.of(1)).build((LinTag<?>) null))
            .hasResultThat().isEqualTo(LinCompoundTag.builder().putInt("a", 1).build());
    }

    @Test
    void addRejectsEndValue() {
        assertThat(OPS.mapBuilder().add("a", LinEndTag.instance()).build(OPS.empty()))
            .hasErrorWithMessageThat().startsWith("Cannot add END tag to compound: ");
    }

    @Test
    void addRejectsEndValueFromDataResult() {
        assertThat(
            OPS.mapBuilder().add("a", DataResult.success((LinTag<?>) LinEndTag.instance())).build(OPS.empty())
        ).hasErrorWithMessageThat().startsWith("Cannot add END tag to compound: ");
    }
}
