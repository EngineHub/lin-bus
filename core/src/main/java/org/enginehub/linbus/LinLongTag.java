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

package org.enginehub.linbus;

import org.checkerframework.checker.nullness.qual.NonNull;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public final class LinLongTag extends LinTag<@NonNull Long> {
    public static LinLongTag readFrom(DataInput input) throws IOException {
        return new LinLongTag(
            input.readLong()
        );
    }

    private final long value;

    public LinLongTag(long value) {
        this.value = value;
    }

    @Override
    public LinTagType<LinLongTag> type() {
        return LinTagType.longTag();
    }

    @Override
    public @NonNull Long value() {
        return value;
    }

    public long valueAsLong() {
        return value;
    }

    @Override
    public void writeTo(DataOutput output) throws IOException {
        output.writeLong(value);
    }
}
