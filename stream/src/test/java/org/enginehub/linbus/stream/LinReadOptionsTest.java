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

import org.junit.jupiter.api.Test;

import static com.google.common.truth.Truth.assertThat;

public class LinReadOptionsTest {
    @Test
    void defaultsToModifiedUtf8() {
        assertThat(LinReadOptions.builder().build().allowNormalUtf8Encoding()).isFalse();
    }

    @Test
    void allowNormalUtf8Encoding() {
        assertThat(LinReadOptions.builder().allowNormalUtf8Encoding(true).build().allowNormalUtf8Encoding()).isTrue();
    }

    @Test
    void builderToString() {
        assertThat(LinReadOptions.builder().toString())
            .isEqualTo("LinReadOptions.Builder{allowNormalUtf8Encoding=false}");
    }

    @Test
    void optionsToString() {
        assertThat(LinReadOptions.builder().allowNormalUtf8Encoding(true).build().toString())
            .isEqualTo("LinReadOptions{allowNormalUtf8Encoding=true}");
    }
}
