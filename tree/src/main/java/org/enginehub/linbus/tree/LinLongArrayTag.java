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

import java.nio.LongBuffer;
import java.util.Arrays;

/**
 * Represents a long array tag.
 */
public final class LinLongArrayTag extends LinTag<long[]> {

    /**
     * Creates a new long array tag from the given long array. The array will be {@linkplain Object#clone() cloned}.
     *
     * @param value the value
     * @return the tag
     */
    public static LinLongArrayTag of(long... value) {
        return new LinLongArrayTag(value.clone());
    }

    private final long[] value;

    private LinLongArrayTag(long[] value) {
        this.value = value;
    }

    @Override
    public LinTagType<LinLongArrayTag> type() {
        return LinTagType.longArrayTag();
    }

    @Override
    public long[] value() {
        return value.clone();
    }

    /**
     * Alternative no-copy byte access, returns a new {@link LongBuffer} that is read-only, and directly wraps the
     * underlying array.
     *
     * @return a read-only {@link LongBuffer} providing view access to the contents of this tag
     */
    public LongBuffer view() {
        return LongBuffer.wrap(value).asReadOnlyBuffer();
    }

    @Override
    public LinStream linStream() {
        return new SurroundingLinStream(
            new LinToken.LongArrayStart(value.length),
            new LinStream() {
                private static final int BUFFER_SIZE = 4096;
                private int i = 0;

                @Override
                public @Nullable LinToken nextOrNull() {
                    if (i >= value.length) {
                        return null;
                    }
                    var length = Math.min(BUFFER_SIZE, value.length - i);
                    var buffer = LongBuffer.wrap(value, i, length).asReadOnlyBuffer();
                    i += length;
                    return new LinToken.LongArrayContent(buffer);
                }
            },
            new LinToken.LongArrayEnd()
        );
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LinLongArrayTag that = (LinLongArrayTag) o;
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
