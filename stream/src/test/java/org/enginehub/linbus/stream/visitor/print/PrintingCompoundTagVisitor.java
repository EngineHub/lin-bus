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

public class PrintingCompoundTagVisitor extends PrintingVisitor implements LinCompoundTagVisitor {
    public PrintingCompoundTagVisitor(String context) {
        super(context);
    }

    @Override
    public LinByteArrayTagVisitor visitValueByteArray(String key) {
        print(key);
        return new PrintingByteArrayTagVisitor(nest(key));
    }

    @Override
    public LinByteTagVisitor visitValueByte(String key) {
        print(key);
        return new PrintingByteTagVisitor(nest(key));
    }

    @Override
    public LinCompoundTagVisitor visitValueCompound(String key) {
        print(key);
        return new PrintingCompoundTagVisitor(nest(key));
    }

    @Override
    public LinDoubleTagVisitor visitValueDouble(String key) {
        print(key);
        return new PrintingDoubleTagVisitor(nest(key));
    }

    @Override
    public LinFloatTagVisitor visitValueFloat(String key) {
        print(key);
        return new PrintingFloatTagVisitor(nest(key));
    }

    @Override
    public LinIntArrayTagVisitor visitValueIntArray(String key) {
        print(key);
        return new PrintingIntArrayTagVisitor(nest(key));
    }

    @Override
    public LinIntTagVisitor visitValueInt(String key) {
        print(key);
        return new PrintingIntTagVisitor(nest(key));
    }

    @Override
    public LinListTagVisitor visitValueList(String key) {
        print(key);
        return new PrintingListTagVisitor(nest(key));
    }

    @Override
    public LinLongArrayTagVisitor visitValueLongArray(String key) {
        print(key);
        return new PrintingLongArrayTagVisitor(nest(key));
    }

    @Override
    public LinLongTagVisitor visitValueLong(String key) {
        print(key);
        return new PrintingLongTagVisitor(nest(key));
    }

    @Override
    public LinShortTagVisitor visitValueShort(String key) {
        print(key);
        return new PrintingShortTagVisitor(nest(key));
    }

    @Override
    public LinStringTagVisitor visitValueString(String key) {
        print(key);
        return new PrintingStringTagVisitor(nest(key));
    }

    @Override
    public void visitEnd() {
        print();
    }
}
