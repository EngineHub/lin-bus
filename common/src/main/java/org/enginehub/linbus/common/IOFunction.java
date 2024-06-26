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

package org.enginehub.linbus.common;

import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.util.function.Function;

/**
 * Exactly like {@link Function}, but may throw {@link IOException}.
 *
 * @param <T> the input type
 * @param <R> the output type
 */
public interface IOFunction<T, R extends @Nullable Object> {
    /**
     * Applies this function to the given argument.
     *
     * @param t the function argument
     * @return the function result
     * @throws IOException if an I/O error occurs
     */
    R apply(T t) throws IOException;
}
