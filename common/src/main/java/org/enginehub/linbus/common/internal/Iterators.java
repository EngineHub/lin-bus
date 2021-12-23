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

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * Internal iterator helper.
 */
public class Iterators {
    /**
     * Combine the given iterators into a single iterator.
     *
     * @param iterators the array of iterators to combine
     * @param <T> the type of objects returned by the combined iterator
     * @return an iterator that iterates over all the elements of the given iterators
     * @implNote delegates to {@link #combine(Iterator)}
     */
    @SafeVarargs
    public static <T> @NotNull Iterator<T> combine(@NotNull Iterator<? extends T> @NotNull ... iterators) {
        return combine(Arrays.asList(iterators).iterator());
    }

    /**
     * Combine the given iterators into a single iterator.
     *
     * @param iterators the iterator of iterators to combine
     * @param <T> the type of objects returned by the combined iterator
     * @return an iterator that iterates over all the elements of the given iterators
     */
    public static <T> @NotNull Iterator<T> combine(@NotNull Iterator<? extends @NotNull Iterator<? extends T>> iterators) {
        return new AbstractIterator<>() {
            private Iterator<? extends T> current;

            @Override
            protected T computeNext() {
                while (current == null || !current.hasNext()) {
                    if (!iterators.hasNext()) {
                        // free `current` just in case
                        current = null;
                        return end();
                    }
                    current = iterators.next();
                }
                return current.next();
            }
        };
    }

    /**
     * A {@link Iterator} that returns the given element once.
     *
     * @param value the element to return
     * @param <T> the type of the element
     * @return the iterator
     * @apiNote Functionally equivalent to {@link Collections#singletonList(Object)
     *     Collections.singletonList(value)}{@code .}{@link List#iterator() iterator()}.
     */
    public static <T> @NotNull Iterator<T> of(T value) {
        // Currently, this doesn't hold any extra memory, unfortunately the
        // Collections.singletonIterator method is not exposed.
        return Collections.singletonList(value).iterator();
    }

    private Iterators() {
    }
}
