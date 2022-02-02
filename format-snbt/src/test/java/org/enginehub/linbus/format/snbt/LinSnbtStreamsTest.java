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

package org.enginehub.linbus.format.snbt;

import com.google.common.io.Resources;
import org.enginehub.linbus.stream.LinNbtStreams;
import org.enginehub.linbus.stream.token.LinToken;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.function.Function;
import java.util.zip.GZIPInputStream;

public class LinSnbtStreamsTest {
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

    private static <T> T convertNbtStream(String name, Function<Iterator<? extends @NotNull LinToken>, T> converter) throws IOException {
        return loadResource(name, stream -> {
            Iterator<? extends @NotNull LinToken> iter = LinNbtStreams.read(new DataInputStream(stream));
            // Hack - SNBT can't print the RootEntry stuff, so we have to skip it
            iter.next();
            return converter.apply(iter);
        });
    }

    @Test
    void bigTestRoundTrip() throws IOException {
        var output = convertNbtStream("bigtest.nbt.gz", LinSnbtStreams::writeToString);

        System.err.println(output);
    }

    @Test
    void allTypesRoundTrip() throws IOException {
        var output = convertNbtStream("all-types.nbt.gz", LinSnbtStreams::writeToString);

        System.err.println(output);
    }
}
