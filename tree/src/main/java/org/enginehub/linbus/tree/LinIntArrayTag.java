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

import org.jetbrains.annotations.NotNull;

import java.nio.IntBuffer;
import java.util.Arrays;

public final class LinIntArrayTag extends LinTag<int @NotNull [], LinIntArrayTag> {
    private final int[] value;

    public LinIntArrayTag(int... value) {
        this(value.clone(), true);
    }

    LinIntArrayTag(int[] value, boolean iSwearToNotModifyValue) {
        if (!iSwearToNotModifyValue) {
            throw new IllegalArgumentException("You think you're clever, huh?");
        }
        this.value = value;
    }

    @Override
    public LinTagType<LinIntArrayTag> type() {
        return LinTagType.intArrayTag();
    }

    @Override
    public int @NotNull [] value() {
        return value.clone();
    }

    /**
     * Alternative no-copy byte access, returns a new {@link IntBuffer} that is read-only, and
     * directly wraps the underlying array.
     */
    public IntBuffer view() {
        return IntBuffer.wrap(value).asReadOnlyBuffer();
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
        int result = super.hashCode();
        result = 31 * result + Arrays.hashCode(value);
        return result;
    }

    @Override
    public String toString() {
        return type().name() + "{" + Arrays.toString(value()) + '}';
    }
}
