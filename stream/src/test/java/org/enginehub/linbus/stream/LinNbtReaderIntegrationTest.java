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

import com.google.common.io.Resources;
import org.enginehub.linbus.common.LinTagId;
import org.enginehub.linbus.stream.visitor.LinRootVisitor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.DataInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.zip.GZIPInputStream;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class LinNbtReaderIntegrationTest {

    private static void acceptFromFile(String name, LinRootVisitor visitor) throws IOException {
        var resource = Resources.getResource(name);
        try (var stream = Resources.asByteSource(resource).openStream();
             var decompressed = name.endsWith(".gz") ? new GZIPInputStream(stream) : stream) {
            LinNbtReader.accept(new DataInputStream(decompressed), visitor);
        }
    }

    @Test
    void bigtest(
        @Mock(answer = Answers.RETURNS_DEEP_STUBS) LinRootVisitor rootVisitor
    ) throws IOException {
        // STUB: set up specialized answers
        var levelVisitor = rootVisitor.visitValue("Level");
        var byteArrayVisitor = levelVisitor.visitValueByteArray(
            "byteArrayTest (the first 1000 values of (n*n*255+n*7)%100, starting with n=0 (0, 62, 34, 16, 8, ...))"
        );

        doAnswer(invocation -> {
            ByteBuffer buffer = invocation.getArgument(0);
            buffer.position(buffer.limit());
            return null;
        }).when(byteArrayVisitor).visitChunk(any());

        // EXECUTE: This is the actual code under test.
        acceptFromFile("bigtest.nbt.gz", rootVisitor);

        // VERIFY: Validate that the data was loaded correctly & in order
        var nestedVisitor = levelVisitor.visitValueCompound("nested compound test");
        var nestedHamVisitor = nestedVisitor.visitValueCompound("ham");
        var nestedHamNameVisitor = nestedHamVisitor.visitValueString("name");
        var nestedHamValueVisitor = nestedHamVisitor.visitValueFloat("value");
        var nestedEggVisitor = nestedVisitor.visitValueCompound("egg");
        var nestedEggNameVisitor = nestedEggVisitor.visitValueString("name");
        var nestedEggValueVisitor = nestedEggVisitor.visitValueFloat("value");
        var listLongVisitor = levelVisitor.visitValueList("listTest (long)");
        var listCompoundVisitor = levelVisitor.visitValueList("listTest (compound)");
        var inOrder = inOrder(
            rootVisitor,
            levelVisitor,
            levelVisitor.visitValueLong("longTest"),
            levelVisitor.visitValueShort("shortTest"),
            levelVisitor.visitValueString("stringTest"),
            levelVisitor.visitValueFloat("floatTest"),
            levelVisitor.visitValueInt("intTest"),
            nestedVisitor,
            nestedHamVisitor,
            nestedHamNameVisitor,
            nestedHamValueVisitor,
            nestedEggVisitor,
            nestedEggNameVisitor,
            nestedEggValueVisitor,
            listLongVisitor,
            listLongVisitor.visitValueLong(0),
            listLongVisitor.visitValueLong(1),
            listLongVisitor.visitValueLong(2),
            listLongVisitor.visitValueLong(3),
            listLongVisitor.visitValueLong(4),
            listCompoundVisitor,
            listCompoundVisitor.visitValueCompound(0),
            listCompoundVisitor.visitValueCompound(0).visitValueString("name"),
            listCompoundVisitor.visitValueCompound(0).visitValueLong("created-on"),
            listCompoundVisitor.visitValueCompound(1),
            listCompoundVisitor.visitValueCompound(1).visitValueString("name"),
            listCompoundVisitor.visitValueCompound(1).visitValueLong("created-on"),
            levelVisitor.visitValueByte("byteTest"),
            byteArrayVisitor,
            levelVisitor.visitValueDouble("doubleTest")
        );
        inOrder.verify(rootVisitor).visitValue("Level");

        inOrder.verify(levelVisitor.visitValueLong("longTest")).visitLong(9223372036854775807L);

        inOrder.verify(levelVisitor.visitValueShort("shortTest")).visitShort((short) 32767);

        inOrder.verify(levelVisitor.visitValueString("stringTest"))
            .visitString("HELLO WORLD THIS IS A TEST STRING ÅÄÖ!");

        inOrder.verify(levelVisitor.visitValueFloat("floatTest")).visitFloat(0.49823147058486938f);

        inOrder.verify(levelVisitor.visitValueInt("intTest")).visitInt(2147483647);

        inOrder.verify(nestedHamNameVisitor).visitString("Hampus");
        inOrder.verify(nestedHamValueVisitor).visitFloat(0.75f);
        inOrder.verify(nestedHamVisitor).visitEnd();
        inOrder.verify(nestedEggNameVisitor).visitString("Eggbert");
        inOrder.verify(nestedEggValueVisitor).visitFloat(0.5f);
        inOrder.verify(nestedEggVisitor).visitEnd();
        inOrder.verify(nestedVisitor).visitEnd();

        inOrder.verify(listLongVisitor).visitSizeAndType(5, LinTagId.LONG);
        long[] listLongValue = {11, 12, 13, 14, 15};
        for (int i = 0; i < listLongValue.length; i++) {
            inOrder.verify(listLongVisitor).visitValueLong(i);
            inOrder.verify(listLongVisitor.visitValueLong(i)).visitLong(listLongValue[i]);
        }
        inOrder.verify(listLongVisitor).visitEnd();

        inOrder.verify(listCompoundVisitor).visitSizeAndType(2, LinTagId.COMPOUND);
        record ListCompoundValue(String name, long createdOn) {
        }
        ListCompoundValue[] listCompoundValue = {
            new ListCompoundValue("Compound tag #0", 1264099775885L),
            new ListCompoundValue("Compound tag #1", 1264099775885L)
        };
        for (int i = 0; i < listCompoundValue.length; i++) {
            inOrder.verify(listCompoundVisitor).visitValueCompound(i);
            inOrder.verify(listCompoundVisitor.visitValueCompound(i).visitValueString("name"))
                .visitString(listCompoundValue[i].name());
            inOrder.verify(listCompoundVisitor.visitValueCompound(i).visitValueLong("created-on"))
                .visitLong(listCompoundValue[i].createdOn());
            inOrder.verify(listCompoundVisitor.visitValueCompound(i)).visitEnd();
        }
        inOrder.verify(listCompoundVisitor).visitEnd();

        inOrder.verify(levelVisitor.visitValueByte("byteTest")).visitByte((byte) 127);

        inOrder.verify(byteArrayVisitor).visitSize(1000);
        inOrder.verify(byteArrayVisitor).visitChunk(argThat(buffer -> {
            byte[] content = new byte[buffer.remaining()];
            buffer.get(content);
            for (int i = 0; i < content.length; i++) {
                if (content[i] != (byte) ((i * i * 255 + i * 7) % 100)) {
                    return false;
                }
            }
            return true;
        }));
        inOrder.verify(byteArrayVisitor).visitEnd();

        inOrder.verify(levelVisitor.visitValueDouble("doubleTest")).visitDouble(0.49312871321823148);

        inOrder.verify(levelVisitor).visitEnd();

        verify(levelVisitor, never()).visitValueIntArray(any());
        verify(levelVisitor, never()).visitValueLongArray(any());
    }
}
