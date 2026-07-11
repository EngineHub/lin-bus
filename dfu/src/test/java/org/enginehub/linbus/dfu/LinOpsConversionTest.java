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

package org.enginehub.linbus.dfu;

import com.google.common.io.ByteStreams;
import com.google.common.io.Resources;
import com.mojang.serialization.JavaOps;
import org.enginehub.linbus.stream.LinBinaryIO;
import org.enginehub.linbus.tree.LinCompoundTag;
import org.enginehub.linbus.tree.LinEndTag;
import org.enginehub.linbus.tree.LinRootEntry;
import org.enginehub.linbus.tree.LinTag;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.zip.GZIPInputStream;

import static com.google.common.truth.Truth.assertThat;

class LinOpsConversionTest {

    private static final LinOps OPS = LinOps.getInstance();

    private static LinCompoundTag allTypes() throws IOException {
        URL resource = Resources.getResource("all-types.nbt.gz");
        byte[] data;
        try (InputStream input = Resources.asByteSource(resource).openStream();
             GZIPInputStream decompressed = new GZIPInputStream(input)) {
            data = decompressed.readAllBytes();
        }
        return LinBinaryIO.readUsing(ByteStreams.newDataInput(data), LinRootEntry::readFrom).toLinTag();
    }

    private static LinCompoundTag roundTripThroughJavaOps(LinCompoundTag original) {
        Object java = OPS.convertTo(JavaOps.INSTANCE, original);
        LinTag<?> back = JavaOps.INSTANCE.convertTo(OPS, java);

        assertThat(back).isInstanceOf(LinCompoundTag.class);
        return (LinCompoundTag) back;
    }

    @Test
    void convertToSelfRoundTripsEveryTagType() throws IOException {
        LinCompoundTag tag = allTypes();
        assertThat(OPS.convertTo(OPS, tag)).isEqualTo(tag);
    }

    @Test
    void convertToSelfKeepsEndTag() {
        assertThat(OPS.convertTo(OPS, LinEndTag.instance())).isEqualTo(LinEndTag.instance());
    }

    @Test
    void convertThroughJavaOpsIsLossless() throws IOException {
        LinCompoundTag tag = allTypes();
        assertThat(roundTripThroughJavaOps(tag)).isEqualTo(tag);
    }
}
