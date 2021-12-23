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

import org.enginehub.linbus.common.internal.AbstractIterator;
import org.enginehub.linbus.common.internal.Iterators;
import org.enginehub.linbus.stream.token.LinToken;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;

/**
 * Represents a compound tag.
 */
public final class LinCompoundTag extends LinTag<@NotNull Map<@NotNull String, @NotNull LinTag<?, ?>>, LinCompoundTag> {

    /**
     * Creates a new builder.
     *
     * @return a new builder
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * A builder for {@link LinCompoundTag LinCompoundTags}.
     */
    public static final class Builder {
        private final LinkedHashMap<@NotNull String, @NotNull LinTag<?, ?>> collector;

        private Builder() {
            this.collector = new LinkedHashMap<>();
        }

        private Builder(LinCompoundTag base) {
            this.collector = new LinkedHashMap<>(base.value);
        }

        /**
         * Add a tag to the compound tag.
         *
         * @param name the name of the tag
         * @param value the value of the tag
         * @return this builder
         */
        public Builder put(@NotNull String name, @NotNull LinTag<?, ?> value) {
            this.collector.put(name, value);
            return this;
        }

        /**
         * Add multiple tags to the compound tag.
         *
         * @param map the tags to add
         * @return this builder
         */
        public Builder putAll(@NotNull Map<String, ? extends LinTag<?, ?>> map) {
            this.collector.putAll(map);
            return this;
        }

        /**
         * Finish building the compound tag.
         *
         * @return the built tag
         */
        public @NotNull LinCompoundTag build() {
            // Let the constructor run a copy for us.
            return new LinCompoundTag(this.collector);
        }
    }

    private final Map<@NotNull String, @NotNull LinTag<?, ?>> value;

    /**
     * Creates a new compound tag.
     *
     * <p>
     * The map <em>will not</em> be copied using {@link Map#copyOf(Map)}, as that fails to preserve order. Instead, the
     * map will be copied using {@link LinkedHashMap#LinkedHashMap(Map)}.
     * </p>
     *
     * @param value the value
     */
    public LinCompoundTag(@NotNull Map<@NotNull String, @NotNull LinTag<?, ?>> value) {
        this(Collections.unmodifiableMap(new LinkedHashMap<>(value)), true);
    }

    LinCompoundTag(@NotNull Map<@NotNull String, @NotNull LinTag<?, ?>> value, boolean iSwearToNotModifyValue) {
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
    public @NotNull Map<@NotNull String, @NotNull LinTag<?, ?>> value() {
        return value;
    }

    @Override
    public @NotNull Iterator<LinToken> iterator() {
        return Iterators.combine(
            Iterators.of(new LinToken.CompoundStart()),
            Iterators.combine(new EntryTokenIterator()),
            Iterators.of(new LinToken.CompoundEnd())
        );
    }

    private class EntryTokenIterator extends AbstractIterator<Iterator<? extends LinToken>> {
        private final Iterator<Map.Entry<String, LinTag<?, ?>>> entryIterator = value.entrySet().iterator();
        private Map.Entry<String, LinTag<?, ?>> currentEntry;

        @Override
        protected Iterator<? extends LinToken> computeNext() {
            if (currentEntry == null) {
                if (!entryIterator.hasNext()) {
                    return end();
                }
                currentEntry = entryIterator.next();
                return Iterators.of(new LinToken.Name(currentEntry.getKey(), currentEntry.getValue().type().id()));
            }
            var next = currentEntry.getValue().iterator();
            currentEntry = null;
            return next;
        }
    }

    /**
     * Find the tag with the given type and name.
     *
     * @param name the name
     * @param type the type
     * @param <T> the type of the tag
     * @return the tag, or {@code null} if not found
     */
    public <T extends LinTag<?, ?>> @Nullable T findTag(@NotNull String name, @NotNull LinTagType<T> type) {
        LinTag<?, ?> tag = value.get(name);
        return type == tag.type() ? type.cast(tag) : null;
    }

    /**
     * Get the tag with the given type and name.
     *
     * @param name the name
     * @param type the type to require
     * @param <T> the type of the tag
     * @return the tag
     * @throws NoSuchElementException if there is no tag under the given name
     * @throws IllegalStateException if the tag exists but is of a different type
     */
    public <T extends LinTag<?, ?>> @NotNull T getTag(@NotNull String name, @NotNull LinTagType<T> type) {
        LinTag<?, ?> tag = value.get(name);

        if (tag == null) {
            throw new NoSuchElementException("No tag under the name '" + name + "' exists");
        }

        if (type != tag.type()) {
            throw new IllegalStateException("Tag under '" + name + "' exists, but is a " + tag.type().name()
                + " instead of " + type.name());
        }

        return type.cast(tag);
    }

    /**
     * Converts this tag into a {@link Builder}.
     *
     * @return a new builder
     */
    public Builder toBuilder() {
        return new Builder(this);
    }

    @Override
    public String toString() {
        return type().name() + value;
    }
}
