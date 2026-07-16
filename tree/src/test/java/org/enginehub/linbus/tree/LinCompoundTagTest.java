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

import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import static com.google.common.truth.Truth.assertThat;
import static org.enginehub.linbus.tree.truth.LinTagSubject.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class LinCompoundTagTest {
    @Test
    void roundTrip() throws IOException {
        TagTestUtil.assertRoundTrip(LinCompoundTag.of(Collections.emptyMap()));
        TagTestUtil.assertRoundTrip(LinCompoundTag.builder()
            .put("Hello", LinStringTag.of("World!"))
            .put("Goodbye", LinIntArrayTag.of(0xCAFE, 0xBABE))
            .build());
    }

    @Test
    void roundTripBuilder() {
        var initial = LinCompoundTag.of(Map.of(
            "Hello", LinStringTag.of("World!"),
            "Goodbye", LinIntArrayTag.of(0xCAFE, 0xBABE)
        ));
        assertThat(initial).isEqualTo(initial.toBuilder().build());
    }

    @Test
    void emptyImplementation() {
        assertThat(LinCompoundTag.empty()).compoundValue().isEmpty();
        assertThat(LinCompoundTag.empty()).isSameInstanceAs(LinCompoundTag.empty());
    }

    @Test
    void builderReturnsEmptySingleton() {
        assertThat(LinCompoundTag.builder().build()).isSameInstanceAs(LinCompoundTag.empty());
    }

    @Test
    void ofEmptyReturnsEmptySingleton() {
        assertThat(LinCompoundTag.of(Map.of())).isSameInstanceAs(LinCompoundTag.empty());
    }

    @Test
    void builderRemove() {
        var initial = LinCompoundTag.of(Map.of(
            "Hello", LinStringTag.of("World!"),
            "Goodbye", LinIntArrayTag.of(0xCAFE, 0xBABE)
        ));
        var afterRemove = initial.toBuilder().remove("Hello").build();
        assertThat(afterRemove).getTagByKey("Hello").isNull();
        assertThat(afterRemove).getTagByKey("Goodbye").intArrayValue().isEqualTo(new int[]{0xCAFE, 0xBABE});
    }

    @Test
    void checksForEndTag() {
        var ex = assertThrows(
            IllegalArgumentException.class,
            () -> LinCompoundTag.of(Map.of("this is the end", LinEndTag.instance()))
        );
        assertThat(ex).hasMessageThat().isEqualTo("Cannot add END tag to compound tag");
    }

    @Test
    void builderChecksForEndTag() {
        var ex = assertThrows(
            IllegalArgumentException.class,
            () -> LinCompoundTag.builder().put("this is the end", LinEndTag.instance())
        );
        assertThat(ex).hasMessageThat().isEqualTo("Cannot add END tag to compound tag");
    }

    @Test
    void toStringImplementation() {
        assertThat(LinCompoundTag.builder()
            .put("Hello", LinStringTag.of("World!"))
            .put("Goodbye", LinIntArrayTag.of(0xCAFE, 0xBABE))
            .build().toString())
            .isEqualTo("LinCompoundTag{Hello=LinStringTag[World!], Goodbye=LinIntArrayTag[51966, 47806]}");
    }

    @Test
    void putsAllFromMap() {
        var tag = LinCompoundTag.builder()
            .put("Initially here", LinDoubleTag.of(1.0))
            .putAll(ImmutableMap.of(
                "Hello", LinStringTag.of("World!"),
                "Goodbye", LinIntArrayTag.of(0xCAFE, 0xBABE)
            ))
            .build();
        assertThat(tag).compoundValue().containsExactly(
            "Initially here", LinDoubleTag.of(1.0),
            "Hello", LinStringTag.of("World!"),
            "Goodbye", LinIntArrayTag.of(0xCAFE, 0xBABE)
        ).inOrder();
    }

    @Test
    void getByName() {
        var tag = LinCompoundTag.builder()
            .put("Hello", LinStringTag.of("World!"))
            .put("Goodbye", LinIntArrayTag.of(0xCAFE, 0xBABE))
            .build();
        assertThat(tag.getTag("Hello", LinTagType.stringTag()))
            .stringValue()
            .isEqualTo("World!");
        assertThat(tag.getTag("Goodbye", LinTagType.intArrayTag()))
            .intArrayValue()
            .isEqualTo(new int[]{0xCAFE, 0xBABE});
        {
            var thrown = assertThrows(
                IllegalStateException.class,
                () -> tag.getTag("Hello", LinTagType.longArrayTag())
            );
            assertThat(thrown).hasMessageThat().isEqualTo(
                "Tag under 'Hello' exists, but is a STRING instead of LONG_ARRAY"
            );
        }

        {
            var thrown = assertThrows(
                NoSuchElementException.class,
                () -> tag.getTag("this key does not exist", LinTagType.stringTag())
            );
            assertThat(thrown).hasMessageThat().isEqualTo(
                "No tag under the name 'this key does not exist' exists"
            );
        }
    }

    @Test
    void transformByName() {
        var tag = LinCompoundTag.of(Map.of(
            "Hello", LinStringTag.of("World!"),
            "Goodbye", LinIntArrayTag.of(0xCAFE, 0xBABE)
        ));
        var transformed = tag.transformTag("Hello", LinTagType.stringTag(), _ -> LinStringTag.of("New World!"));
        assertThat(transformed).isEqualTo(LinCompoundTag.of(Map.of(
            "Hello", LinStringTag.of("New World!"),
            "Goodbye", LinIntArrayTag.of(0xCAFE, 0xBABE)
        )));

        var transformedToNewType = tag.transformTag("Hello", LinTagType.stringTag(), _ -> LinIntArrayTag.of(0xDEAD, 0xBEEF));
        assertThat(transformedToNewType).isEqualTo(LinCompoundTag.of(Map.of(
            "Hello", LinIntArrayTag.of(0xDEAD, 0xBEEF),
            "Goodbye", LinIntArrayTag.of(0xCAFE, 0xBABE)
        )));
    }

    @Test
    void transformByNameThrows() {
        var tag = LinCompoundTag.of(Map.of("Hello", LinStringTag.of("World!")));
        {
            var ex = assertThrows(
                NoSuchElementException.class,
                () -> tag.transformTag("Nope", LinTagType.stringTag(), t -> t)
            );
            assertThat(ex).hasMessageThat().isEqualTo("No tag under the name 'Nope' exists");
        }
        {
            var ex = assertThrows(
                IllegalStateException.class,
                () -> tag.transformTag("Hello", LinTagType.intTag(), t -> t)
            );
            assertThat(ex).hasMessageThat().isEqualTo("Tag under 'Hello' exists, but is a STRING instead of INT");
        }
    }

    @Test
    void transformRejectsEndTag() {
        var tag = LinCompoundTag.of(Map.of("Hello", LinStringTag.of("World!")));
        var ex = assertThrows(
            IllegalArgumentException.class,
            () -> tag.transformTag("Hello", LinTagType.stringTag(), _ -> LinEndTag.instance())
        );
        assertThat(ex).hasMessageThat().isEqualTo("Cannot add END tag to compound tag");
    }

    @Test
    void transformRejectsNullResult() {
        var tag = LinCompoundTag.of(Map.of("Hello", LinStringTag.of("World!")));
        assertThrows(
            NullPointerException.class,
            () -> tag.transformTag("Hello", LinTagType.stringTag(), _ -> null)
        );
    }

    @Test
    void transformListByName() {
        var tag = LinCompoundTag.of(Map.of(
            "list", LinListTag.of(LinTagType.stringTag(), List.of(LinStringTag.of("a")))
        ));
        var transformed = tag.transformListTag(
            "list", LinTagType.stringTag(), l -> l.toBuilder().add(LinStringTag.of("b")).build()
        );
        assertThat(transformed).getTagByKey("list").listValue().containsExactly(
            LinStringTag.of("a"), LinStringTag.of("b")
        ).inOrder();
        {
            var ex = assertThrows(
                IllegalStateException.class,
                () -> tag.transformListTag("list", LinTagType.intTag(), l -> l)
            );
            assertThat(ex).hasMessageThat().isEqualTo(
                "Tag under 'list' exists, but is a STRING list instead of a INT list"
            );
        }
        {
            var ex = assertThrows(
                NoSuchElementException.class,
                () -> tag.transformListTag("nope", LinTagType.stringTag(), l -> l)
            );
            assertThat(ex).hasMessageThat().isEqualTo("No tag under the name 'nope' exists");
        }
    }

    @Test
    void transformIfPresentByName() {
        var tag = LinCompoundTag.of(Map.of("Hello", LinStringTag.of("World!")));
        var transformed = tag.transformTagIfPresent(
            "Hello", LinTagType.stringTag(), _ -> LinStringTag.of("New World!")
        );
        assertThat(transformed).getTagByKey("Hello").stringValue().isEqualTo("New World!");

        assertThat(tag.transformTagIfPresent("Nope", LinTagType.stringTag(), _ -> LinStringTag.of("x")))
            .isSameInstanceAs(tag);

        var ex = assertThrows(
            IllegalStateException.class,
            () -> tag.transformTagIfPresent("Hello", LinTagType.intTag(), t -> t)
        );
        assertThat(ex).hasMessageThat().isEqualTo("Tag under 'Hello' exists, but is a STRING instead of INT");
    }

    @Test
    void transformOrInsertByName() {
        var tag = LinCompoundTag.of(Map.of("Hello", LinStringTag.of("World!")));

        var updated = tag.transformTagOrInsert("Hello", LinTagType.stringTag(), t -> {
            assertThat(t).isNotNull();
            return LinStringTag.of("New World!");
        });
        assertThat(updated).getTagByKey("Hello").stringValue().isEqualTo("New World!");

        var inserted = tag.transformTagOrInsert("Added", LinTagType.stringTag(), t -> {
            assertThat(t).isNull();
            return LinStringTag.of("brand new");
        });
        assertThat(inserted).compoundValue().containsExactly(
            "Hello", LinStringTag.of("World!"),
            "Added", LinStringTag.of("brand new")
        ).inOrder();

        {
            var ex = assertThrows(
                IllegalStateException.class,
                () -> tag.transformTagOrInsert("Hello", LinTagType.intTag(), t -> t)
            );
            assertThat(ex).hasMessageThat().isEqualTo("Tag under 'Hello' exists, but is a STRING instead of INT");
        }
        assertThrows(
            NullPointerException.class,
            () -> tag.transformTagOrInsert("Hello", LinTagType.stringTag(), _ -> null)
        );
    }

    @Test
    void transformListIfPresentAndOrInsert() {
        var tag = LinCompoundTag.of(Map.of(
            "list", LinListTag.of(LinTagType.stringTag(), List.of(LinStringTag.of("a"))),
            "notList", LinStringTag.of("x")
        ));

        var appended = tag.transformListTagIfPresent(
            "list", LinTagType.stringTag(), l -> l.toBuilder().add(LinStringTag.of("b")).build()
        );
        assertThat(appended).getTagByKey("list").listValue().containsExactly(
            LinStringTag.of("a"), LinStringTag.of("b")
        ).inOrder();

        assertThat(tag.transformListTagIfPresent("nope", LinTagType.stringTag(), l -> l))
            .isSameInstanceAs(tag);

        var inserted = tag.transformListTagOrInsert("added", LinTagType.stringTag(), l -> {
            assertThat(l).isNull();
            return LinListTag.of(LinTagType.stringTag(), List.of(LinStringTag.of("new")));
        });
        assertThat(inserted).getTagByKey("added").listValue().containsExactly(LinStringTag.of("new"));

        {
            var ex = assertThrows(
                IllegalStateException.class,
                () -> tag.transformListTagIfPresent("list", LinTagType.intTag(), l -> l)
            );
            assertThat(ex).hasMessageThat().isEqualTo(
                "Tag under 'list' exists, but is a STRING list instead of a INT list"
            );
        }
        {
            var ex = assertThrows(
                IllegalStateException.class,
                () -> tag.transformListTagIfPresent("notList", LinTagType.stringTag(), l -> l)
            );
            assertThat(ex).hasMessageThat().isEqualTo("Tag under 'notList' exists, but is a STRING instead of LIST");
        }
    }

    @Test
    void findByName() {
        var tag = LinCompoundTag.builder()
            .put("Hello", LinStringTag.of("World!"))
            .put("Goodbye", LinIntArrayTag.of(0xCAFE, 0xBABE))
            .build();
        assertThat(tag.findTag("Hello", LinTagType.stringTag()))
            .stringValue()
            .isEqualTo("World!");
        assertThat(tag.findTag("Goodbye", LinTagType.intArrayTag()))
            .intArrayValue()
            .isEqualTo(new int[]{0xCAFE, 0xBABE});
        assertThat(tag.findTag("Hello", LinTagType.longArrayTag()))
            .isNull();

        assertThat(tag.findTag("this key does not exist", LinTagType.stringTag()))
            .isNull();
    }

    @Test
    void findListTag() {
        var tag = LinCompoundTag.builder()
            .put("Hello", LinListTag.empty(LinTagType.stringTag()))
            .build();
        var helloList = tag.findListTag("Hello", LinTagType.stringTag());
        assertThat(helloList).isNotNull();
        assertThat(helloList).listValue().isEmpty();
        assertThat(tag.findListTag("Hello", LinTagType.longArrayTag()))
            .isNull();
        assertThat(tag.findListTag("Not Here", LinTagType.longArrayTag()))
            .isNull();
    }

    @Test
    void getListTag() {
        var tag = LinCompoundTag.builder()
            .put("Hello", LinListTag.empty(LinTagType.stringTag()))
            .build();
        var helloList = tag.getListTag("Hello", LinTagType.stringTag());
        assertThat(helloList).isNotNull();
        assertThat(helloList).listValue().isEmpty();
        {
            var ex = assertThrows(
                IllegalStateException.class,
                () -> tag.getListTag("Hello", LinTagType.longArrayTag())
            );
            assertThat(ex).hasMessageThat().isEqualTo("Tag under 'Hello' exists, but is a STRING list instead of a LONG_ARRAY list");
        }
        {
            var ex = assertThrows(
                NoSuchElementException.class,
                () -> tag.getListTag("Not Here", LinTagType.longArrayTag())
            );
            assertThat(ex).hasMessageThat().isEqualTo("No tag under the name 'Not Here' exists");
        }
    }

    @Test
    void builderSpecialization() {
        var tag = LinCompoundTag.builder()
            .putByteArray("byteArray", new byte[]{(byte) 0xCA, (byte) 0xFE})
            .putByte("byte", (byte) 0xBA)
            .putCompound("compound", Map.of("inner", LinStringTag.of("inner")))
            .putDouble("double", 1.0)
            .putFloat("float", 1.0f)
            .putIntArray("intArray", new int[]{0xCAFE, 0xBABE})
            .putInt("int", 0xCAFE)
            .putList("list", LinTagType.stringTag(), List.of(LinStringTag.of("a"), LinStringTag.of("b")))
            .putLongArray("longArray", new long[]{0xCAFEBABE, 0xBAEB1ADE})
            .putLong("long", 0xCAFEBABE)
            .putShort("short", (short) 0xCAFE)
            .putString("string", "Hello World!")
            .build();
        assertThat(tag).getTagByKey("byteArray").byteArrayValue().isEqualTo(new byte[]{(byte) 0xCA, (byte) 0xFE});
        assertThat(tag).getTagByKey("byte").byteValue().isEqualTo((byte) 0xBA);
        assertThat(tag).getTagByKey("compound").getTagByKey("inner").stringValue().isEqualTo("inner");
        assertThat(tag).getTagByKey("double").doubleValue().isEqualTo(1.0);
        assertThat(tag).getTagByKey("float").floatValue().isEqualTo(1.0f);
        assertThat(tag).getTagByKey("intArray").intArrayValue().isEqualTo(new int[]{0xCAFE, 0xBABE});
        assertThat(tag).getTagByKey("int").intValue().isEqualTo(0xCAFE);
        assertThat(tag).getTagByKey("list").listValue()
            .containsExactly(LinStringTag.of("a"), LinStringTag.of("b")).inOrder();
        assertThat(tag).getTagByKey("longArray").longArrayValue().isEqualTo(new long[]{0xCAFEBABE, 0xBAEB1ADE});
        assertThat(tag).getTagByKey("long").longValue().isEqualTo(0xCAFEBABE);
        assertThat(tag).getTagByKey("short").shortValue().isEqualTo((short) 0xCAFE);
        assertThat(tag).getTagByKey("string").stringValue().isEqualTo("Hello World!");
    }
}
