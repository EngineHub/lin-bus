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
import org.enginehub.linbus.stream.LinNbtStreams;
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
     * <p>
     * This will {@linkplain LinNbtStreams#calculateOptionalInfo(Iterator) calculate optional info} before reading.
     * </p>
     *
     * @param tokens the tokens to read from
     * @return the root entry
     */
    public static LinRootEntry readRoot(@NotNull Iterator<? extends @NotNull LinToken> tokens) {
        tokens = LinNbtStreams.calculateOptionalInfo(tokens);
        if (!tokens.hasNext() || !(tokens.next() instanceof LinToken.Name name)) {
            throw new IllegalStateException("Expected root name");
        }
        if (name.id().orElseThrow() != LinTagId.COMPOUND) {
            throw new IllegalStateException("Expected compound tag for root tag");
        }
        var tag = readCompound(tokens);
        return new LinRootEntry(name.name(), tag);
    }

    /**
     * Read a compound tag.
     *
     * <p>
     * This will {@linkplain LinNbtStreams#calculateOptionalInfo(Iterator) calculate optional info} before reading.
     * </p>
     *
     * @param tokens the tokens to read from
     * @return the compound tag
     */
    public static LinCompoundTag readCompound(@NotNull Iterator<? extends @NotNull LinToken> tokens) {
        tokens = LinNbtStreams.calculateOptionalInfo(tokens);
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
            var value = readValue(tokens, LinTagType.fromId(name.id().orElseThrow()));
            builder.put(name.name(), value);
        }
        throw new IllegalStateException("Expected compound end");
    }

    private static LinByteArrayTag readByteArray(Iterator<? extends @NotNull LinToken> tokens) {
        if (!tokens.hasNext() || !(tokens.next() instanceof LinToken.ByteArrayStart start)) {
            throw new IllegalStateException("Expected byte array start");
        }
        var buffer = ByteBuffer.allocate(start.size().orElseThrow());
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

    private static LinIntArrayTag readIntArray(Iterator<? extends @NotNull LinToken> tokens) {
        if (!tokens.hasNext() || !(tokens.next() instanceof LinToken.IntArrayStart start)) {
            throw new IllegalStateException("Expected int array start");
        }
        var buffer = IntBuffer.allocate(start.size().orElseThrow());
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

    private static LinLongArrayTag readLongArray(Iterator<? extends @NotNull LinToken> tokens) {
        if (!tokens.hasNext() || !(tokens.next() instanceof LinToken.LongArrayStart start)) {
            throw new IllegalStateException("Expected long array start");
        }
        var buffer = LongBuffer.allocate(start.size().orElseThrow());
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

    private static <T extends @NotNull LinTag<?, T>> LinListTag<T> readList(Iterator<? extends @NotNull LinToken> tokens) {
        if (!tokens.hasNext() || !(tokens.next() instanceof LinToken.ListStart start)) {
            throw new IllegalStateException("Expected list start");
        }
        @SuppressWarnings("unchecked")
        LinTagType<T> elementType = (LinTagType<T>) LinTagType.fromId(start.elementId().orElseThrow());
        var builder = LinListTag.builder(elementType);
        for (int i = 0; i < start.size().orElseThrow(); i++) {
            T tag = readValue(tokens, elementType);
            builder.add(tag);
        }
        if (!tokens.hasNext() || !(tokens.next() instanceof LinToken.ListEnd)) {
            throw new IllegalStateException("Expected list end");
        }
        return builder.build();
    }

    private static <T extends LinTag<?, T>> T readValue(@NotNull Iterator<? extends @NotNull LinToken> tokens, LinTagType<T> id) {
        return id.cast(switch (id.id()) {
            case BYTE_ARRAY -> readByteArray(tokens);
            case BYTE -> new LinByteTag(((LinToken.Byte) tokens.next()).value());
            case COMPOUND -> readCompound(tokens);
            case DOUBLE -> new LinDoubleTag(((LinToken.Double) tokens.next()).value());
            case END -> throw new IllegalStateException("Unexpected END id");
            case FLOAT -> new LinFloatTag(((LinToken.Float) tokens.next()).value());
            case INT_ARRAY -> readIntArray(tokens);
            case INT -> new LinIntTag(((LinToken.Int) tokens.next()).value());
            case LIST -> readList(tokens);
            case LONG_ARRAY -> readLongArray(tokens);
            case LONG -> new LinLongTag(((LinToken.Long) tokens.next()).value());
            case SHORT -> new LinShortTag(((LinToken.Short) tokens.next()).value());
            case STRING -> new LinStringTag(((LinToken.String) tokens.next()).value());
        });
    }

    private LinTagReader() {
    }
}
