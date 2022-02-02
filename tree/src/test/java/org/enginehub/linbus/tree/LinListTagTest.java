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
import java.util.List;

import static com.google.common.truth.Truth.assertThat;
import static org.enginehub.linbus.tree.truth.LinTagSubject.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class LinListTagTest {
    @Test
    void roundTrip() throws IOException {
        TagTestUtil.assertRoundTrip(new LinListTag<>(LinTagType.stringTag(), List.of(
            new LinStringTag("Hello"),
            new LinStringTag("World!")
        )));
        TagTestUtil.assertRoundTrip(new LinListTag<>(LinTagType.doubleTag(), List.of(
            new LinDoubleTag(Double.POSITIVE_INFINITY),
            new LinDoubleTag(0.0),
            new LinDoubleTag(-0.0)
        )));
    }

    @Test
    void roundTripBuilder() {
        var initial = new LinListTag<>(LinTagType.stringTag(), List.of(
            new LinStringTag("Hello"),
            new LinStringTag("World!")
        ));
        assertThat(initial).isEqualTo(initial.toBuilder().build());
    }

    @Test
    void emptyImplementation() {
        var empty = LinListTag.empty(LinTagType.stringTag());
        assertThat(empty).valueIfList().isEmpty();
        assertThat(empty.elementType()).isEqualTo(LinTagType.stringTag());
    }

    @Test
    void toStringImplementation() {
        assertThat(new LinListTag<>(LinTagType.stringTag(), List.of(
            new LinStringTag("Hello"),
            new LinStringTag("World!")
        )).toString()).isEqualTo("LinListTag[LinStringTag[Hello], LinStringTag[World!]]");
    }

    @Test
    void throwsIfAddingBadTag() {
        @SuppressWarnings({"unchecked", "rawtypes"})
        var thrown = assertThrows(
            IllegalArgumentException.class,
            () -> LinListTag.builder((LinTagType) LinTagType.stringTag())
                .add(new LinDoubleTag(0.0))
        );
        assertThat(thrown).hasMessageThat().isEqualTo("Element is not of type STRING but DOUBLE");
    }

    @Test
    void throwsIfConstructedWithBadTag() {
        @SuppressWarnings({"unchecked", "rawtypes"})
        var thrown = assertThrows(
            IllegalArgumentException.class,
            () -> new LinListTag<>((LinTagType) LinTagType.stringTag(), List.of(
                new LinDoubleTag(0.0)
            ))
        );
        assertThat(thrown).hasMessageThat().isEqualTo("Element is not of type STRING but DOUBLE");
    }

    @Test
    void cannotCreateNonEmptyEndList() {
        {
            var thrown = assertThrows(
                IllegalArgumentException.class,
                () -> LinListTag.builder(LinTagType.endTag())
                    .add(LinEndTag.instance())
                    .build()
            );
            assertThat(thrown).hasMessageThat().isEqualTo("A non-empty list cannot be of type END");
        }
        {
            var thrown = assertThrows(
                IllegalArgumentException.class,
                () -> new LinListTag<>(LinTagType.endTag(), List.of(LinEndTag.instance()))
            );
            assertThat(thrown).hasMessageThat().isEqualTo("A non-empty list cannot be of type END");
        }
    }

    @Test
    void addsAllFromCollection() {
        var tag = LinListTag.builder(LinTagType.stringTag())
            .add(new LinStringTag("I'm first!"))
            .addAll(List.of(new LinStringTag("Hello"), new LinStringTag("World!")))
            .build();
        assertThat(tag).valueIfList().containsExactly(
            new LinStringTag("I'm first!"),
            new LinStringTag("Hello"),
            new LinStringTag("World!")
        ).inOrder();
    }
}
