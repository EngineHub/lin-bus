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

package org.enginehub.linbus.stream;

import com.google.common.collect.ImmutableList;
import com.google.common.io.ByteStreams;
import org.enginehub.linbus.common.LinTagId;
import org.enginehub.linbus.stream.token.LinToken;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;

import static com.google.common.truth.Truth.assertThat;
import static org.enginehub.linbus.stream.StreamTestUtil.convertNbtStream;
import static org.enginehub.linbus.stream.StreamTestUtil.loadResource;

public class LinNbtStreamsIntegrationTest {
    @Test
    void bigtest() throws IOException {
        var theFirst1000Values = ByteBuffer.allocate(1000);
        for (int i = 0; i < theFirst1000Values.capacity(); i++) {
            theFirst1000Values.put(i, (byte) ((i * i * 255 + i * 7) % 100));
        }

        var bytes = loadResource("bigtest.nbt.gz", InputStream::readAllBytes);
        var tokens = convertNbtStream("bigtest.nbt.gz", ImmutableList::copyOf);
        assertThat(tokens).containsExactly(
            new LinToken.Name("Level", LinTagId.COMPOUND),
            new LinToken.CompoundStart(),
            new LinToken.Name("longTest", LinTagId.LONG),
            new LinToken.Long(9223372036854775807L),
            new LinToken.Name("shortTest", LinTagId.SHORT),
            new LinToken.Short((short) 32767),
            new LinToken.Name("stringTest", LinTagId.STRING),
            new LinToken.String("HELLO WORLD THIS IS A TEST STRING ÅÄÖ!"),
            new LinToken.Name("floatTest", LinTagId.FLOAT),
            new LinToken.Float(0.49823147F),
            new LinToken.Name("intTest", LinTagId.INT),
            new LinToken.Int(2147483647),
            new LinToken.Name("nested compound test", LinTagId.COMPOUND),
            new LinToken.CompoundStart(),
            new LinToken.Name("ham", LinTagId.COMPOUND),
            new LinToken.CompoundStart(),
            new LinToken.Name("name", LinTagId.STRING),
            new LinToken.String("Hampus"),
            new LinToken.Name("value", LinTagId.FLOAT),
            new LinToken.Float(0.75F),
            new LinToken.CompoundEnd(),
            new LinToken.Name("egg", LinTagId.COMPOUND),
            new LinToken.CompoundStart(),
            new LinToken.Name("name", LinTagId.STRING),
            new LinToken.String("Eggbert"),
            new LinToken.Name("value", LinTagId.FLOAT),
            new LinToken.Float(0.5F),
            new LinToken.CompoundEnd(),
            new LinToken.CompoundEnd(),
            new LinToken.Name("listTest (long)", LinTagId.LIST),
            new LinToken.ListStart(5, LinTagId.LONG),
            new LinToken.Long(11L),
            new LinToken.Long(12L),
            new LinToken.Long(13L),
            new LinToken.Long(14L),
            new LinToken.Long(15L),
            new LinToken.ListEnd(),
            new LinToken.Name("listTest (compound)", LinTagId.LIST),
            new LinToken.ListStart(2, LinTagId.COMPOUND),
            new LinToken.CompoundStart(),
            new LinToken.Name("name", LinTagId.STRING),
            new LinToken.String("Compound tag #0"),
            new LinToken.Name("created-on", LinTagId.LONG),
            new LinToken.Long(1264099775885L),
            new LinToken.CompoundEnd(),
            new LinToken.CompoundStart(),
            new LinToken.Name("name", LinTagId.STRING),
            new LinToken.String("Compound tag #1"),
            new LinToken.Name("created-on", LinTagId.LONG),
            new LinToken.Long(1264099775885L),
            new LinToken.CompoundEnd(),
            new LinToken.ListEnd(),
            new LinToken.Name("byteTest", LinTagId.BYTE),
            new LinToken.Byte((byte) 127),
            new LinToken.Name("byteArrayTest (the first 1000 values of (n*n*255+n*7)%100, starting with n=0 (0, 62, 34, 16, 8, ...))", LinTagId.BYTE_ARRAY),
            new LinToken.ByteArrayStart(1000),
            new LinToken.ByteArrayContent(theFirst1000Values.asReadOnlyBuffer()),
            new LinToken.ByteArrayEnd(),
            new LinToken.Name("doubleTest", LinTagId.DOUBLE),
            new LinToken.Double(0.4931287132182315),
            new LinToken.CompoundEnd()
        ).inOrder();

        var byteCollector = ByteStreams.newDataOutput();
        LinNbtStreams.write(byteCollector, tokens.iterator());
        assertThat(byteCollector.toByteArray()).isEqualTo(bytes);
    }

    @Test
    void allTypes() throws IOException {
        var bytes = loadResource("all-types.nbt.gz", InputStream::readAllBytes);
        var tokens = convertNbtStream("all-types.nbt.gz", ImmutableList::copyOf);
        assertThat(tokens).containsExactly(
            new LinToken.Name("root", LinTagId.COMPOUND),
            new LinToken.CompoundStart(),
            new LinToken.Name("byte", LinTagId.BYTE),
            new LinToken.Byte((byte) 1),
            new LinToken.Name("short", LinTagId.SHORT),
            new LinToken.Short((short) 127),
            new LinToken.Name("int", LinTagId.INT),
            new LinToken.Int(127),
            new LinToken.Name("long", LinTagId.LONG),
            new LinToken.Long(127),
            new LinToken.Name("float", LinTagId.FLOAT),
            new LinToken.Float(127),
            new LinToken.Name("double", LinTagId.DOUBLE),
            new LinToken.Double(127),
            new LinToken.Name("string", LinTagId.STRING),
            new LinToken.String("this is a string"),
            new LinToken.Name("byteArray", LinTagId.BYTE_ARRAY),
            new LinToken.ByteArrayStart(1),
            new LinToken.ByteArrayContent(ByteBuffer.wrap(new byte[]{(byte) 1}).asReadOnlyBuffer()),
            new LinToken.ByteArrayEnd(),
            new LinToken.Name("intArray", LinTagId.INT_ARRAY),
            new LinToken.IntArrayStart(1),
            new LinToken.IntArrayContent(IntBuffer.wrap(new int[]{127}).asReadOnlyBuffer()),
            new LinToken.IntArrayEnd(),
            new LinToken.Name("longArray", LinTagId.LONG_ARRAY),
            new LinToken.LongArrayStart(1),
            new LinToken.LongArrayContent(LongBuffer.wrap(new long[]{127}).asReadOnlyBuffer()),
            new LinToken.LongArrayEnd(),
            new LinToken.Name("byteList", LinTagId.LIST),
            new LinToken.ListStart(1, LinTagId.BYTE),
            new LinToken.Byte((byte) 1),
            new LinToken.ListEnd(),
            new LinToken.Name("shortList", LinTagId.LIST),
            new LinToken.ListStart(1, LinTagId.SHORT),
            new LinToken.Short((short) 127),
            new LinToken.ListEnd(),
            new LinToken.Name("intList", LinTagId.LIST),
            new LinToken.ListStart(1, LinTagId.INT),
            new LinToken.Int(127),
            new LinToken.ListEnd(),
            new LinToken.Name("longList", LinTagId.LIST),
            new LinToken.ListStart(1, LinTagId.LONG),
            new LinToken.Long(127),
            new LinToken.ListEnd(),
            new LinToken.Name("floatList", LinTagId.LIST),
            new LinToken.ListStart(1, LinTagId.FLOAT),
            new LinToken.Float(127),
            new LinToken.ListEnd(),
            new LinToken.Name("doubleList", LinTagId.LIST),
            new LinToken.ListStart(1, LinTagId.DOUBLE),
            new LinToken.Double(127),
            new LinToken.ListEnd(),
            new LinToken.Name("compound1", LinTagId.COMPOUND),
            new LinToken.CompoundStart(),
            new LinToken.Name("compound2", LinTagId.COMPOUND),
            new LinToken.CompoundStart(),
            new LinToken.Name("compound3", LinTagId.COMPOUND),
            new LinToken.CompoundStart(),
            new LinToken.Name("list", LinTagId.LIST),
            new LinToken.ListStart(2, LinTagId.COMPOUND),
            new LinToken.CompoundStart(),
            new LinToken.Name("key", LinTagId.STRING),
            new LinToken.String("value"),
            new LinToken.CompoundEnd(),
            new LinToken.CompoundStart(),
            new LinToken.Name("key", LinTagId.STRING),
            new LinToken.String("value"),
            new LinToken.CompoundEnd(),
            new LinToken.ListEnd(),
            new LinToken.CompoundEnd(),
            new LinToken.CompoundEnd(),
            new LinToken.CompoundEnd(),
            new LinToken.CompoundEnd()
        ).inOrder();

        var byteCollector = ByteStreams.newDataOutput();
        LinNbtStreams.write(byteCollector, tokens.iterator());
        assertThat(byteCollector.toByteArray()).isEqualTo(bytes);
    }
}
