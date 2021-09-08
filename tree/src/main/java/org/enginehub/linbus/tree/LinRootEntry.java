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

import org.enginehub.linbus.stream.LinNbtReader;
import org.jetbrains.annotations.NotNull;

import java.io.DataInput;
import java.io.IOException;
import java.util.Map;
import java.util.Objects;

/**
 * Represents the root implicit-compound-tag entry.
 */
public record LinRootEntry(
    String name,
    LinCompoundTag value
) implements ToLinTag<LinCompoundTag> {
    public static LinRootEntry readFrom(DataInput input) throws IOException {
        var visitor = new TreeRootVisitor();
        LinNbtReader.accept(input, visitor);
        return visitor.result();
    }

    public LinRootEntry {
        Objects.requireNonNull(name);
        Objects.requireNonNull(value);
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
}
