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

import org.enginehub.linbus.common.LinTagId;
import org.enginehub.linbus.stream.LinBinaryIO;
import org.enginehub.linbus.stream.LinStream;
import org.enginehub.linbus.stream.LinStreamable;
import org.enginehub.linbus.stream.internal.SurroundingLinStream;
import org.enginehub.linbus.stream.token.LinToken;
import org.enginehub.linbus.tree.impl.LinTagReader;
import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Map;
import java.util.Objects;

/**
 * Represents the root implicit-compound-tag entry.
 */
public record LinRootEntry(
    String name,
    LinCompoundTag value
) implements ToLinTag<LinCompoundTag>, LinStreamable {
    /**
     * Read a root entry from the given stream.
     *
     * @param tokens the stream to read from
     * @return the root entry
     * @throws IOException if an I/O error occurs
     */
    public static LinRootEntry readFrom(@NotNull LinStream tokens) throws IOException {
        return LinTagReader.readRoot(tokens);
    }

    /**
     * Create a new root entry with the given name and value.
     *
     * @param name the name of the entry
     * @param value the value of the entry
     */
    public LinRootEntry {
        Objects.requireNonNull(name);
        Objects.requireNonNull(value);
    }

    /**
     * Write this entry to a byte array.
     *
     * @return the byte array
     */
    public byte[] writeToArray() {
        var output = new ByteArrayOutputStream();
        try (var dataOutputStream = new DataOutputStream(output)) {
            LinBinaryIO.write(dataOutputStream, this);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return output.toByteArray();
    }

    /**
     * {@inheritDoc}
     * <p>
     * Note that the tag returned is not the same as {@link #value()}.
     */
    @Override
    public @NotNull LinCompoundTag toLinTag() {
        return LinCompoundTag.of(Map.of(name, value));
    }

    @Override
    public @NotNull LinStream linStream() {
        return new SurroundingLinStream(
            new LinToken.Name(name, LinTagId.COMPOUND),
            value.linStream(),
            null
        );
    }
}
