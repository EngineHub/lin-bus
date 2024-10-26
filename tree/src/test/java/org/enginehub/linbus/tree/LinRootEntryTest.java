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

import java.util.Map;

import static com.google.common.truth.Truth.assertThat;

public class LinRootEntryTest {
    @Test
    void transformName() {
        var root = new LinRootEntry("old", LinCompoundTag.of(Map.of("Hello", LinStringTag.of("World!"))));
        var renamed = root.transformName(n -> n + " new");
        assertThat(renamed.name()).isEqualTo("old new");
        assertThat(renamed.value()).isEqualTo(root.value());
    }

    @Test
    void transformValue() {
        var root = new LinRootEntry("root", LinCompoundTag.of(Map.of("Hello", LinStringTag.of("World!"))));
        var updated = root.transformValue(
            v -> v.transformTag("Hello", LinTagType.stringTag(), t -> LinStringTag.of("New World!"))
        );
        assertThat(updated.name()).isEqualTo("root");
        assertThat(updated.value()).isEqualTo(LinCompoundTag.of(Map.of("Hello", LinStringTag.of("New World!"))));
    }
}
