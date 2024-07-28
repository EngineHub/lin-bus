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

package org.enginehub.linbus.gui.javafx;

import javafx.collections.ModifiableObservableListBase;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;

public sealed abstract class BufferObservableList<T> extends ModifiableObservableListBase<T> {
    @Override
    protected void doAdd(int index, T element) {
        throw new UnsupportedOperationException();
    }

    @Override
    protected T doRemove(int index) {
        throw new UnsupportedOperationException();
    }

    public static final class OfByte extends BufferObservableList<Byte> {
        private final ByteBuffer buffer;

        public OfByte(ByteBuffer buffer) {
            this.buffer = buffer;
        }

        public ByteBuffer buffer() {
            return buffer;
        }

        @Override
        public int size() {
            return buffer.remaining();
        }

        @Override
        public Byte get(int index) {
            return buffer.get(buffer.position() + index);
        }

        @Override
        protected Byte doSet(int index, Byte value) {
            Byte oldValue = buffer.get(buffer.position() + index);
            buffer.put(buffer.position() + index, value);
            return oldValue;
        }
    }

    public static final class OfInt extends BufferObservableList<Integer> {
        private final IntBuffer buffer;

        public OfInt(IntBuffer buffer) {
            this.buffer = buffer;
        }

        public IntBuffer buffer() {
            return buffer;
        }

        @Override
        public int size() {
            return buffer.remaining();
        }

        @Override
        public Integer get(int index) {
            return buffer.get(buffer.position() + index);
        }

        @Override
        protected Integer doSet(int index, Integer value) {
            Integer oldValue = buffer.get(buffer.position() + index);
            buffer.put(buffer.position() + index, value);
            return oldValue;
        }
    }

    public static final class OfLong extends BufferObservableList<Long> {
        private final LongBuffer buffer;

        public OfLong(LongBuffer buffer) {
            this.buffer = buffer;
        }

        public LongBuffer buffer() {
            return buffer;
        }

        @Override
        public int size() {
            return buffer.remaining();
        }

        @Override
        public Long get(int index) {
            return buffer.get(buffer.position() + index);
        }

        @Override
        protected Long doSet(int index, Long value) {
            Long oldValue = buffer.get(buffer.position() + index);
            buffer.put(buffer.position() + index, value);
            return oldValue;
        }
    }
}
