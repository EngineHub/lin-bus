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
import org.enginehub.linbus.stream.token.LinToken;
import org.junit.jupiter.api.Test;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class LinTagReaderTest {
    @Test
    void noRootName() {
        {
            var thrown = assertThrows(
                IllegalStateException.class,
                () -> LinTagReader.readRoot(LinStream.of())
            );
            assertThat(thrown).hasMessageThat().isEqualTo("Expected root name");
        }
        {
            var thrown = assertThrows(
                IllegalStateException.class,
                () -> LinTagReader.readRoot(LinStream.of(new LinToken.String("bogus")))
            );
            assertThat(thrown).hasMessageThat().isEqualTo("Expected root name");
        }
    }

    @Test
    void wrongRootTagId() {
        var thrown = assertThrows(
            IllegalStateException.class,
            () -> LinTagReader.readRoot(LinStream.of(
                new LinToken.Name("root", LinTagId.STRING)
            ))
        );
        assertThat(thrown).hasMessageThat().isEqualTo("Expected compound tag for root tag");
    }

    @Test
    void noCompoundStart() {
        {
            var thrown = assertThrows(
                IllegalStateException.class,
                () -> LinTagReader.readRoot(LinStream.of(
                    new LinToken.Name("root", LinTagId.COMPOUND)
                ))
            );
            assertThat(thrown).hasMessageThat().isEqualTo("Expected compound start");
        }
        {
            var thrown = assertThrows(
                IllegalStateException.class,
                () -> LinTagReader.readRoot(LinStream.of(
                    new LinToken.Name("root", LinTagId.COMPOUND),
                    new LinToken.String("bogus")
                ))
            );
            assertThat(thrown).hasMessageThat().isEqualTo("Expected compound start");
        }
    }

    @Test
    void noNameInCompound() {
        var thrown = assertThrows(
            IllegalStateException.class,
            () -> LinTagReader.readRoot(LinStream.of(
                new LinToken.Name("root", LinTagId.COMPOUND),
                new LinToken.CompoundStart(),
                new LinToken.String("bogus")
            ))
        );
        assertThat(thrown).hasMessageThat().isEqualTo("Expected name, got String[value=bogus]");
    }

    @Test
    void noEndOfCompound() {
        var thrown = assertThrows(
            IllegalStateException.class,
            () -> LinTagReader.readRoot(LinStream.of(
                new LinToken.Name("root", LinTagId.COMPOUND),
                new LinToken.CompoundStart()
            ))
        );
        assertThat(thrown).hasMessageThat().isEqualTo("Expected compound end");
    }

    @Test
    void noByteArrayStart() {
        {
            var thrown = assertThrows(
                IllegalStateException.class,
                () -> LinTagReader.readRoot(LinStream.of(
                    new LinToken.Name("root", LinTagId.COMPOUND),
                    new LinToken.CompoundStart(),
                    new LinToken.Name("inner", LinTagId.BYTE_ARRAY)
                ))
            );
            assertThat(thrown).hasMessageThat().isEqualTo("Expected byte array start");
        }
        {
            var thrown = assertThrows(
                IllegalStateException.class,
                () -> LinTagReader.readRoot(LinStream.of(
                    new LinToken.Name("root", LinTagId.COMPOUND),
                    new LinToken.CompoundStart(),
                    new LinToken.Name("inner", LinTagId.BYTE_ARRAY),
                    new LinToken.String("bogus")
                ))
            );
            assertThat(thrown).hasMessageThat().isEqualTo("Expected byte array start");
        }
    }

    @Test
    void notByteArrayContent() {
        var thrown = assertThrows(
            IllegalStateException.class,
            () -> LinTagReader.readRoot(LinStream.of(
                new LinToken.Name("root", LinTagId.COMPOUND),
                new LinToken.CompoundStart(),
                new LinToken.Name("inner", LinTagId.BYTE_ARRAY),
                new LinToken.ByteArrayStart(1),
                new LinToken.String("bogus")
            ))
        );
        assertThat(thrown).hasMessageThat().isEqualTo("Expected byte array content, got String[value=bogus]");
    }

    @Test
    void noEndOfByteArray() {
        var thrown = assertThrows(
            IllegalStateException.class,
            () -> LinTagReader.readRoot(LinStream.of(
                new LinToken.Name("root", LinTagId.COMPOUND),
                new LinToken.CompoundStart(),
                new LinToken.Name("inner", LinTagId.BYTE_ARRAY),
                new LinToken.ByteArrayStart(1)
            ))
        );
        assertThat(thrown).hasMessageThat().isEqualTo("Expected byte array end");
    }

    @Test
    void earlyEndOfByteArray() {
        var thrown = assertThrows(
            IllegalStateException.class,
            () -> LinTagReader.readRoot(LinStream.of(
                new LinToken.Name("root", LinTagId.COMPOUND),
                new LinToken.CompoundStart(),
                new LinToken.Name("inner", LinTagId.BYTE_ARRAY),
                new LinToken.ByteArrayStart(1),
                new LinToken.ByteArrayEnd()
            ))
        );
        assertThat(thrown).hasMessageThat().isEqualTo("Not all bytes received");
    }

    @Test
    void noIntArrayStart() {
        {
            var thrown = assertThrows(
                IllegalStateException.class,
                () -> LinTagReader.readRoot(LinStream.of(
                    new LinToken.Name("root", LinTagId.COMPOUND),
                    new LinToken.CompoundStart(),
                    new LinToken.Name("inner", LinTagId.INT_ARRAY)
                ))
            );
            assertThat(thrown).hasMessageThat().isEqualTo("Expected int array start");
        }
        {
            var thrown = assertThrows(
                IllegalStateException.class,
                () -> LinTagReader.readRoot(LinStream.of(
                    new LinToken.Name("root", LinTagId.COMPOUND),
                    new LinToken.CompoundStart(),
                    new LinToken.Name("inner", LinTagId.INT_ARRAY),
                    new LinToken.String("bogus")
                ))
            );
            assertThat(thrown).hasMessageThat().isEqualTo("Expected int array start");
        }
    }

    @Test
    void notIntArrayContent() {
        var thrown = assertThrows(
            IllegalStateException.class,
            () -> LinTagReader.readRoot(LinStream.of(
                new LinToken.Name("root", LinTagId.COMPOUND),
                new LinToken.CompoundStart(),
                new LinToken.Name("inner", LinTagId.INT_ARRAY),
                new LinToken.IntArrayStart(1),
                new LinToken.String("bogus")
            ))
        );
        assertThat(thrown).hasMessageThat().isEqualTo("Expected int array content, got String[value=bogus]");
    }

    @Test
    void noEndOfIntArray() {
        var thrown = assertThrows(
            IllegalStateException.class,
            () -> LinTagReader.readRoot(LinStream.of(
                new LinToken.Name("root", LinTagId.COMPOUND),
                new LinToken.CompoundStart(),
                new LinToken.Name("inner", LinTagId.INT_ARRAY),
                new LinToken.IntArrayStart(1)
            ))
        );
        assertThat(thrown).hasMessageThat().isEqualTo("Expected int array end");
    }

    @Test
    void earlyEndOfIntArray() {
        var thrown = assertThrows(
            IllegalStateException.class,
            () -> LinTagReader.readRoot(LinStream.of(
                new LinToken.Name("root", LinTagId.COMPOUND),
                new LinToken.CompoundStart(),
                new LinToken.Name("inner", LinTagId.INT_ARRAY),
                new LinToken.IntArrayStart(1),
                new LinToken.IntArrayEnd()
            ))
        );
        assertThat(thrown).hasMessageThat().isEqualTo("Not all ints received");
    }

    @Test
    void noLongArrayStart() {
        {
            var thrown = assertThrows(
                IllegalStateException.class,
                () -> LinTagReader.readRoot(LinStream.of(
                    new LinToken.Name("root", LinTagId.COMPOUND),
                    new LinToken.CompoundStart(),
                    new LinToken.Name("inner", LinTagId.LONG_ARRAY)
                ))
            );
            assertThat(thrown).hasMessageThat().isEqualTo("Expected long array start");
        }
        {
            var thrown = assertThrows(
                IllegalStateException.class,
                () -> LinTagReader.readRoot(LinStream.of(
                    new LinToken.Name("root", LinTagId.COMPOUND),
                    new LinToken.CompoundStart(),
                    new LinToken.Name("inner", LinTagId.LONG_ARRAY),
                    new LinToken.String("bogus")
                ))
            );
            assertThat(thrown).hasMessageThat().isEqualTo("Expected long array start");
        }
    }

    @Test
    void notLongArrayContent() {
        var thrown = assertThrows(
            IllegalStateException.class,
            () -> LinTagReader.readRoot(LinStream.of(
                new LinToken.Name("root", LinTagId.COMPOUND),
                new LinToken.CompoundStart(),
                new LinToken.Name("inner", LinTagId.LONG_ARRAY),
                new LinToken.LongArrayStart(1),
                new LinToken.String("bogus")
            ))
        );
        assertThat(thrown).hasMessageThat().isEqualTo("Expected long array content, got String[value=bogus]");
    }

    @Test
    void noEndOfLongArray() {
        var thrown = assertThrows(
            IllegalStateException.class,
            () -> LinTagReader.readRoot(LinStream.of(
                new LinToken.Name("root", LinTagId.COMPOUND),
                new LinToken.CompoundStart(),
                new LinToken.Name("inner", LinTagId.LONG_ARRAY),
                new LinToken.LongArrayStart(1)
            ))
        );
        assertThat(thrown).hasMessageThat().isEqualTo("Expected long array end");
    }

    @Test
    void earlyEndOfLongArray() {
        var thrown = assertThrows(
            IllegalStateException.class,
            () -> LinTagReader.readRoot(LinStream.of(
                new LinToken.Name("root", LinTagId.COMPOUND),
                new LinToken.CompoundStart(),
                new LinToken.Name("inner", LinTagId.LONG_ARRAY),
                new LinToken.LongArrayStart(1),
                new LinToken.LongArrayEnd()
            ))
        );
        assertThat(thrown).hasMessageThat().isEqualTo("Not all longs received");
    }

    @Test
    void noListStart() {
        {
            var thrown = assertThrows(
                IllegalStateException.class,
                () -> LinTagReader.readRoot(LinStream.of(
                    new LinToken.Name("root", LinTagId.COMPOUND),
                    new LinToken.CompoundStart(),
                    new LinToken.Name("inner", LinTagId.LIST)
                ))
            );
            assertThat(thrown).hasMessageThat().isEqualTo("Expected list start");
        }
        {
            var thrown = assertThrows(
                IllegalStateException.class,
                () -> LinTagReader.readRoot(LinStream.of(
                    new LinToken.Name("root", LinTagId.COMPOUND),
                    new LinToken.CompoundStart(),
                    new LinToken.Name("inner", LinTagId.LIST),
                    new LinToken.String("bogus")
                ))
            );
            assertThat(thrown).hasMessageThat().isEqualTo("Expected list start");
        }
    }

    @Test
    void noListEnd() {
        {
            var thrown = assertThrows(
                IllegalStateException.class,
                () -> LinTagReader.readRoot(LinStream.of(
                    new LinToken.Name("root", LinTagId.COMPOUND),
                    new LinToken.CompoundStart(),
                    new LinToken.Name("inner", LinTagId.LIST),
                    new LinToken.ListStart(0, LinTagId.STRING)
                ))
            );
            assertThat(thrown).hasMessageThat().isEqualTo("Expected list end");
        }
        {
            var thrown = assertThrows(
                IllegalStateException.class,
                () -> LinTagReader.readRoot(LinStream.of(
                    new LinToken.Name("root", LinTagId.COMPOUND),
                    new LinToken.CompoundStart(),
                    new LinToken.Name("inner", LinTagId.LIST),
                    new LinToken.ListStart(0, LinTagId.STRING),
                    new LinToken.String("bogus")
                ))
            );
            assertThat(thrown).hasMessageThat().isEqualTo("Expected list end");
        }
    }

    @Test
    void unexpectedEndId() {
        var thrown = assertThrows(
            IllegalStateException.class,
            () -> LinTagReader.readRoot(LinStream.of(
                new LinToken.Name("root", LinTagId.COMPOUND),
                new LinToken.CompoundStart(),
                new LinToken.Name("inner", LinTagId.LIST),
                new LinToken.ListStart(1, LinTagId.END)
            ))
        );
        assertThat(thrown).hasMessageThat().isEqualTo("Unexpected END id");
    }
}
