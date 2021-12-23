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
import com.google.common.io.Resources;
import org.enginehub.linbus.common.LinTagId;
import org.enginehub.linbus.stream.token.LinToken;
import org.junit.jupiter.api.Test;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.Iterator;
import java.util.function.Function;
import java.util.zip.GZIPInputStream;

import static com.google.common.truth.Truth.assertThat;

public class LinNbtReaderIntegrationTest {

    private interface ResourceLoader<T> {
        T load(InputStream stream) throws IOException;
    }

    private static <T> T loadResource(String name, ResourceLoader<T> loader) throws IOException {
        var resource = Resources.getResource(name);
        try (var stream = Resources.asByteSource(resource).openStream();
             var decompressed = name.endsWith(".gz") ? new GZIPInputStream(stream) : stream;
             var buffered = new BufferedInputStream(decompressed)) {
            return loader.load(buffered);
        }
    }

    private static <T> T convertNbtStream(String name, Function<Iterator<? extends LinToken>, T> converter) throws IOException {
        return loadResource(name, stream -> converter.apply(LinNbtStreams.read(new DataInputStream(stream))));
    }

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
}
