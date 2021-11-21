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

import org.enginehub.linbus.common.LinTagId;
import org.enginehub.linbus.stream.visitor.LinByteArrayTagVisitor;
import org.enginehub.linbus.stream.visitor.LinByteTagVisitor;
import org.enginehub.linbus.stream.visitor.LinCompoundTagVisitor;
import org.enginehub.linbus.stream.visitor.LinContainerVisitor;
import org.enginehub.linbus.stream.visitor.LinDoubleTagVisitor;
import org.enginehub.linbus.stream.visitor.LinFloatTagVisitor;
import org.enginehub.linbus.stream.visitor.LinIntArrayTagVisitor;
import org.enginehub.linbus.stream.visitor.LinIntTagVisitor;
import org.enginehub.linbus.stream.visitor.LinListTagVisitor;
import org.enginehub.linbus.stream.visitor.LinLongArrayTagVisitor;
import org.enginehub.linbus.stream.visitor.LinLongTagVisitor;
import org.enginehub.linbus.stream.visitor.LinShortTagVisitor;
import org.enginehub.linbus.stream.visitor.LinStringTagVisitor;

import java.io.DataOutput;

public abstract class ContainerWriter<K> implements LinContainerVisitor<K> {
    protected final DataOutput output;

    protected ContainerWriter(DataOutput output) {
        this.output = output;
    }

    protected abstract void writeHeader(LinTagId id, K key);

    @Override
    public LinByteArrayTagVisitor visitValueByteArray(K key) {
        writeHeader(LinTagId.BYTE_ARRAY, key);
        return new ByteArrayTagWriter(output);
    }

    @Override
    public LinByteTagVisitor visitValueByte(K key) {
        writeHeader(LinTagId.BYTE, key);
        return new ByteTagWriter(output);
    }

    @Override
    public LinCompoundTagVisitor visitValueCompound(K key) {
        writeHeader(LinTagId.COMPOUND, key);
        return new CompoundTagWriter(output);
    }

    @Override
    public LinDoubleTagVisitor visitValueDouble(K key) {
        writeHeader(LinTagId.DOUBLE, key);
        return new DoubleTagWriter(output);
    }

    @Override
    public LinFloatTagVisitor visitValueFloat(K key) {
        writeHeader(LinTagId.FLOAT, key);
        return new FloatTagWriter(output);
    }

    @Override
    public LinIntArrayTagVisitor visitValueIntArray(K key) {
        writeHeader(LinTagId.INT_ARRAY, key);
        return new IntArrayTagWriter(output);
    }

    @Override
    public LinIntTagVisitor visitValueInt(K key) {
        writeHeader(LinTagId.INT, key);
        return new IntTagWriter(output);
    }

    @Override
    public LinListTagVisitor visitValueList(K key) {
        writeHeader(LinTagId.LIST, key);
        return new ListTagWriter(output);
    }

    @Override
    public LinLongArrayTagVisitor visitValueLongArray(K key) {
        writeHeader(LinTagId.LONG_ARRAY, key);
        return new LongArrayTagWriter(output);
    }

    @Override
    public LinLongTagVisitor visitValueLong(K key) {
        writeHeader(LinTagId.LONG, key);
        return new LongTagWriter(output);
    }

    @Override
    public LinShortTagVisitor visitValueShort(K key) {
        writeHeader(LinTagId.SHORT, key);
        return new ShortTagWriter(output);
    }

    @Override
    public LinStringTagVisitor visitValueString(K key) {
        writeHeader(LinTagId.STRING, key);
        return new StringTagWriter(output);
    }
}
