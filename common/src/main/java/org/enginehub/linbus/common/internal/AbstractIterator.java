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

package org.enginehub.linbus.common.internal;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Look-alike for Guava's abstract iterator.
 *
 * @param <T> the type of items returned by this iterator
 */
public abstract class AbstractIterator<T> implements Iterator<T> {
    private boolean needNext = true;
    private boolean end;
    private T next;

    /**
     * Compute the next item.
     *
     * @return the next item, or the return value of {@link #end()} if there are no more items
     */
    protected abstract T computeNext();

    /**
     * Signal the end of the iteration.
     *
     * @return {@code null}
     */
    protected final T end() {
        this.end = true;
        return null;
    }

    @Override
    public T next() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }
        T next = this.next;
        needNext = true;
        this.next = null;
        return next;
    }

    @Override
    public boolean hasNext() {
        if (needNext) {
            needNext = false;
            next = computeNext();
        }
        return !end;
    }
}
