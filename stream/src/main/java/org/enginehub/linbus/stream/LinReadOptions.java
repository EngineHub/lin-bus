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

package org.enginehub.linbus.stream;


/**
 * Options for reading NBT streams.
 */
public final class LinReadOptions {

    /**
     * Create a new builder.
     *
     * @return a new builder
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Builder for {@link LinReadOptions}.
     */
    public static final class Builder {
        private boolean allowNormalUtf8Encoding = false;

        private Builder() {
        }

        /**
         * Set whether to allow normal UTF-8 encoding instead of the modified UTF-8 encoding of
         * {@link java.io.DataInput}.
         *
         * <p>
         * Note that this option will force checking the bytes to select the correct encoding, which will be slower.
         * It will still accept the modified UTF-8 encoding, but will do its best to disallow mixed encodings.
         * </p>
         *
         * @param allowNormalUtf8Encoding whether to allow normal UTF-8 string encoding
         * @return this builder
         */
        public Builder allowNormalUtf8Encoding(boolean allowNormalUtf8Encoding) {
            this.allowNormalUtf8Encoding = allowNormalUtf8Encoding;
            return this;
        }

        /**
         * Build the options.
         *
         * @return the options
         */
        public LinReadOptions build() {
            return new LinReadOptions(this);
        }

        @Override
        public String toString() {
            return "LinReadOptions.Builder{" +
                "allowNormalUtf8Encoding=" + allowNormalUtf8Encoding +
                '}';
        }
    }

    private final boolean allowNormalUtf8Encoding;

    private LinReadOptions(Builder builder) {
        this.allowNormalUtf8Encoding = builder.allowNormalUtf8Encoding;
    }

    /**
     * {@return whether to allow normal UTF-8 encoding instead of the modified UTF-8 encoding of
     * {@link java.io.DataInput}}
     *
     * <p>
     * Note that this option will force checking the bytes to select the correct encoding, which will be slower.
     * It will still accept the modified UTF-8 encoding, but will do its best to disallow mixed encodings.
     * </p>
     */
    public boolean allowNormalUtf8Encoding() {
        return allowNormalUtf8Encoding;
    }

    @Override
    public String toString() {
        return "LinReadOptions{" +
            "allowNormalUtf8Encoding=" + allowNormalUtf8Encoding +
            '}';
    }
}
