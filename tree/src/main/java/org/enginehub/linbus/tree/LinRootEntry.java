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
import org.enginehub.linbus.common.internal.Iterators;
import org.enginehub.linbus.stream.LinNbtStreams;
import org.enginehub.linbus.stream.token.LinToken;
import org.enginehub.linbus.tree.impl.LinTagReader;
import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayOutputStream;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;

/**
 * Represents the root implicit-compound-tag entry.
 */
public record LinRootEntry(
    String name,
    LinCompoundTag value
) implements ToLinTag<LinCompoundTag>, Iterable<LinToken> {
    /**
     * Read a root entry from the given input.
     *
     * @param input the input to read from
     * @return the root entry
     */
    public static LinRootEntry readFrom(@NotNull DataInput input) {
        return readFrom(LinNbtStreams.read(input));
    }

    /**
     * Read a root entry from the given stream.
     *
     * @param tokens the stream to read from
     * @return the root entry
     */
    public static LinRootEntry readFrom(@NotNull Iterator<? extends LinToken> tokens) {
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
            writeTo(dataOutputStream);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return output.toByteArray();
    }

    /**
     * Write this entry to the given output.
     *
     * @param output the output to write to
     * @throws IOException if an I/O error occurs
     */
    public void writeTo(DataOutput output) throws IOException {
        LinNbtStreams.write(output, iterator());
    }

    /**
     * {@inheritDoc}
     * <p>
     * Note that the tag returned is not the same as {@link #value()}.
     */
    @Override
    public @NotNull LinCompoundTag toLinTag() {
        return new LinCompoundTag(Map.of(name, value), true);
    }

    @Override
    public @NotNull Iterator<LinToken> iterator() {
        return Iterators.combine(
            Iterators.of(new LinToken.Name(name, LinTagId.COMPOUND)),
            value.iterator()
        );
    }
}
