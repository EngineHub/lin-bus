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

import org.enginehub.linbus.common.LinTagId;
import org.enginehub.linbus.stream.LinStream;
import org.enginehub.linbus.stream.LinStreamable;
import org.enginehub.linbus.stream.internal.FlatteningLinStream;
import org.enginehub.linbus.stream.internal.SurroundingLinStream;
import org.enginehub.linbus.stream.token.LinToken;
import org.enginehub.linbus.tree.impl.LinTagReader;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;

/**
 * Represents a compound tag.
 */
public final class LinCompoundTag extends LinTag<@NotNull Map<@NotNull String, ? extends @NotNull LinTag<?, ?>>, LinCompoundTag> {

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
            if (value.type().id() == LinTagId.END) {
                throw new IllegalArgumentException("Cannot add END tag to compound tag");
            }
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
            map.forEach(this::put);
            return this;
        }

        /**
         * Remove a tag from the compound tag.
         *
         * @param name the name of the tag
         * @return this builder
         */
        public Builder remove(@NotNull String name) {
            this.collector.remove(name);
            return this;
        }

        /**
         * Add a byte array to the compound tag.
         *
         * @param name the name of the tag
         * @param value the value to add
         * @return this builder
         */
        public Builder putByteArray(@NotNull String name, byte @NotNull [] value) {
            return put(name, new LinByteArrayTag(value));
        }

        /**
         * Add a byte to the compound tag.
         *
         * @param name the name of the tag
         * @param value the value to add
         * @return this builder
         */
        public Builder putByte(@NotNull String name, byte value) {
            return put(name, new LinByteTag(value));
        }

        /**
         * Add a map to the compound tag.
         *
         * @param name the name of the tag
         * @param value the value to add
         * @return this builder
         */
        public Builder putCompound(@NotNull String name, @NotNull Map<String, ? extends LinTag<?, ?>> value) {
            return put(name, new LinCompoundTag(value));
        }

        /**
         * Add a double to the compound tag.
         *
         * @param name the name of the tag
         * @param value the value to add
         * @return this builder
         */
        public Builder putDouble(@NotNull String name, double value) {
            return put(name, new LinDoubleTag(value));
        }

        /**
         * Add a float to the compound tag.
         *
         * @param name the name of the tag
         * @param value the value to add
         * @return this builder
         */
        public Builder putFloat(@NotNull String name, float value) {
            return put(name, new LinFloatTag(value));
        }

        /**
         * Add an int array to the compound tag.
         *
         * @param name the name of the tag
         * @param value the value to add
         * @return this builder
         */
        public Builder putIntArray(@NotNull String name, int @NotNull [] value) {
            return put(name, new LinIntArrayTag(value));
        }

        /**
         * Add an int to the compound tag.
         *
         * @param name the name of the tag
         * @param value the value to add
         * @return this builder
         */
        public Builder putInt(@NotNull String name, int value) {
            return put(name, new LinIntTag(value));
        }

        /**
         * Add a long array to the compound tag.
         *
         * @param name the name of the tag
         * @param value the value to add
         * @return this builder
         */
        public Builder putLongArray(@NotNull String name, long @NotNull [] value) {
            return put(name, new LinLongArrayTag(value));
        }

        /**
         * Add a long to the compound tag.
         *
         * @param name the name of the tag
         * @param value the value to add
         * @return this builder
         */
        public Builder putLong(@NotNull String name, long value) {
            return put(name, new LinLongTag(value));
        }

        /**
         * Add a short to the compound tag.
         *
         * @param name the name of the tag
         * @param value the value to add
         * @return this builder
         */
        public Builder putShort(@NotNull String name, short value) {
            return put(name, new LinShortTag(value));
        }

        /**
         * Add a string to the compound tag.
         *
         * @param name the name of the tag
         * @param value the value to add
         * @return this builder
         */
        public Builder putString(@NotNull String name, @NotNull String value) {
            return put(name, new LinStringTag(value));
        }

        /**
         * Finish building the compound tag.
         *
         * @return the built tag
         */
        public @NotNull LinCompoundTag build() {
            return new LinCompoundTag(copyImmutable(this.collector), false);
        }
    }

    /**
     * Read a compound tag from the given stream.
     *
     * @param tokens the stream to read from
     * @return the compound tag
     */
    public static LinCompoundTag readFrom(@NotNull LinStream tokens) throws IOException {
        return LinTagReader.readCompound(tokens);
    }

    private static @NotNull Map<@NotNull String, @NotNull LinTag<?, ?>> copyImmutable(@NotNull Map<@NotNull String, ? extends @NotNull LinTag<?, ?>> value) {
        return Collections.unmodifiableMap(new LinkedHashMap<>(value));
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
    public LinCompoundTag(@NotNull Map<@NotNull String, ? extends @NotNull LinTag<?, ?>> value) {
        this(copyImmutable(value), true);
    }


    LinCompoundTag(@NotNull Map<@NotNull String, @NotNull LinTag<?, ?>> value, boolean check) {
        if (check) {
            for (LinTag<?, ?> tag : value.values()) {
                if (tag.type().id() == LinTagId.END) {
                    throw new IllegalArgumentException("Cannot add END tag to compound tag");
                }
            }
        }
        this.value = Objects.requireNonNull(value, "value is null");
    }

    @Override
    public @NotNull LinTagType<LinCompoundTag> type() {
        return LinTagType.compoundTag();
    }

    @Override
    public @NotNull Map<@NotNull String, ? extends @NotNull LinTag<?, ?>> value() {
        return value;
    }

    @Override
    public @NotNull LinStream linStream() {
        return new SurroundingLinStream(
            new LinToken.CompoundStart(),
            new FlatteningLinStream(new EntryTokenIterator()),
            new LinToken.CompoundEnd()
        );
    }

    private class EntryTokenIterator implements Iterator<LinStreamable> {
        private final Iterator<Map.Entry<String, LinTag<?, ?>>> entryIterator = value.entrySet().iterator();

        @Override
        public boolean hasNext() {
            return entryIterator.hasNext();
        }

        @Override
        public LinStreamable next() {
            var entry = entryIterator.next();
            return new SurroundingLinStream(
                new LinToken.Name(entry.getKey(), entry.getValue().type().id()),
                entry.getValue().linStream(),
                null
            );
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
    public <T extends LinTag<?, T>> @Nullable T findTag(@NotNull String name, @NotNull LinTagType<T> type) {
        LinTag<?, ?> tag = value.get(name);
        return tag != null && type == tag.type() ? type.cast(tag) : null;
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
    public <T extends LinTag<?, T>> @NotNull T getTag(@NotNull String name, @NotNull LinTagType<T> type) {
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
    public @NotNull String toString() {
        return getClass().getSimpleName() + value;
    }
}
