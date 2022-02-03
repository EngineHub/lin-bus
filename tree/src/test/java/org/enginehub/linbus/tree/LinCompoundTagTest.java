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
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.NoSuchElementException;

import static com.google.common.truth.Truth.assertThat;
import static org.enginehub.linbus.tree.truth.LinTagSubject.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class LinCompoundTagTest {
    @Test
    void roundTrip() throws IOException {
        TagTestUtil.assertRoundTrip(new LinCompoundTag(Collections.emptyMap()));
        TagTestUtil.assertRoundTrip(new LinCompoundTag(new LinkedHashMap<>() {{
            put("Hello", new LinStringTag("World!"));
            put("Goodbye", new LinIntArrayTag(0xCAFE, 0xBABE));
        }}));
    }

    @Test
    void roundTripBuilder() {
        var initial = new LinCompoundTag(Map.of(
            "Hello", new LinStringTag("World!"),
            "Goodbye", new LinIntArrayTag(0xCAFE, 0xBABE)
        ));
        assertThat(initial).isEqualTo(initial.toBuilder().build());
    }

    @Test
    void builderRemove() {
        var initial = new LinCompoundTag(Map.of(
            "Hello", new LinStringTag("World!"),
            "Goodbye", new LinIntArrayTag(0xCAFE, 0xBABE)
        ));
        var afterRemove = initial.toBuilder().remove("Hello").build();
        assertThat(afterRemove).getTagByKey("Hello").isNull();
        assertThat(afterRemove).getTagByKey("Goodbye").intArrayValue().isEqualTo(new int[]{0xCAFE, 0xBABE});
    }

    @Test
    void checksForEndTag() {
        var ex = assertThrows(
            IllegalArgumentException.class,
            () -> new LinCompoundTag(Map.of("this is the end", LinEndTag.instance()), true)
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
    void canAvoidCheckForPerformance() {
        assertDoesNotThrow(
            () -> new LinCompoundTag(Map.of("this is the end", LinEndTag.instance()), false)
        );
    }

    @Test
    void toStringImplementation() {
        assertThat(new LinCompoundTag(new LinkedHashMap<>() {{
            put("Hello", new LinStringTag("World!"));
            put("Goodbye", new LinIntArrayTag(0xCAFE, 0xBABE));
        }}).toString())
            .isEqualTo("LinCompoundTag{Hello=LinStringTag[World!], Goodbye=LinIntArrayTag[51966, 47806]}");
    }

    @Test
    void putsAllFromMap() {
        var tag = LinCompoundTag.builder()
            .put("Initially here", new LinDoubleTag(1.0))
            .putAll(ImmutableMap.of(
                "Hello", new LinStringTag("World!"),
                "Goodbye", new LinIntArrayTag(0xCAFE, 0xBABE)
            ))
            .build();
        assertThat(tag).compoundValue().containsExactly(
            "Initially here", new LinDoubleTag(1.0),
            "Hello", new LinStringTag("World!"),
            "Goodbye", new LinIntArrayTag(0xCAFE, 0xBABE)
        ).inOrder();
    }

    @Test
    void getByName() {
        var tag = new LinCompoundTag(new LinkedHashMap<>() {{
            put("Hello", new LinStringTag("World!"));
            put("Goodbye", new LinIntArrayTag(0xCAFE, 0xBABE));
        }});
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
    void findByName() {
        var tag = new LinCompoundTag(new LinkedHashMap<>() {{
            put("Hello", new LinStringTag("World!"));
            put("Goodbye", new LinIntArrayTag(0xCAFE, 0xBABE));
        }});
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
    void builderSpecialization() {
        var tag = LinCompoundTag.builder()
            .putByteArray("byteArray", new byte[]{(byte) 0xCA, (byte) 0xFE})
            .putByte("byte", (byte) 0xBA)
            .putCompound("compound", Map.of("inner", new LinStringTag("inner")))
            .putDouble("double", 1.0)
            .putFloat("float", 1.0f)
            .putIntArray("intArray", new int[]{0xCAFE, 0xBABE})
            .putInt("int", 0xCAFE)
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
        assertThat(tag).getTagByKey("longArray").longArrayValue().isEqualTo(new long[]{0xCAFEBABE, 0xBAEB1ADE});
        assertThat(tag).getTagByKey("long").longValue().isEqualTo(0xCAFEBABE);
        assertThat(tag).getTagByKey("short").shortValue().isEqualTo((short) 0xCAFE);
        assertThat(tag).getTagByKey("string").stringValue().isEqualTo("Hello World!");
    }
}
