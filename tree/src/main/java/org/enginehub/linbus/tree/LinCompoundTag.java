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

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;

public final class LinCompoundTag extends LinTag<@NonNull Map<String, @NonNull LinTag<?, ?>>, LinCompoundTag> {
    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private final LinkedHashMap<String, @NonNull LinTag<?, ?>> collector;

        private Builder() {
            this.collector = new LinkedHashMap<>();
        }

        private Builder(LinCompoundTag base) {
            this.collector = new LinkedHashMap<>(base.value);
        }

        public Builder put(String name, LinTag<?, ?> value) {
            this.collector.put(name, value);
            return this;
        }

        public @NonNull LinCompoundTag build() {
            // Let the constructor run a copy for us.
            return new LinCompoundTag(this.collector);
        }
    }

    private final Map<String, @NonNull LinTag<?, ?>> value;

    public LinCompoundTag(@NonNull Map<String, @NonNull LinTag<?, ?>> value) {
        this(Collections.unmodifiableMap(new LinkedHashMap<>(value)), true);
    }

    LinCompoundTag(@NonNull Map<String, @NonNull LinTag<?, ?>> value, boolean iSwearToNotModifyValue) {
        if (!iSwearToNotModifyValue) {
            throw new IllegalArgumentException("You think you're clever, huh?");
        }
        this.value = Objects.requireNonNull(value, "value is null");
    }

    @Override
    public LinTagType<LinCompoundTag> type() {
        return LinTagType.compoundTag();
    }

    @Override
    public @NonNull Map<String, @NonNull LinTag<?, ?>> value() {
        return value;
    }

    public <T extends LinTag<?, ?>> @Nullable T findTag(@NonNull String key, @NonNull LinTagType<T> type) {
        LinTag<?, ?> tag = value.get(key);
        return type == tag.type() ? type.cast(tag) : null;
    }

    public <T extends LinTag<?, ?>> @NonNull T getTag(@NonNull String key, @NonNull LinTagType<T> type) {
        LinTag<?, ?> tag = value.get(key);

        if (tag == null) {
            throw new NoSuchElementException("No tag under the key '" + key + "' exists");
        }

        if (type != tag.type()) {
            throw new IllegalStateException("Tag under '" + key + "' exists, but is a " + tag.type().name()
                + " instead of " + type.name());
        }

        return type.cast(tag);
    }

    public Builder toBuilder() {
        return new Builder(this);
    }

    @Override
    public String toString() {
        return "compound" + value;
    }
}
