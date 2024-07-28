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

import org.enginehub.linbus.stream.LinStream;
import org.enginehub.linbus.stream.internal.FlatteningLinStream;
import org.enginehub.linbus.stream.internal.SurroundingLinStream;
import org.enginehub.linbus.stream.token.LinToken;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * Represents a list of {@link LinTag LinTags}.
 *
 * @param <T> the type of the elements in the list
 */
public final class LinListTag<T extends LinTag<?>> extends LinTag<List<T>> {

    /**
     * Creates a new list tag.
     *
     * <p>
     * The list will be copied as per the {@link List#copyOf(Collection)} method.
     * </p>
     *
     * @param elementType the element type of the list
     * @param value the elements in the list
     * @param <T> the type of the elements in the list
     * @return the tag
     */
    public static <T extends LinTag<?>> LinListTag<T> of(
        LinTagType<T> elementType, List<T> value
    ) {
        for (T t : value) {
            if (t.type() != elementType) {
                throw new IllegalArgumentException("Element is not of type " + elementType.name() + " but "
                    + t.type().name());
            }
        }
        return new LinListTag<>(elementType, List.copyOf(value));
    }

    /**
     * Get an empty list of the given element type.
     *
     * @param elementType the element type of the list
     * @param <T> the type of the elements in the list
     * @return an empty list
     */
    public static <T extends LinTag<?>> LinListTag<T> empty(LinTagType<T> elementType) {
        return builder(elementType).build();
    }

    /**
     * Creates a new builder for a list of the given element type.
     *
     * @param elementType the element type of the list
     * @param <T> the type of the elements in the list
     * @return a new builder
     */
    public static <T extends LinTag<?>> Builder<T> builder(LinTagType<T> elementType) {
        return new Builder<>(elementType);
    }

    /**
     * A builder for {@link LinListTag LinListTags}.
     *
     * @param <T> the type of the elements in the list
     */
    public static final class Builder<T extends LinTag<?>> {
        private final LinTagType<T> elementType;
        private final List<T> collector;

        private Builder(LinTagType<T> elementType) {
            this.elementType = elementType;
            this.collector = new ArrayList<>();
        }

        private Builder(LinListTag<T> base) {
            this.elementType = base.elementType;
            this.collector = new ArrayList<>(base.value);
        }

        /**
         * Add an element to the list.
         *
         * @param tag the element
         * @return this builder
         */
        public Builder<T> add(T tag) {
            if (tag.type() != elementType) {
                throw new IllegalArgumentException("Element is not of type " + elementType.name() + " but "
                    + tag.type().name());
            }
            this.collector.add(tag);
            return this;
        }

        /**
         * Add a collection of elements to the list.
         *
         * @param tags the elements
         * @return this builder
         */
        public Builder<T> addAll(Collection<? extends T> tags) {
            tags.forEach(this::add);
            return this;
        }

        /**
         * Set the element at the given index. There must already be an element at the index.
         *
         * @param index the index
         * @param tag the element
         * @return this builder
         */
        public Builder<T> set(int index, T tag) {
            if (tag.type() != elementType) {
                throw new IllegalArgumentException("Element is not of type " + elementType.name() + " but "
                    + tag.type().name());
            }
            this.collector.set(index, tag);
            return this;
        }

        /**
         * Finish building the list tag.
         *
         * @return the built tag
         */
        public LinListTag<T> build() {
            return new LinListTag<>(this.elementType, List.copyOf(this.collector));
        }
    }

    private final LinTagType<T> elementType;
    private final List<T> value;

    private LinListTag(LinTagType<T> elementType, List<T> value) {
        Objects.requireNonNull(value, "value is null");
        if (!value.isEmpty() && elementType == LinTagType.endTag()) {
            throw new IllegalArgumentException("A non-empty list cannot be of type END");
        }
        this.elementType = elementType;
        this.value = value;
    }

    @Override
    public LinTagType<LinListTag<T>> type() {
        return LinTagType.listTag();
    }

    /**
     * {@return the element type of this list}
     */
    public LinTagType<T> elementType() {
        return elementType;
    }

    /**
     * Safely converts this list to a list of the given type.
     *
     * @param elementType the type to convert to
     * @param <U> the type of the elements in the list
     * @return the converted list
     * @throws IllegalStateException if the {@link #elementType()}  is not the same as the given type
     */
    public <U extends LinTag<?>> LinListTag<U> asTypeChecked(LinTagType<U> elementType) {
        if (elementType != this.elementType) {
            throw new IllegalStateException(
                "List is of type " + this.elementType.name() + ", not " + elementType.name()
            );
        }
        @SuppressWarnings("unchecked")
        LinListTag<U> cast = (LinListTag<U>) this;
        return cast;
    }

    @Override
    public List<T> value() {
        return value;
    }

    @Override
    public LinStream linStream() {
        return new SurroundingLinStream(
            new LinToken.ListStart(value.size(), elementType.id()),
            new FlatteningLinStream(value.iterator()),
            new LinToken.ListEnd()
        );
    }

    /**
     * Direct shorthand for {@link #value() value()}{@code .}{@link List#get(int) get(index)}.
     *
     * @param index the index of the element to get
     * @return the element at the given index
     */
    public T get(int index) {
        return value.get(index);
    }

    /**
     * Converts this tag into a {@link Builder}.
     *
     * @return a new builder
     */
    public Builder<T> toBuilder() {
        return new Builder<>(this);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + value;
    }
}
