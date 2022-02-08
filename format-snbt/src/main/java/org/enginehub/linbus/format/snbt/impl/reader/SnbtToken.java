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

package org.enginehub.linbus.format.snbt.impl.reader;

import org.enginehub.linbus.stream.token.LinToken;

/**
 * Not to be confused with {@link LinToken}.
 */
public sealed interface SnbtToken {
    /**
     * '{'.
     */
    enum CompoundStart implements SnbtToken {
        /**
         * The instance.
         */
        INSTANCE;

        @Override
        public String toString() {
            return "'{'";
        }
    }

    /**
     * '}'.
     */
    enum CompoundEnd implements SnbtToken {
        /**
         * The instance.
         */
        INSTANCE;

        @Override
        public String toString() {
            return "'}'";
        }
    }

    /**
     * '['
     */
    enum ListLikeStart implements SnbtToken {
        /**
         * The instance.
         */
        INSTANCE;

        @Override
        public String toString() {
            return "'['";
        }
    }

    /**
     * ']'
     */
    enum ListLikeEnd implements SnbtToken {
        /**
         * The instance.
         */
        INSTANCE;

        @Override
        public String toString() {
            return "']'";
        }
    }

    /**
     * ':'
     */
    enum EntrySeparator implements SnbtToken {
        /**
         * The instance.
         */
        INSTANCE;

        @Override
        public String toString() {
            return "':'";
        }
    }

    /**
     * ';'
     */
    enum ListTypeSeparator implements SnbtToken {
        /**
         * The instance.
         */
        INSTANCE;

        @Override
        public String toString() {
            return "';'";
        }
    }

    /**
     * ','
     */
    enum Separator implements SnbtToken {
        /**
         * The instance.
         */
        INSTANCE;

        @Override
        public String toString() {
            return "','";
        }
    }

    /**
     * String literal for arbitrary text.
     */
    record Text(boolean quoted, String content) implements SnbtToken {
    }
}
