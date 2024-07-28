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

import org.enginehub.linbus.stream.LinStream;
import org.enginehub.linbus.stream.internal.SurroundingLinStream;
import org.enginehub.linbus.stream.token.LinToken;
import org.jspecify.annotations.Nullable;

import java.nio.IntBuffer;
import java.util.Arrays;

/**
 * Represents an int array tag.
 */
public final class LinIntArrayTag extends LinTag<int[]> {

    /**
     * Creates a new int array tag from the given int array. The array will be {@linkplain Object#clone() cloned}.
     *
     * @param value the value
     * @return the tag
     */
    public static LinIntArrayTag of(int... value) {
        return new LinIntArrayTag(value.clone());
    }

    private final int[] value;

    private LinIntArrayTag(int[] value) {
        this.value = value;
    }

    @Override
    public LinTagType<LinIntArrayTag> type() {
        return LinTagType.intArrayTag();
    }

    @Override
    public int[] value() {
        return value.clone();
    }

    /**
     * Alternative no-copy byte access, returns a new {@link IntBuffer} that is read-only, and directly wraps the
     * underlying array.
     *
     * @return a read-only {@link IntBuffer} providing view access to the contents of this tag
     */
    public IntBuffer view() {
        return IntBuffer.wrap(value).asReadOnlyBuffer();
    }

    @Override
    public LinStream linStream() {
        return new SurroundingLinStream(
            new LinToken.IntArrayStart(value.length),
            new LinStream() {
                private static final int BUFFER_SIZE = 4096;
                private int i = 0;

                @Override
                public @Nullable LinToken nextOrNull() {
                    if (i >= value.length) {
                        return null;
                    }
                    var length = Math.min(BUFFER_SIZE, value.length - i);
                    var buffer = IntBuffer.wrap(value, i, length).asReadOnlyBuffer();
                    i += length;
                    return new LinToken.IntArrayContent(buffer);
                }
            },
            new LinToken.IntArrayEnd()
        );
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LinIntArrayTag that = (LinIntArrayTag) o;
        return Arrays.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        int result = 1;
        result = 31 * result + Arrays.hashCode(value);
        return result;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + Arrays.toString(value);
    }
}
