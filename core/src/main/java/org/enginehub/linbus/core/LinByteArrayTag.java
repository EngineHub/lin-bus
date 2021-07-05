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

package org.enginehub.linbus.core;

import org.checkerframework.checker.nullness.qual.NonNull;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;

public final class LinByteArrayTag extends LinTag<byte @NonNull []> {
    public static LinByteArrayTag readFrom(DataInput input) throws IOException {
        int length = input.readInt();
        byte[] value = new byte[length];
        input.readFully(value);
        return new LinByteArrayTag(value, true);
    }

    private final byte[] value;

    public LinByteArrayTag(byte... value) {
        this(value.clone(), true);
    }

    private LinByteArrayTag(byte[] value, boolean iSwearToNotModifyValue) {
        if (!iSwearToNotModifyValue) {
            throw new IllegalArgumentException("You think you're clever, huh?");
        }
        this.value = value;
    }

    @Override
    public LinTagType<LinByteArrayTag> type() {
        return LinTagType.byteArrayTag();
    }

    @Override
    public byte @NonNull [] value() {
        return value.clone();
    }

    /**
     * Alternative no-copy byte access, returns a new {@link ByteBuffer} that is read-only, and
     * directly wraps the underlying array.
     */
    public ByteBuffer view() {
        return ByteBuffer.wrap(value).asReadOnlyBuffer();
    }

    @Override
    public void writeTo(DataOutput output) throws IOException {
        output.writeInt(value.length);
        output.write(value);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LinByteArrayTag that = (LinByteArrayTag) o;
        return Arrays.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + Arrays.hashCode(value);
        return result;
    }

    @Override
    public String toString() {
        return type().name() + "{" + Arrays.toString(value()) + '}';
    }
}
