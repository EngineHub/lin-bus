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

import java.util.Objects;

public final class LinStringTag extends LinTag<@NotNull String, LinStringTag> {
    private final String value;

    public LinStringTag(String value) {
        this.value = Objects.requireNonNull(value, "value is null");
    }

    @Override
    public LinTagType<LinStringTag> type() {
        return LinTagType.stringTag();
    }

    @Override
    public @NotNull String value() {
        return value;
    }
}
