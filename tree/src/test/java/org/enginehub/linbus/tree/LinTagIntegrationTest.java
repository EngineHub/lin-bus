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

import com.google.common.io.ByteStreams;
import com.google.common.io.Resources;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;

import static com.google.common.truth.Truth.assertAbout;
import static com.google.common.truth.Truth.assertThat;
import static org.enginehub.linbus.tree.truth.LinTagSubject.linTags;

public class LinTagIntegrationTest {
    private record TestTagData(
        LinRootEntry root,
        byte[] serializedForm
    ) {
    }

    private static TestTagData load(String name) throws IOException {
        var resource = Resources.getResource(name);
        byte[] data;
        try (var stream = Resources.asByteSource(resource).openStream();
             var decompressed = name.endsWith(".gz") ? new GZIPInputStream(stream) : stream) {
            data = decompressed.readAllBytes();
        }
        return new TestTagData(
            LinRootEntry.readFrom(ByteStreams.newDataInput(data)),
            data
        );
    }

    @Test
    void bigtest() throws IOException {
        TestTagData tagData = load("bigtest.nbt.gz");
        var tagSubject = assertAbout(linTags()).that(tagData.root().toLinTag());
        var rootCompoundSubject = tagSubject.getTagByKey("Level");
        rootCompoundSubject.getTagByKey("nested compound test")
            .getTagByKey("egg")
            .getTagByKey("name")
            .valueIfString()
            .isEqualTo("Eggbert");
        rootCompoundSubject.getTagByKey("nested compound test")
            .getTagByKey("egg")
            .getTagByKey("value")
            .valueIfFloat()
            .isEqualTo(0.5F);
        rootCompoundSubject.getTagByKey("nested compound test")
            .getTagByKey("ham")
            .getTagByKey("name")
            .valueIfString()
            .isEqualTo("Hampus");
        rootCompoundSubject.getTagByKey("nested compound test")
            .getTagByKey("ham")
            .getTagByKey("value")
            .valueIfFloat()
            .isEqualTo(0.75F);
        rootCompoundSubject.getTagByKey("intTest").valueIfInt().isEqualTo(2147483647);
        rootCompoundSubject.getTagByKey("byteTest").valueIfByte().isEqualTo(127);
        rootCompoundSubject.getTagByKey("stringTest").valueIfString()
            .isEqualTo("HELLO WORLD THIS IS A TEST STRING \u00c5\u00c4\u00d6!");
        for (int i = 0; i < 5; i++) {
            rootCompoundSubject.getTagByKey("listTest (long)")
                .getTagByIndex(i).valueIfLong().isEqualTo(11 + i);
        }
        rootCompoundSubject.getTagByKey("doubleTest").valueIfDouble().isEqualTo(0.49312871321823148);
        rootCompoundSubject.getTagByKey("floatTest").valueIfFloat().isEqualTo(0.49823147058486938F);
        rootCompoundSubject.getTagByKey("longTest").valueIfLong().isEqualTo(9223372036854775807L);
        rootCompoundSubject.getTagByKey("listTest (compound)").getTagByIndex(0)
            .getTagByKey("created-on").valueIfLong().isEqualTo(1264099775885L);
        rootCompoundSubject.getTagByKey("listTest (compound)").getTagByIndex(0)
            .getTagByKey("name").valueIfString().isEqualTo("Compound tag #0");
        rootCompoundSubject.getTagByKey("listTest (compound)").getTagByIndex(1)
            .getTagByKey("created-on").valueIfLong().isEqualTo(1264099775885L);
        rootCompoundSubject.getTagByKey("listTest (compound)").getTagByIndex(1)
            .getTagByKey("name").valueIfString().isEqualTo("Compound tag #1");
        Byte[] expectedByteArray = new Byte[1000];
        for (int i = 0; i < expectedByteArray.length; i++) {
            expectedByteArray[i] = (byte) ((i * i * 255 + i * 7) % 100);
        }
        rootCompoundSubject.getTagByKey("byteArrayTest (the first 1000 values of (n*n*255+n*7)%100, starting with n=0 (0, 62, 34, 16, 8, ...))").valueIfByteArray().asList()
            .containsExactlyElementsIn(expectedByteArray).inOrder();
        rootCompoundSubject.getTagByKey("shortTest").valueIfShort().isEqualTo(32767);

        assertThat(tagData.root().writeToArray()).isEqualTo(tagData.serializedForm());
    }

    @Test
    void allTypes() throws IOException {
        TestTagData tagData = load("all-types.nbt.gz");
        var tagSubject = assertAbout(linTags()).that(tagData.root().toLinTag());
        var rootCompoundSubject = tagSubject.getTagByKey("root");
        rootCompoundSubject.getTagByKey("byte").valueIfByte().isEqualTo((byte) 1);
        rootCompoundSubject.getTagByKey("short").valueIfShort().isEqualTo((short) 127);
        rootCompoundSubject.getTagByKey("int").valueIfInt().isEqualTo(127);
        rootCompoundSubject.getTagByKey("long").valueIfLong().isEqualTo(127);
        rootCompoundSubject.getTagByKey("float").valueIfFloat().isEqualTo(127);
        rootCompoundSubject.getTagByKey("double").valueIfDouble().isEqualTo(127);
        rootCompoundSubject.getTagByKey("string").valueIfString().isEqualTo("this is a string");
        rootCompoundSubject.getTagByKey("byteArray").valueIfByteArray()
            .asList().isEqualTo(List.of((byte) 1));
        rootCompoundSubject.getTagByKey("intArray").valueIfIntArray()
            .asList().isEqualTo(List.of(127));
        rootCompoundSubject.getTagByKey("longArray").valueIfLongArray()
            .asList().isEqualTo(List.of(127L));
        rootCompoundSubject.getTagByKey("byteList").valueIfList().isEqualTo(List.of(new LinByteTag(1)));
        rootCompoundSubject.getTagByKey("shortList").valueIfList().isEqualTo(List.of(new LinShortTag((short) 127)));
        rootCompoundSubject.getTagByKey("intList").valueIfList().isEqualTo(List.of(new LinIntTag(127)));
        rootCompoundSubject.getTagByKey("longList").valueIfList().isEqualTo(List.of(new LinLongTag(127L)));
        rootCompoundSubject.getTagByKey("floatList").valueIfList().isEqualTo(List.of(new LinFloatTag(127F)));
        rootCompoundSubject.getTagByKey("doubleList").valueIfList().isEqualTo(List.of(new LinDoubleTag(127D)));
        var compound1Subject = rootCompoundSubject.getTagByKey("compound1");
        var compound2Subject = compound1Subject.getTagByKey("compound2");
        var compound3Subject = compound2Subject.getTagByKey("compound3");
        var listSubject = compound3Subject.getTagByKey("list");
        listSubject.valueIfList().hasSize(2);
        listSubject.getTagByIndex(0).valueIfCompound().isEqualTo(Map.of("key", new LinStringTag("value")));
        listSubject.getTagByIndex(1).valueIfCompound().isEqualTo(Map.of("key", new LinStringTag("value")));

        assertThat(tagData.root().writeToArray()).isEqualTo(tagData.serializedForm());
    }
}
