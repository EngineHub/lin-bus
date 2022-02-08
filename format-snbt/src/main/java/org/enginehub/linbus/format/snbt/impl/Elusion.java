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

package org.enginehub.linbus.format.snbt.impl;

/**
 * Escape helpers.
 */
public class Elusion {
    /**
     * Is {@code c} a character than can be emitted without quotes?
     *
     * @param c character to check
     * @return {@code true} if {@code c} is a safe character
     */
    public static boolean isSafeCharacter(char c) {
        return ('A' <= c && c <= 'Z')
            || ('a' <= c && c <= 'z')
            || ('0' <= c && c <= '9')
            || c == '_' || c == '.' || c == '+' || c == '-';
    }

    /**
     * Escape (and quote) the given string if needed.
     *
     * @param s the string to escape
     * @return escaped string, or the original string if it was safe
     */
    public static CharSequence escapeIfNeeded(String s) {
        boolean totallySafe = true;
        int singleCharCount = 0;
        int doubleCharCount = 0;
        int backslashCount = 0;
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (totallySafe && isSafeCharacter(c)) {
                continue;
            }
            totallySafe = false;
            switch (c) {
                case '\'' -> singleCharCount++;
                case '"' -> doubleCharCount++;
                case '\\' -> backslashCount++;
            }
        }
        if (totallySafe) {
            return s;
        }

        var leastCharCount = Math.min(singleCharCount, doubleCharCount);
        // Original length + backslashes + quotes that will be escaped + wrapping quotes
        var builder = new StringBuilder(s.length() + backslashCount + leastCharCount + 2);
        // Quote using the least common quote character, so there's less escapes.
        char quoteChar = leastCharCount == singleCharCount ? '\'' : '"';
        builder.append(quoteChar);
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '\\' || c == quoteChar) {
                builder.append('\\');
            }
            builder.append(c);
        }
        builder.append(quoteChar);
        return builder;
    }
}
