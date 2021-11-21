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

package org.enginehub.linbus.stream.visitor.writer;

import org.enginehub.linbus.stream.visitor.LinShortTagVisitor;

import java.io.DataOutput;
import java.io.IOException;
import java.io.UncheckedIOException;

public class ShortTagWriter implements LinShortTagVisitor {
    private final DataOutput output;

    public ShortTagWriter(DataOutput output) {
        this.output = output;
    }

    @Override
    public void visitShort(short value) {
        try {
            output.writeShort(value);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
