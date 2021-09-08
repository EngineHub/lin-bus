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

import org.enginehub.linbus.common.LinTagId;
import org.enginehub.linbus.stream.visitor.LinByteArrayTagVisitor;
import org.enginehub.linbus.stream.visitor.LinCompoundTagVisitor;
import org.enginehub.linbus.stream.visitor.LinContainerVisitor;
import org.enginehub.linbus.stream.visitor.LinIntArrayTagVisitor;
import org.enginehub.linbus.stream.visitor.LinListTagVisitor;
import org.enginehub.linbus.stream.visitor.LinLongArrayTagVisitor;
import org.enginehub.linbus.stream.visitor.LinRootVisitor;

import java.io.DataInput;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Objects;

public class LinNbtReader {
    public static void accept(DataInput input, LinRootVisitor visitor) throws IOException {
        if (input.readUnsignedByte() != LinTagId.COMPOUND.id()) {
            throw new IllegalStateException("NBT stream does not start with a compound tag");
        }

        var name = input.readUTF();

        var compoundVisitor = Objects.requireNonNull(
            visitor.visitValue(name),
            () -> visitor + " returned null for value visitor"
        );
        acceptCompound(input, compoundVisitor);
    }

    private static void acceptByteArray(DataInput input, LinByteArrayTagVisitor visitor) throws IOException {
        int size = input.readInt();
        acceptArrayShared(input, size, visitor);
    }

    // These are using `acceptArrayShared` to be a little more DRY.
    // If it turns out not to be efficient, alternative methods should be used.

    private static void acceptIntArray(DataInput input, LinIntArrayTagVisitor visitor) throws IOException {
        int size = input.readInt() * 4;
        acceptArrayShared(input, size, new LinByteArrayTagVisitor() {
            @Override
            public void visitSize(int size) {
                visitor.visitSize(size / 4);
            }

            @Override
            public void visitChunk(ByteBuffer buffer) {
                var intBuffer = buffer.asIntBuffer();
                visitor.visitChunk(intBuffer);
                buffer.position(buffer.position() + intBuffer.position() * 4);
            }

            @Override
            public void visitEnd() {
                visitor.visitEnd();
            }
        });
    }

    private static void acceptLongArray(DataInput input, LinLongArrayTagVisitor visitor) throws IOException {
        int size = input.readInt() * 8;
        acceptArrayShared(input, size, new LinByteArrayTagVisitor() {
            @Override
            public void visitSize(int size) {
                visitor.visitSize(size / 8);
            }

            @Override
            public void visitChunk(ByteBuffer buffer) {
                var longBuffer = buffer.asLongBuffer();
                visitor.visitChunk(longBuffer);
                buffer.position(buffer.position() + longBuffer.position() * 8);
            }

            @Override
            public void visitEnd() {
                visitor.visitEnd();
            }
        });
    }

    private static void acceptArrayShared(DataInput input, int size, LinByteArrayTagVisitor visitor) throws IOException {
        visitor.visitSize(size);
        var buffer = ByteBuffer.allocate(Math.min(size, 8192));
        var readOnly = buffer.asReadOnlyBuffer();
        while (size > 0) {
            // Read into array[pos->pos+min(size,rem)]
            int readAmount = Math.min(size, buffer.remaining());
            input.readFully(buffer.array(), buffer.position(), readAmount);
            size -= readAmount;
            // Update RO buffer
            readOnly.position(buffer.position());
            readOnly.limit(buffer.position() + readAmount);

            visitor.visitChunk(readOnly);
            // Update our buffer from new pos/limit
            buffer.position(readOnly.position());
            buffer.limit(readOnly.limit());
            // Compact the buffer for next reading round
            buffer.compact();
        }
        // Finish consuming the buffer
        readOnly.position(0);
        readOnly.limit(buffer.position());
        while (readOnly.hasRemaining()) {
            visitor.visitChunk(readOnly);
        }
        visitor.visitEnd();
    }

    private static void acceptList(DataInput input, LinListTagVisitor visitor) throws IOException {
        var id = LinTagId.fromId(input.readUnsignedByte());
        int size = input.readInt();
        if (size > 0 && id == LinTagId.END) {
            throw new IllegalStateException("Read a non-empty list with an element type of 'end', this is not legal");
        }
        visitor.visitSizeAndType(size, id);
        for (int i = 0; i < size; i++) {
            visitValue(input, visitor, id, i);
        }
        visitor.visitEnd();
    }

    private static void acceptCompound(DataInput input, LinCompoundTagVisitor visitor) throws IOException {
        while (true) {
            var id = LinTagId.fromId(input.readUnsignedByte());
            if (id == LinTagId.END) {
                break;
            }

            String name = input.readUTF();

            visitValue(input, visitor, id, name);
        }
        visitor.visitEnd();
    }

    private static <K> void visitValue(DataInput input, LinContainerVisitor<K> visitor, LinTagId id, K key) throws IOException {
        switch (id) {
            // END omitted to use `default` case and throw
            case BYTE -> visitor.visitValueByte(key).visitByte(input.readByte());
            case SHORT -> visitor.visitValueShort(key).visitShort(input.readShort());
            case INT -> visitor.visitValueInt(key).visitInt(input.readInt());
            case LONG -> visitor.visitValueLong(key).visitLong(input.readLong());
            case FLOAT -> visitor.visitValueFloat(key).visitFloat(input.readFloat());
            case DOUBLE -> visitor.visitValueDouble(key).visitDouble(input.readDouble());
            case BYTE_ARRAY -> acceptByteArray(input, visitor.visitValueByteArray(key));
            case STRING -> visitor.visitValueString(key).visitString(input.readUTF());
            case LIST -> acceptList(input, visitor.visitValueList(key));
            case COMPOUND -> acceptCompound(input, visitor.visitValueCompound(key));
            case INT_ARRAY -> acceptIntArray(input, visitor.visitValueIntArray(key));
            case LONG_ARRAY -> acceptLongArray(input, visitor.visitValueLongArray(key));
            default -> throw new IllegalStateException("Invalid id: " + id);
        }
    }
}
