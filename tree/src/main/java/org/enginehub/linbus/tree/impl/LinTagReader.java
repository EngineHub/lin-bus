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

package org.enginehub.linbus.tree.impl;

import org.enginehub.linbus.common.LinTagId;
import org.enginehub.linbus.stream.token.LinToken;
import org.enginehub.linbus.tree.LinByteArrayTag;
import org.enginehub.linbus.tree.LinByteTag;
import org.enginehub.linbus.tree.LinCompoundTag;
import org.enginehub.linbus.tree.LinDoubleTag;
import org.enginehub.linbus.tree.LinFloatTag;
import org.enginehub.linbus.tree.LinIntArrayTag;
import org.enginehub.linbus.tree.LinIntTag;
import org.enginehub.linbus.tree.LinListTag;
import org.enginehub.linbus.tree.LinLongArrayTag;
import org.enginehub.linbus.tree.LinLongTag;
import org.enginehub.linbus.tree.LinRootEntry;
import org.enginehub.linbus.tree.LinShortTag;
import org.enginehub.linbus.tree.LinStringTag;
import org.enginehub.linbus.tree.LinTag;
import org.enginehub.linbus.tree.LinTagType;
import org.jetbrains.annotations.NotNull;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.util.Iterator;

/**
 * Class to hold methods to read tags.
 */
public class LinTagReader {
    /**
     * Read the root entry.
     *
     * @param tokens the tokens to read from
     * @return the root entry
     */
    public static LinRootEntry readRoot(@NotNull Iterator<? extends LinToken> tokens) {
        if (!tokens.hasNext() || !(tokens.next() instanceof LinToken.Name name)) {
            throw new IllegalStateException("Expected root name");
        }
        var tag = readCompound(tokens);
        return new LinRootEntry(name.name(), tag);
    }

    private static LinCompoundTag readCompound(@NotNull Iterator<? extends LinToken> tokens) {
        if (!tokens.hasNext() || !(tokens.next() instanceof LinToken.CompoundStart)) {
            throw new IllegalStateException("Expected compound start");
        }
        var builder = LinCompoundTag.builder();
        while (tokens.hasNext()) {
            LinToken token = tokens.next();
            if (token instanceof LinToken.CompoundEnd) {
                return builder.build();
            }
            if (!(token instanceof LinToken.Name name)) {
                throw new IllegalStateException("Expected name, got " + token);
            }
            LinTag<?, ?> value = readValue(tokens, name.id());
            builder.put(name.name(), value);
        }
        throw new IllegalStateException("Expected compound end");
    }

    private static LinByteArrayTag readByteArray(Iterator<? extends LinToken> tokens) {
        if (!tokens.hasNext() || !(tokens.next() instanceof LinToken.ByteArrayStart start)) {
            throw new IllegalStateException("Expected byte array start");
        }
        var buffer = ByteBuffer.allocate(start.size());
        while (tokens.hasNext()) {
            var token = tokens.next();
            if (token instanceof LinToken.ByteArrayEnd) {
                if (buffer.hasRemaining()) {
                    throw new IllegalStateException("Not all bytes received");
                }
                return new LinByteArrayTag(buffer.array());
            }
            if (!(token instanceof LinToken.ByteArrayContent content)) {
                throw new IllegalStateException("Expected byte array content, got " + token);
            }
            buffer.put(content.buffer());
        }
        throw new IllegalStateException("Expected byte array end");
    }

    private static LinIntArrayTag readIntArray(Iterator<? extends LinToken> tokens) {
        if (!tokens.hasNext() || !(tokens.next() instanceof LinToken.IntArrayStart start)) {
            throw new IllegalStateException("Expected int array start");
        }
        var buffer = IntBuffer.allocate(start.size());
        while (tokens.hasNext()) {
            var token = tokens.next();
            if (token instanceof LinToken.IntArrayEnd) {
                if (buffer.hasRemaining()) {
                    throw new IllegalStateException("Not all ints received");
                }
                return new LinIntArrayTag(buffer.array());
            }
            if (!(token instanceof LinToken.IntArrayContent content)) {
                throw new IllegalStateException("Expected int array content, got " + token);
            }
            buffer.put(content.buffer());
        }
        throw new IllegalStateException("Expected int array end");
    }

    private static LinLongArrayTag readLongArray(Iterator<? extends LinToken> tokens) {
        if (!tokens.hasNext() || !(tokens.next() instanceof LinToken.LongArrayStart start)) {
            throw new IllegalStateException("Expected long array start");
        }
        var buffer = LongBuffer.allocate(start.size());
        while (tokens.hasNext()) {
            var token = tokens.next();
            if (token instanceof LinToken.LongArrayEnd) {
                if (buffer.hasRemaining()) {
                    throw new IllegalStateException("Not all longs received");
                }
                return new LinLongArrayTag(buffer.array());
            }
            if (!(token instanceof LinToken.LongArrayContent content)) {
                throw new IllegalStateException("Expected long array content, got " + token);
            }
            buffer.put(content.buffer());
        }
        throw new IllegalStateException("Expected long array end");
    }

    private static LinListTag<?> readList(Iterator<? extends LinToken> tokens) {
        if (!tokens.hasNext() || !(tokens.next() instanceof LinToken.ListStart start)) {
            throw new IllegalStateException("Expected list start");
        }
        @SuppressWarnings("unchecked")
        var builder = LinListTag.builder((LinTagType<LinTag<?, ?>>) LinTagType.fromId(start.elementId()));
        for (int i = 0; i < start.size(); i++) {
            builder.add(readValue(tokens, start.elementId()));
        }
        if (!tokens.hasNext() || !(tokens.next() instanceof LinToken.ListEnd)) {
            throw new IllegalStateException("Expected list end");
        }
        return builder.build();
    }

    private static LinTag<?, ?> readValue(@NotNull Iterator<? extends LinToken> tokens, LinTagId id) {
        return switch (id) {
            case BYTE_ARRAY -> readByteArray(tokens);
            case BYTE -> new LinByteTag(((LinToken.Byte) tokens.next()).value());
            case COMPOUND -> readCompound(tokens);
            case DOUBLE -> new LinDoubleTag(((LinToken.Double) tokens.next()).value());
            case END -> throw new IllegalArgumentException("Unexpected END");
            case FLOAT -> new LinFloatTag(((LinToken.Float) tokens.next()).value());
            case INT_ARRAY -> readIntArray(tokens);
            case INT -> new LinIntTag(((LinToken.Int) tokens.next()).value());
            case LIST -> readList(tokens);
            case LONG_ARRAY -> readLongArray(tokens);
            case LONG -> new LinLongTag(((LinToken.Long) tokens.next()).value());
            case SHORT -> new LinShortTag(((LinToken.Short) tokens.next()).value());
            case STRING -> new LinStringTag(((LinToken.String) tokens.next()).value());
        };
    }
}
