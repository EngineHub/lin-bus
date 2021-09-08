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

import org.enginehub.linbus.stream.visitor.LinByteArrayTagVisitor;

import java.nio.ByteBuffer;
import java.util.function.Consumer;

class TreeByteArrayVisitor extends TreeVisitor<LinByteArrayTag> implements LinByteArrayTagVisitor {
    private ByteBuffer data;

    protected TreeByteArrayVisitor(Consumer<LinByteArrayTag> tagConsumer) {
        super(tagConsumer);
    }

    @Override
    public void visitSize(int size) {
        data = ByteBuffer.allocate(size);
    }

    @Override
    public void visitChunk(ByteBuffer buffer) {
        data.put(buffer);
    }

    @Override
    public void visitEnd() {
        tagFinished(new LinByteArrayTag(data.array(), true));
        data = null;
    }
}
