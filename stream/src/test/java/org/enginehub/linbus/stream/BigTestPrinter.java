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

import com.google.common.io.Resources;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;

public class BigTestPrinter {
    public static void main(String[] args) throws IOException {
        var resource = Resources.getResource("bigtest.nbt.gz");
        try (var stream = Resources.asByteSource(resource).openStream();
             var decompressed = new GZIPInputStream(stream);
             var buffered = new BufferedInputStream(decompressed)) {
            System.err.println("Here we go:");
            LinNbtStreams.read(new DataInputStream(buffered)).forEachRemaining(System.err::println);
            System.err.println("... that's all folks!");
        }
    }
}
