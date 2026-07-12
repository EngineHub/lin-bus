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
import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.function.Function;

/**
 * Represents a compound tag.
 */
public final class LinCompoundTag extends LinTag<Map<String, ? extends LinTag<?>>> {

    /**
     * Creates a new compound tag.
     *
     * <p>
     * The map <em>will not</em> be copied using {@link Map#copyOf(Map)}, as that fails to preserve order. Instead, the
     * map will be copied using {@link LinkedHashMap#LinkedHashMap(Map)}.
     * </p>
     *
     * @param value the value
     * @return the tag
     */
    public static LinCompoundTag of(Map<String, ? extends LinTag<?>> value) {
        if (value.isEmpty()) {
            return EMPTY;
        }
        return new LinCompoundTag(copyImmutable(value), true);
    }

    private static final LinCompoundTag EMPTY = new LinCompoundTag(Map.of(), false);

    /**
     * {@return an empty compound tag}
     */
    public static LinCompoundTag empty() {
        return EMPTY;
    }

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
        private final LinkedHashMap<String, LinTag<?>> collector;

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
        public Builder put(String name, LinTag<?> value) {
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
        public Builder putAll(Map<String, ? extends LinTag<?>> map) {
            map.forEach(this::put);
            return this;
        }

        /**
         * Remove a tag from the compound tag.
         *
         * @param name the name of the tag
         * @return this builder
         */
        public Builder remove(String name) {
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
        public Builder putByteArray(String name, byte[] value) {
            return put(name, LinByteArrayTag.of(value));
        }

        /**
         * Add a byte to the compound tag.
         *
         * @param name the name of the tag
         * @param value the value to add
         * @return this builder
         */
        public Builder putByte(String name, byte value) {
            return put(name, LinByteTag.of(value));
        }

        /**
         * Add a map to the compound tag.
         *
         * @param name the name of the tag
         * @param value the value to add
         * @return this builder
         */
        public Builder putCompound(String name, Map<String, ? extends LinTag<?>> value) {
            return put(name, new LinCompoundTag(copyImmutable(value), true));
        }

        /**
         * Add a double to the compound tag.
         *
         * @param name the name of the tag
         * @param value the value to add
         * @return this builder
         */
        public Builder putDouble(String name, double value) {
            return put(name, LinDoubleTag.of(value));
        }

        /**
         * Add a float to the compound tag.
         *
         * @param name the name of the tag
         * @param value the value to add
         * @return this builder
         */
        public Builder putFloat(String name, float value) {
            return put(name, LinFloatTag.of(value));
        }

        /**
         * Add an int array to the compound tag.
         *
         * @param name the name of the tag
         * @param value the value to add
         * @return this builder
         */
        public Builder putIntArray(String name, int[] value) {
            return put(name, LinIntArrayTag.of(value));
        }

        /**
         * Add an int to the compound tag.
         *
         * @param name the name of the tag
         * @param value the value to add
         * @return this builder
         */
        public Builder putInt(String name, int value) {
            return put(name, LinIntTag.of(value));
        }

        /**
         * Add a list to the compound tag.
         *
         * @param name the name of the tag
         * @param elementType the element type of the list
         * @param value the value to add
         * @param <T> the type of the elements in the list
         * @return this builder
         */
        public <T extends LinTag<?>> Builder putList(String name, LinTagType<T> elementType, List<T> value) {
            return put(name, LinListTag.of(elementType, value));
        }

        /**
         * Add a long array to the compound tag.
         *
         * @param name the name of the tag
         * @param value the value to add
         * @return this builder
         */
        public Builder putLongArray(String name, long[] value) {
            return put(name, LinLongArrayTag.of(value));
        }

        /**
         * Add a long to the compound tag.
         *
         * @param name the name of the tag
         * @param value the value to add
         * @return this builder
         */
        public Builder putLong(String name, long value) {
            return put(name, LinLongTag.of(value));
        }

        /**
         * Add a short to the compound tag.
         *
         * @param name the name of the tag
         * @param value the value to add
         * @return this builder
         */
        public Builder putShort(String name, short value) {
            return put(name, LinShortTag.of(value));
        }

        /**
         * Add a string to the compound tag.
         *
         * @param name the name of the tag
         * @param value the value to add
         * @return this builder
         */
        public Builder putString(String name, String value) {
            return put(name, LinStringTag.of(value));
        }

        /**
         * Finish building the compound tag.
         *
         * @return the built tag
         */
        public LinCompoundTag build() {
            if (this.collector.isEmpty()) {
                return EMPTY;
            }
            return new LinCompoundTag(copyImmutable(this.collector), false);
        }
    }

    /**
     * Read a compound tag from the given stream.
     *
     * @param tokens the stream to read from
     * @return the compound tag
     * @throws IOException if an I/O error occurs
     */
    public static LinCompoundTag readFrom(LinStream tokens) throws IOException {
        return LinTagReader.readCompound(tokens);
    }

    private static Map<String, LinTag<?>> copyImmutable(
        Map<String, ? extends LinTag<?>> value
    ) {
        return Collections.unmodifiableMap(new LinkedHashMap<>(value));
    }

    private final Map<String, LinTag<?>> value;

    private LinCompoundTag(Map<String, LinTag<?>> value, boolean check) {
        if (check) {
            for (LinTag<?> tag : value.values()) {
                if (tag.type().id() == LinTagId.END) {
                    throw new IllegalArgumentException("Cannot add END tag to compound tag");
                }
            }
        }
        this.value = Objects.requireNonNull(value, "value is null");
    }

    @Override
    public LinTagType<LinCompoundTag> type() {
        return LinTagType.compoundTag();
    }

    @Override
    public Map<String, LinTag<?>> value() {
        return value;
    }

    @Override
    public LinStream linStream() {
        return new SurroundingLinStream(
            new LinToken.CompoundStart(),
            new FlatteningLinStream(new EntryTokenIterator()),
            new LinToken.CompoundEnd()
        );
    }

    private class EntryTokenIterator implements Iterator<LinStreamable> {
        private final Iterator<? extends Map.Entry<String, ? extends LinTag<?>>> entryIterator = value.entrySet().iterator();

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
    public <T extends LinTag<?>> @Nullable T findTag(String name, LinTagType<T> type) {
        LinTag<?> tag = value.get(name);
        return tag != null && type == tag.type() ? type.cast(tag) : null;
    }

    /**
     * Find the list tag with the given element type and name.
     *
     * @param name the name
     * @param elementType the element type
     * @param <T> the element type of the tag
     * @return the tag, or {@code null} if not found
     */
    public <T extends LinTag<?>> @Nullable LinListTag<T> findListTag(
        String name, LinTagType<T> elementType
    ) {
        var listTag = findTag(name, LinTagType.listTag());
        if (listTag == null || listTag.elementType() != elementType) {
            return null;
        }
        @SuppressWarnings("unchecked")
        LinListTag<T> cast = (LinListTag<T>) listTag;
        return cast;
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
    public <T extends LinTag<?>> T getTag(String name, LinTagType<T> type) {
        LinTag<?> tag = value.get(name);

        if (tag == null) {
            throw new NoSuchElementException("No tag under the name '" + name + "' exists");
        }

        return requireType(name, tag, type);
    }

    private <T extends LinTag<?>> T requireType(String name, LinTag<?> tag, LinTagType<T> type) {
        if (type != tag.type()) {
            throw new IllegalStateException("Tag under '" + name + "' exists, but is a " + tag.type().name()
                + " instead of " + type.name());
        }

        return type.cast(tag);
    }

    /**
     * Get the list tag with the given element type and name.
     *
     * @param name the name
     * @param elementType the element type
     * @param <T> the element type of the tag
     * @return the tag
     * @throws NoSuchElementException if there is no tag under the given name
     * @throws IllegalStateException if the tag exists but is of a different type
     */
    public <T extends LinTag<?>> LinListTag<T> getListTag(
        String name, LinTagType<T> elementType
    ) {
        var listTag = getTag(name, LinTagType.listTag());
        if (listTag.elementType() != elementType) {
            throw new IllegalStateException("Tag under '" + name + "' exists, but is a " + listTag.elementType().name()
                + " list instead of a " + elementType.name() + " list");
        }
        @SuppressWarnings("unchecked")
        LinListTag<T> cast = (LinListTag<T>) listTag;
        return cast;
    }

    private LinCompoundTag withChangedTag(String name, LinTag<?> value) {
        Objects.requireNonNull(value, "transformer returned a null tag");
        if (value.type().id() == LinTagId.END) {
            throw new IllegalArgumentException("Cannot add END tag to compound tag");
        }
        LinkedHashMap<String, LinTag<?>> newMap = new LinkedHashMap<>(this.value);
        newMap.put(name, value);
        return new LinCompoundTag(Collections.unmodifiableMap(newMap), false);
    }

    /**
     * Transform a value in the compound tag. You may change the type of the tag.
     *
     * <p>
     * This uses {@link #getTag(String, LinTagType)}, so it will throw an exception if the tag does not exist
     * or is of the wrong type.
     * </p>
     *
     * @param name the name of the tag to transform
     * @param type the type of the tag to transform
     * @param transformer the function to transform the tag
     * @return the new compound tag
     */
    public <T extends LinTag<?>> LinCompoundTag transformTag(
        String name, LinTagType<T> type, Function<T, ? extends LinTag<?>> transformer
    ) {
        T tag = getTag(name, type);
        LinTag<?> transformed = transformer.apply(tag);
        return withChangedTag(name, transformed);
    }

    /**
     * Transform a value in the compound tag, if it is present. You may change the type of the tag.
     *
     * <p>
     * If there is no tag under the given name, this returns {@code this} unchanged. If a tag is present but is of
     * the wrong type, an exception is thrown.
     * </p>
     *
     * @param name the name of the tag to transform
     * @param type the type of the tag to transform
     * @param transformer the function to transform the tag
     * @return the new compound tag, or {@code this} if there was no tag under the given name
     */
    public <T extends LinTag<?>> LinCompoundTag transformTagIfPresent(
        String name, LinTagType<T> type, Function<T, ? extends LinTag<?>> transformer
    ) {
        LinTag<?> existing = value.get(name);
        if (existing == null) {
            return this;
        }
        T tag = requireType(name, existing, type);
        return withChangedTag(name, transformer.apply(tag));
    }

    /**
     * Transform a value in the compound tag, inserting it if it is absent. You may change the type of the tag.
     *
     * <p>
     * If there is no tag under the given name, the transformer is called with {@code null} and its result is
     * inserted. If a tag is present but is of the wrong type, an exception is thrown. The transformer must not
     * return {@code null}.
     * </p>
     *
     * @param name the name of the tag to transform
     * @param type the type of the tag to transform
     * @param transformer the function to transform the tag, receiving {@code null} if it is absent
     * @return the new compound tag
     */
    public <T extends LinTag<?>> LinCompoundTag transformTagOrInsert(
        String name, LinTagType<T> type, Function<@Nullable T, ? extends LinTag<?>> transformer
    ) {
        LinTag<?> existing = value.get(name);
        T tag = existing == null ? null : requireType(name, existing, type);
        return withChangedTag(name, transformer.apply(tag));
    }

    /**
     * Transform a list value in the compound tag. You may change the type of the tag.
     *
     * <p>
     * This uses {@link #getListTag(String, LinTagType)}, so it will throw an exception if the tag does not exist
     * or is of the wrong type.
     * </p>
     *
     * @param name the name of the tag to transform
     * @param elementType the element type of the list tag to transform
     * @param transformer the function to transform the tag
     * @return the new compound tag
     */
    public <T extends LinTag<?>> LinCompoundTag transformListTag(
        String name, LinTagType<T> elementType, Function<LinListTag<T>, ? extends LinTag<?>> transformer
    ) {
        LinListTag<T> tag = getListTag(name, elementType);
        LinTag<?> transformed = transformer.apply(tag);
        return withChangedTag(name, transformed);
    }

    /**
     * Transform a list value in the compound tag, if it is present. You may change the type of the tag.
     *
     * <p>
     * If there is no tag under the given name, this returns {@code this} unchanged. If a tag is present but is not
     * a list of the given element type, an exception is thrown.
     * </p>
     *
     * @param name the name of the tag to transform
     * @param elementType the element type of the list tag to transform
     * @param transformer the function to transform the tag
     * @return the new compound tag, or {@code this} if there was no tag under the given name
     */
    public <T extends LinTag<?>> LinCompoundTag transformListTagIfPresent(
        String name, LinTagType<T> elementType, Function<LinListTag<T>, ? extends LinTag<?>> transformer
    ) {
        if (value.get(name) == null) {
            return this;
        }
        LinListTag<T> tag = getListTag(name, elementType);
        return withChangedTag(name, transformer.apply(tag));
    }

    /**
     * Transform a list value in the compound tag, inserting it if it is absent. You may change the type of the
     * tag.
     *
     * <p>
     * If there is no tag under the given name, the transformer is called with {@code null} and its result is
     * inserted. If a tag is present but is not a list of the given element type, an exception is thrown. The
     * transformer must not return {@code null}.
     * </p>
     *
     * @param name the name of the tag to transform
     * @param elementType the element type of the list tag to transform
     * @param transformer the function to transform the tag, receiving {@code null} if it is absent
     * @return the new compound tag
     */
    public <T extends LinTag<?>> LinCompoundTag transformListTagOrInsert(
        String name, LinTagType<T> elementType, Function<@Nullable LinListTag<T>, ? extends LinTag<?>> transformer
    ) {
        LinListTag<T> tag = value.get(name) == null ? null : getListTag(name, elementType);
        return withChangedTag(name, transformer.apply(tag));
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
        return getClass().getSimpleName() + value;
    }
}
