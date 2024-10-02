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

import com.google.common.collect.ImmutableList;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import org.enginehub.linbus.common.LinTagId;
import org.enginehub.linbus.stream.token.LinToken;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.UTFDataFormatException;
import java.io.UncheckedIOException;
import java.nio.charset.MalformedInputException;
import java.nio.charset.StandardCharsets;

import static com.google.common.truth.Truth.assertThat;
import static org.enginehub.linbus.stream.StreamTestUtil.convertNbtStream;
import static org.enginehub.linbus.stream.StreamTestUtil.streamFromIterator;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class JnbtCompatibilityIntegrationTest {

    private static final LinReadOptions OPTIONS = LinReadOptions.builder().allowNormalUtf8Encoding(true).build();

    private static final String NULL_BYTE_TEST_STRING = "Null: \0";
    private static final String TWO_BYTE_TEST_STRING = "2-byte: Ø";
    private static final String THREE_BYTE_TEST_STRING = "3-byte: ඞ";
    private static final String FOUR_BYTE_TEST_STRING = "4-byte: 🐲";
    private static final String FULL_UNICODE_TEST_STRING = NULL_BYTE_TEST_STRING + TWO_BYTE_TEST_STRING
        + THREE_BYTE_TEST_STRING + FOUR_BYTE_TEST_STRING;

    @Test
    void parsesNormalNbtWhenUsingFlag() throws IOException {
        var tokens = convertNbtStream("all-types.nbt.gz", OPTIONS, s -> ImmutableList.copyOf(s.asIterator()));
        assertThat(tokens).containsExactlyElementsIn(LinBinaryIOIntegrationTest.ALL_TYPES_TOKENS).inOrder();
    }

    @Test
    void allDifferentUnicode() throws IOException {
        byte[] bytes = createDifferentUnicodeJnbt();

        // By default, this should not parse due to incorrect encoding
        var uncheckedIoEx = assertThrows(UncheckedIOException.class, () -> ImmutableList.copyOf(
            LinBinaryIO.read(
                new DataInputStream(new ByteArrayInputStream(bytes))
            ).asIterator()
        ));
        assertThat(uncheckedIoEx.getCause()).isInstanceOf(UTFDataFormatException.class);

        // With the compatibility flag on, it should parse correctly
        var tokens = ImmutableList.copyOf(
            LinBinaryIO.read(
                new DataInputStream(new ByteArrayInputStream(bytes)),
                OPTIONS
            ).asIterator()
        );
        assertThat(tokens).containsExactly(
            new LinToken.Name(FULL_UNICODE_TEST_STRING, LinTagId.COMPOUND),
            new LinToken.CompoundStart(),
            new LinToken.CompoundEnd()
        ).inOrder();

        var byteCollector = ByteStreams.newDataOutput();
        LinBinaryIO.write(byteCollector, streamFromIterator(tokens.iterator()));
        // We use modified UTF-8 encoding, so the bytes should not be equal
        assertThat(byteCollector.toByteArray()).isNotEqualTo(bytes);
    }

    private byte[] createDifferentUnicodeJnbt() {
        var byteCollector = ByteStreams.newDataOutput();
        byteCollector.write(LinTagId.COMPOUND.id());
        writeNormalUtf8(byteCollector, FULL_UNICODE_TEST_STRING);
        byteCollector.write(LinTagId.END.id());
        return byteCollector.toByteArray();
    }

    @ParameterizedTest
    @ValueSource(strings = {NULL_BYTE_TEST_STRING, FOUR_BYTE_TEST_STRING, FULL_UNICODE_TEST_STRING})
    void locksInModifiedUtf8IfPossible(String testStr) {
        byte[] bytes = createDualModifiedFirst(testStr);

        // By default, this should not parse due to incorrect encoding
        var uncheckedIoEx = assertThrows(UncheckedIOException.class, () -> ImmutableList.copyOf(
            LinBinaryIO.read(
                new DataInputStream(new ByteArrayInputStream(bytes))
            ).asIterator()
        ));
        assertThat(uncheckedIoEx.getCause()).isInstanceOf(UTFDataFormatException.class);

        // With the compatibility flag on, it should also not parse because we lock in the modified UTF-8 encoding
        uncheckedIoEx = assertThrows(UncheckedIOException.class, () -> ImmutableList.copyOf(
            LinBinaryIO.read(
                new DataInputStream(new ByteArrayInputStream(bytes)),
                OPTIONS
            ).asIterator()
        ));
        assertThat(uncheckedIoEx.getCause()).isInstanceOf(UTFDataFormatException.class);
    }

    private byte[] createDualModifiedFirst(String testStr) {
        var byteCollector = ByteStreams.newDataOutput();
        byteCollector.write(LinTagId.COMPOUND.id());
        byteCollector.writeUTF(""); // Name
        byteCollector.write(LinTagId.STRING.id());
        byteCollector.writeUTF("determiner"); // Name
        byteCollector.writeUTF(testStr);
        byteCollector.write(LinTagId.STRING.id());
        byteCollector.writeUTF("detector"); // Name
        writeNormalUtf8(byteCollector, FULL_UNICODE_TEST_STRING);
        byteCollector.write(LinTagId.END.id());
        return byteCollector.toByteArray();
    }

    // NULL_BYTE_TEST_STRING cannot lock in the encoding, as it is acceptable as input to modified UTF-8
    @ParameterizedTest
    @ValueSource(strings = {FOUR_BYTE_TEST_STRING, FULL_UNICODE_TEST_STRING})
    void locksInNormalUtf8IfPossible(String testStr) {
        byte[] bytes = createDualNormalFirst(testStr);

        // By default, this should not parse due to incorrect encoding
        var uncheckedIoEx = assertThrows(UncheckedIOException.class, () -> ImmutableList.copyOf(
            LinBinaryIO.read(
                new DataInputStream(new ByteArrayInputStream(bytes))
            ).asIterator()
        ));
        assertThat(uncheckedIoEx.getCause()).isInstanceOf(UTFDataFormatException.class);

        // With the compatibility flag on, it should also not parse because we lock in the normal UTF-8 encoding
        uncheckedIoEx = assertThrows(UncheckedIOException.class, () -> ImmutableList.copyOf(
            LinBinaryIO.read(
                new DataInputStream(new ByteArrayInputStream(bytes)),
                OPTIONS
            ).asIterator()
        ));
        assertThat(uncheckedIoEx.getCause()).isInstanceOf(MalformedInputException.class);
    }

    private byte[] createDualNormalFirst(String testStr) {
        var byteCollector = ByteStreams.newDataOutput();
        byteCollector.write(LinTagId.COMPOUND.id());
        byteCollector.writeUTF(""); // Name
        byteCollector.write(LinTagId.STRING.id());
        byteCollector.writeUTF("determiner"); // Name
        writeNormalUtf8(byteCollector, testStr);
        byteCollector.write(LinTagId.STRING.id());
        byteCollector.writeUTF("detector"); // Name
        byteCollector.writeUTF(FULL_UNICODE_TEST_STRING);
        byteCollector.write(LinTagId.END.id());
        return byteCollector.toByteArray();
    }

    private enum StringEncoding {
        NORMAL, MODIFIED
    }

    @ParameterizedTest
    @EnumSource(value = StringEncoding.class)
    void doesNotLockInWhenUndetectable(StringEncoding encoding) {
        byte[] bytes = createAmbiguousWithPostFullString(encoding);

        if (encoding != StringEncoding.MODIFIED) {
            // By default, this should not parse due to incorrect encoding
            var uncheckedIoEx = assertThrows(UncheckedIOException.class, () -> ImmutableList.copyOf(
                LinBinaryIO.read(
                    new DataInputStream(new ByteArrayInputStream(bytes))
                ).asIterator()
            ));
            assertThat(uncheckedIoEx.getCause()).isInstanceOf(UTFDataFormatException.class);
        }

        // With the compatibility flag on, it should parse because we couldn't lock in the encoding
        var tokens = ImmutableList.copyOf(
            LinBinaryIO.read(
                new DataInputStream(new ByteArrayInputStream(bytes)),
                LinReadOptions.builder().allowNormalUtf8Encoding(true).build()
            ).asIterator()
        );
        assertThat(tokens).containsExactly(
            new LinToken.Name("", LinTagId.COMPOUND),
            new LinToken.CompoundStart(),
            new LinToken.Name("null-normal", LinTagId.STRING),
            new LinToken.String(NULL_BYTE_TEST_STRING),
            new LinToken.Name("2byte-normal", LinTagId.STRING),
            new LinToken.String(TWO_BYTE_TEST_STRING),
            new LinToken.Name("2byte-modified", LinTagId.STRING),
            new LinToken.String(TWO_BYTE_TEST_STRING),
            new LinToken.Name("3byte-normal", LinTagId.STRING),
            new LinToken.String(THREE_BYTE_TEST_STRING),
            new LinToken.Name("3byte-modified", LinTagId.STRING),
            new LinToken.String(THREE_BYTE_TEST_STRING),
            new LinToken.Name("decider", LinTagId.STRING),
            new LinToken.String(FULL_UNICODE_TEST_STRING),
            new LinToken.CompoundEnd()
        ).inOrder();
    }

    private byte[] createAmbiguousWithPostFullString(StringEncoding encoding) {
        var byteCollector = ByteStreams.newDataOutput();
        byteCollector.write(LinTagId.COMPOUND.id());
        byteCollector.writeUTF("");

        // All these strings encode the same in normal and modified UTF-8
        // So we can't lock in the encoding

        byteCollector.write(LinTagId.STRING.id());
        byteCollector.writeUTF("null-normal"); // Name
        writeNormalUtf8(byteCollector, NULL_BYTE_TEST_STRING);

        // We don't bother writing the modified version of null, as it can determine the encoding

        byteCollector.write(LinTagId.STRING.id());
        byteCollector.writeUTF("2byte-normal"); // Name
        writeNormalUtf8(byteCollector, TWO_BYTE_TEST_STRING);
        byteCollector.write(LinTagId.STRING.id());
        byteCollector.writeUTF("2byte-modified"); // Name
        byteCollector.writeUTF(TWO_BYTE_TEST_STRING);
        byteCollector.write(LinTagId.STRING.id());
        byteCollector.writeUTF("3byte-normal"); // Name
        writeNormalUtf8(byteCollector, THREE_BYTE_TEST_STRING);
        byteCollector.write(LinTagId.STRING.id());
        byteCollector.writeUTF("3byte-modified"); // Name
        byteCollector.writeUTF(THREE_BYTE_TEST_STRING);
        byteCollector.write(LinTagId.STRING.id()); // String
        byteCollector.writeUTF("decider"); // Name
        switch (encoding) {
            case NORMAL -> writeNormalUtf8(byteCollector, FULL_UNICODE_TEST_STRING);
            case MODIFIED -> byteCollector.writeUTF(FULL_UNICODE_TEST_STRING);
            default -> throw new AssertionError("Unknown encoding: " + encoding);
        }
        byteCollector.write(LinTagId.END.id());
        return byteCollector.toByteArray();
    }

    private static void writeNormalUtf8(ByteArrayDataOutput byteCollector, String str) {
        byte[] stringContent = str.getBytes(StandardCharsets.UTF_8);
        byteCollector.writeShort(stringContent.length);
        byteCollector.write(stringContent, 0, stringContent.length);
    }
}
