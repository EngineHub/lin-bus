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

package org.enginehub.linbus.stream.visitor.print;

import org.enginehub.linbus.common.LinTagId;
import org.enginehub.linbus.stream.visitor.LinByteArrayTagVisitor;
import org.enginehub.linbus.stream.visitor.LinByteTagVisitor;
import org.enginehub.linbus.stream.visitor.LinCompoundTagVisitor;
import org.enginehub.linbus.stream.visitor.LinDoubleTagVisitor;
import org.enginehub.linbus.stream.visitor.LinFloatTagVisitor;
import org.enginehub.linbus.stream.visitor.LinIntArrayTagVisitor;
import org.enginehub.linbus.stream.visitor.LinIntTagVisitor;
import org.enginehub.linbus.stream.visitor.LinListTagVisitor;
import org.enginehub.linbus.stream.visitor.LinLongArrayTagVisitor;
import org.enginehub.linbus.stream.visitor.LinLongTagVisitor;
import org.enginehub.linbus.stream.visitor.LinShortTagVisitor;
import org.enginehub.linbus.stream.visitor.LinStringTagVisitor;

public class PrintingListTagVisitor extends PrintingVisitor implements LinListTagVisitor {
    public PrintingListTagVisitor(String context) {
        super(context);
    }

    @Override
    public LinByteArrayTagVisitor visitValueByteArray(Integer key) {
        print(key);
        return new PrintingByteArrayTagVisitor(nest(key.toString()));
    }

    @Override
    public LinByteTagVisitor visitValueByte(Integer key) {
        print(key);
        return new PrintingByteTagVisitor(nest(key.toString()));
    }

    @Override
    public LinCompoundTagVisitor visitValueCompound(Integer key) {
        print(key);
        return new PrintingCompoundTagVisitor(nest(key.toString()));
    }

    @Override
    public LinDoubleTagVisitor visitValueDouble(Integer key) {
        print(key);
        return new PrintingDoubleTagVisitor(nest(key.toString()));
    }

    @Override
    public LinFloatTagVisitor visitValueFloat(Integer key) {
        print(key);
        return new PrintingFloatTagVisitor(nest(key.toString()));
    }

    @Override
    public LinIntArrayTagVisitor visitValueIntArray(Integer key) {
        print(key);
        return new PrintingIntArrayTagVisitor(nest(key.toString()));
    }

    @Override
    public LinIntTagVisitor visitValueInt(Integer key) {
        print(key);
        return new PrintingIntTagVisitor(nest(key.toString()));
    }

    @Override
    public LinListTagVisitor visitValueList(Integer key) {
        print(key);
        return new PrintingListTagVisitor(nest(key.toString()));
    }

    @Override
    public LinLongArrayTagVisitor visitValueLongArray(Integer key) {
        print(key);
        return new PrintingLongArrayTagVisitor(nest(key.toString()));
    }

    @Override
    public LinLongTagVisitor visitValueLong(Integer key) {
        print(key);
        return new PrintingLongTagVisitor(nest(key.toString()));
    }

    @Override
    public LinShortTagVisitor visitValueShort(Integer key) {
        print(key);
        return new PrintingShortTagVisitor(nest(key.toString()));
    }

    @Override
    public LinStringTagVisitor visitValueString(Integer key) {
        print(key);
        return new PrintingStringTagVisitor(nest(key.toString()));
    }

    @Override
    public void visitSizeAndType(int size, LinTagId type) {
        print(size, type);
    }

    @Override
    public void visitEnd() {
        print();
    }
}
