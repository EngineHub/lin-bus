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

import com.mojang.serialization.DataResult;
import com.mojang.serialization.RecordBuilder;
import org.enginehub.linbus.tree.LinCompoundTag;
import org.enginehub.linbus.tree.LinEndTag;
import org.enginehub.linbus.tree.LinTag;
import org.jspecify.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.Map;

final class LinRecordBuilder extends RecordBuilder.AbstractStringBuilder<LinTag<?>, Map<String, LinTag<?>>> {
    LinRecordBuilder(LinOps ops) {
        super(ops);
    }

    @Override
    public RecordBuilder<LinTag<?>> add(String key, LinTag<?> value) {
        if (value instanceof LinEndTag) {
            return withErrorsFrom(DataResult.error(() -> "Cannot add END tag to compound: " + key));
        }
        return super.add(key, value);
    }

    @Override
    public RecordBuilder<LinTag<?>> add(String key, DataResult<LinTag<?>> value) {
        return super.add(
            key,
            value.flatMap(tag -> tag instanceof LinEndTag
                ? DataResult.error(() -> "Cannot add END tag to compound: " + key)
                : DataResult.success(tag)
            )
        );
    }

    @Override
    protected Map<String, LinTag<?>> initBuilder() {
        return new LinkedHashMap<>();
    }

    @Override
    protected Map<String, LinTag<?>> append(String key, LinTag<?> value, Map<String, LinTag<?>> builder) {
        builder.put(key, value);
        return builder;
    }

    @Override
    protected DataResult<LinTag<?>> build(Map<String, LinTag<?>> builder, @Nullable LinTag<?> prefix) {
        try {
            if (prefix == null || prefix instanceof LinEndTag) {
                return DataResult.success(LinCompoundTag.of(builder));
            }
            if (!(prefix instanceof LinCompoundTag compound)) {
                return DataResult.error(() -> "mergeToMap called with non-map: " + prefix, prefix);
            }
            return DataResult.success(compound.toBuilder().putAll(builder).build());
        } catch (IllegalArgumentException e) {
            return DataResult.error(e::getMessage);
        }
    }
}
