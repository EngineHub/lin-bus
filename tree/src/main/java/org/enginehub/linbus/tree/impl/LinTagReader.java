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
import org.enginehub.linbus.stream.LinStream;
import org.enginehub.linbus.stream.exception.NbtParseException;
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

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;

/**
 * Class to hold methods to read tags.
 */
public class LinTagReader {
    /**
     * Read the root entry.
     *
     * <p>
     * This will {@linkplain LinStream#calculateOptionalInfo() calculate optional info} before reading.
     * </p>
     *
     * @param tokens the tokens to read from
     * @return the root entry
     * @throws IOException if an I/O error occurs
     */
    public static LinRootEntry readRoot(@NotNull LinStream tokens) throws IOException {
        tokens = tokens.calculateOptionalInfo();
        if (!(tokens.nextOrNull() instanceof LinToken.Name name)) {
            throw new NbtParseException("Expected root name");
        }
        if (name.id().orElseThrow() != LinTagId.COMPOUND) {
            throw new NbtParseException("Expected compound tag for root tag");
        }
        var tag = readCompound(tokens);
        return new LinRootEntry(name.name(), tag);
    }

    /**
     * Read a compound tag.
     *
     * <p>
     * This will {@linkplain LinStream#calculateOptionalInfo() calculate optional info} before reading.
     * </p>
     *
     * @param tokens the tokens to read from
     * @return the compound tag
     * @throws IOException if an I/O error occurs
     */
    public static LinCompoundTag readCompound(@NotNull LinStream tokens) throws IOException {
        tokens = tokens.linStream();
        if (!(tokens.nextOrNull() instanceof LinToken.CompoundStart)) {
            throw new NbtParseException("Expected compound start");
        }
        var builder = LinCompoundTag.builder();
        while (true) {
            LinToken token = tokens.nextOrNull();
            if (token == null) {
                break;
            }
            if (token instanceof LinToken.CompoundEnd) {
                return builder.build();
            }
            if (!(token instanceof LinToken.Name name)) {
                throw new NbtParseException("Expected name, got " + token);
            }
            var value = readValue(tokens, LinTagType.fromId(name.id().orElseThrow()));
            builder.put(name.name(), value);
        }
        throw new NbtParseException("Expected compound end");
    }

    private static LinByteArrayTag readByteArray(LinStream tokens) throws IOException {
        if (!(tokens.nextOrNull() instanceof LinToken.ByteArrayStart start)) {
            throw new NbtParseException("Expected byte array start");
        }
        var buffer = ByteBuffer.allocate(start.size().orElseThrow());
        while (true) {
            var token = tokens.nextOrNull();
            if (token == null) {
                break;
            }
            if (token instanceof LinToken.ByteArrayEnd) {
                if (buffer.hasRemaining()) {
                    throw new NbtParseException("Not all bytes received");
                }
                return LinByteArrayTag.of(buffer.array());
            }
            if (!(token instanceof LinToken.ByteArrayContent content)) {
                throw new NbtParseException("Expected byte array content, got " + token);
            }
            buffer.put(content.buffer());
        }
        throw new NbtParseException("Expected byte array end");
    }

    private static LinIntArrayTag readIntArray(LinStream tokens) throws IOException {
        if (!(tokens.nextOrNull() instanceof LinToken.IntArrayStart start)) {
            throw new NbtParseException("Expected int array start");
        }
        var buffer = IntBuffer.allocate(start.size().orElseThrow());
        while (true) {
            var token = tokens.nextOrNull();
            if (token == null) {
                break;
            }
            if (token instanceof LinToken.IntArrayEnd) {
                if (buffer.hasRemaining()) {
                    throw new NbtParseException("Not all ints received");
                }
                return LinIntArrayTag.of(buffer.array());
            }
            if (!(token instanceof LinToken.IntArrayContent content)) {
                throw new NbtParseException("Expected int array content, got " + token);
            }
            buffer.put(content.buffer());
        }
        throw new NbtParseException("Expected int array end");
    }

    private static LinLongArrayTag readLongArray(LinStream tokens) throws IOException {
        if (!(tokens.nextOrNull() instanceof LinToken.LongArrayStart start)) {
            throw new NbtParseException("Expected long array start");
        }
        var buffer = LongBuffer.allocate(start.size().orElseThrow());
        while (true) {
            var token = tokens.nextOrNull();
            if (token == null) {
                break;
            }
            if (token instanceof LinToken.LongArrayEnd) {
                if (buffer.hasRemaining()) {
                    throw new NbtParseException("Not all longs received");
                }
                return LinLongArrayTag.of(buffer.array());
            }
            if (!(token instanceof LinToken.LongArrayContent content)) {
                throw new NbtParseException("Expected long array content, got " + token);
            }
            buffer.put(content.buffer());
        }
        throw new NbtParseException("Expected long array end");
    }

    private static <T extends @NotNull LinTag<?>> LinListTag<T> readList(LinStream tokens) throws IOException {
        if (!(tokens.nextOrNull() instanceof LinToken.ListStart start)) {
            throw new NbtParseException("Expected list start");
        }
        @SuppressWarnings("unchecked")
        LinTagType<T> elementType = (LinTagType<T>) LinTagType.fromId(start.elementId().orElseThrow());
        var builder = LinListTag.builder(elementType);
        for (int i = 0; i < start.size().orElseThrow(); i++) {
            T tag = readValue(tokens, elementType);
            builder.add(tag);
        }
        if (!(tokens.nextOrNull() instanceof LinToken.ListEnd)) {
            throw new NbtParseException("Expected list end");
        }
        return builder.build();
    }

    private static <T extends @NotNull LinTag<?>> T readValue(@NotNull LinStream tokens, LinTagType<T> id) throws IOException {
        return id.cast(switch (id.id()) {
            case BYTE_ARRAY -> readByteArray(tokens);
            case BYTE -> LinByteTag.of(((LinToken.Byte) requireNextToken(tokens)).value());
            case COMPOUND -> readCompound(tokens);
            case DOUBLE -> LinDoubleTag.of(((LinToken.Double) requireNextToken(tokens)).value());
            case END -> throw new NbtParseException("Unexpected END id");
            case FLOAT -> LinFloatTag.of(((LinToken.Float) requireNextToken(tokens)).value());
            case INT_ARRAY -> readIntArray(tokens);
            case INT -> LinIntTag.of(((LinToken.Int) requireNextToken(tokens)).value());
            case LIST -> readList(tokens);
            case LONG_ARRAY -> readLongArray(tokens);
            case LONG -> LinLongTag.of(((LinToken.Long) requireNextToken(tokens)).value());
            case SHORT -> LinShortTag.of(((LinToken.Short) requireNextToken(tokens)).value());
            case STRING -> LinStringTag.of(((LinToken.String) requireNextToken(tokens)).value());
        });
    }

    private static LinToken requireNextToken(@NotNull LinStream tokens) throws IOException {
        LinToken linToken = tokens.nextOrNull();
        if (linToken == null) {
            throw new NbtParseException("Unexpected end of stream");
        }
        return linToken;
    }

    private LinTagReader() {
    }
}
