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

import org.enginehub.linbus.stream.LinStreamable;
import org.jspecify.annotations.Nullable;

import java.util.Objects;

/**
 * Represents an NBT tag, which has a canonical representation of type {@code T}.
 *
 * @param <T> the type of the canonical representation
 */
public sealed abstract class LinTag<T> implements ToLinTag<LinTag<T>>, LinStreamable
    permits LinByteArrayTag, LinCompoundTag, LinEndTag, LinIntArrayTag, LinListTag, LinLongArrayTag, LinNumberTag, LinStringTag {

    /**
     * Constructor for subclasses.
     */
    protected LinTag() {
    }

    /**
     * Gets the type of this tag.
     *
     * @return the type of this tag
     */
    // This is to be overriden directly to save memory in the tag itself
    public abstract LinTagType<? extends LinTag<T>> type();

    /**
     * Gets the value of this tag.
     *
     * @return the value of the tag
     */
    public abstract T value();

    @Override
    public final LinTag<T> toLinTag() {
        // This could be overriden by subclasses to provide a sharper return type. I didn't do this because it's
        // a lot of work, but please ask / PR if you want it.
        return this;
    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LinTag<?> that = (LinTag<?>) o;
        return Objects.equals(value(), that.value());
    }

    @Override
    public int hashCode() {
        return value().hashCode();
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[" + value() + ']';
    }
}
