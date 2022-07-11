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

package org.enginehub.linbus.stream.exception;

import java.io.Serial;

/**
 * Thrown when a NBT stream is malformed for writing.
 */
public final class NbtWriteException extends RuntimeException {
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * {@inheritDoc}
     */
    public NbtWriteException(String message) {
        super(message);
    }

    /**
     * {@inheritDoc}
     */
    public NbtWriteException(String message, Throwable cause) {
        super(message, cause);
    }
}
