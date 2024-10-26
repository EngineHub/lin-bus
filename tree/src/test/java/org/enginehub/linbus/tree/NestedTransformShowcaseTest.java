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

import static com.google.common.truth.Truth.assertThat;
import static org.enginehub.linbus.tree.truth.LinTagSubject.assertThat;

public class NestedTransformShowcaseTest {
    @Test
    void transformSchematicMetadata() {
        LinRootEntry root = new LinRootEntry(
            "",
            LinCompoundTag.builder()
                .put("Schematic", LinCompoundTag.builder()
                    .put("Metadata", LinCompoundTag.builder()
                        .put("Name", LinStringTag.of("My Schematic"))
                        .put("Author", LinStringTag.of("Linbus"))
                        .build())
                    .build())
                .build()
        );
        LinRootEntry withNewName = root.transformValue(v ->
            v.transformTag("Schematic", LinTagType.compoundTag(), schematic ->
                schematic.transformTag("Metadata", LinTagType.compoundTag(), metadata ->
                    metadata.transformTag("Name", LinTagType.stringTag(), name ->
                        LinStringTag.of("My New Schematic")
                    )
                )
            )
        );

        LinCompoundTag metadata = withNewName.value()
            .getTag("Schematic", LinTagType.compoundTag())
            .getTag("Metadata", LinTagType.compoundTag());
        assertThat(metadata).getTagByKey("Name").stringValue().isEqualTo("My New Schematic");
        assertThat(metadata).getTagByKey("Author").stringValue().isEqualTo("Linbus");
    }
}
