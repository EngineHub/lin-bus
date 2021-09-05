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

package org.enginehub.linbus.stream.visitor;

public interface LinContainerVisitor<K> {
    default LinByteArrayTagVisitor visitValueByteArray(K key) {
        return LinByteArrayTagVisitor.defaultInstance();
    }

    default LinByteTagVisitor visitValueByte(K key) {
        return LinByteTagVisitor.defaultInstance();
    }

    default LinCompoundTagVisitor visitValueCompound(K key) {
        return LinCompoundTagVisitor.defaultInstance();
    }

    default LinDoubleTagVisitor visitValueDouble(K key) {
        return LinDoubleTagVisitor.defaultInstance();
    }

    default LinFloatTagVisitor visitValueFloat(K key) {
        return LinFloatTagVisitor.defaultInstance();
    }

    default LinIntArrayTagVisitor visitValueIntArray(K key) {
        return LinIntArrayTagVisitor.defaultInstance();
    }

    default LinIntTagVisitor visitValueInt(K key) {
        return LinIntTagVisitor.defaultInstance();
    }

    default LinListTagVisitor visitValueList(K key) {
        return LinListTagVisitor.defaultInstance();
    }

    default LinLongArrayTagVisitor visitValueLongArray(K key) {
        return LinLongArrayTagVisitor.defaultInstance();
    }

    default LinLongTagVisitor visitValueLong(K key) {
        return LinLongTagVisitor.defaultInstance();
    }

    default LinShortTagVisitor visitValueShort(K key) {
        return LinShortTagVisitor.defaultInstance();
    }

    default LinStringTagVisitor visitValueString(K key) {
        return LinStringTagVisitor.defaultInstance();
    }

    void visitEnd();
}
