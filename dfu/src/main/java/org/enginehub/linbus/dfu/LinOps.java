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

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapLike;
import com.mojang.serialization.RecordBuilder;
import org.enginehub.linbus.tree.LinByteArrayTag;
import org.enginehub.linbus.tree.LinByteTag;
import org.enginehub.linbus.tree.LinCompoundTag;
import org.enginehub.linbus.tree.LinDoubleTag;
import org.enginehub.linbus.tree.LinEndTag;
import org.enginehub.linbus.tree.LinFloatTag;
import org.enginehub.linbus.tree.LinIntArrayTag;
import org.enginehub.linbus.tree.LinIntTag;
import org.enginehub.linbus.tree.LinListTag;
import org.enginehub.linbus.tree.LinLongArrayTag;
import org.enginehub.linbus.tree.LinLongTag;
import org.enginehub.linbus.tree.LinNumberTag;
import org.enginehub.linbus.tree.LinShortTag;
import org.enginehub.linbus.tree.LinStringTag;
import org.enginehub.linbus.tree.LinTag;
import org.enginehub.linbus.tree.LinTagType;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map.Entry;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;

/**
 * A {@link DynamicOps} implementation backed by lin-bus tags, analogous to Minecraft's {@code NbtOps}.
 *
 * <p>
 * The lin-bus tags are stricter than generic NBT: lists are homogeneous and compounds cannot contain an END value.
 * Operations that would produce such a structure either return a failed {@link DataResult} or throw.
 * </p>
 */
public final class LinOps implements DynamicOps<LinTag<?>> {
    private static final LinOps INSTANCE = new LinOps();

    /**
     * The singleton instance of {@link LinOps}.
     */
    public static LinOps getInstance() {
        return INSTANCE;
    }

    private LinOps() {
    }

    @Override
    public LinTag<?> empty() {
        return LinEndTag.instance();
    }

    @Override
    public LinTag<?> emptyList() {
        return LinListTag.empty(LinTagType.endTag());
    }

    @Override
    public LinTag<?> emptyMap() {
        return LinCompoundTag.builder().build();
    }

    @Override
    public <U> U convertTo(DynamicOps<U> outOps, LinTag<?> input) {
        return switch (input) {
            case LinEndTag _ -> outOps.empty();
            case LinByteTag tag -> outOps.createByte(tag.valueAsByte());
            case LinShortTag tag -> outOps.createShort(tag.valueAsShort());
            case LinIntTag tag -> outOps.createInt(tag.valueAsInt());
            case LinLongTag tag -> outOps.createLong(tag.valueAsLong());
            case LinFloatTag tag -> outOps.createFloat(tag.valueAsFloat());
            case LinDoubleTag tag -> outOps.createDouble(tag.valueAsDouble());
            case LinByteArrayTag tag -> outOps.createByteList(tag.view());
            case LinStringTag tag -> outOps.createString(tag.value());
            case LinListTag<?> tag -> convertList(outOps, tag);
            case LinCompoundTag tag -> convertMap(outOps, tag);
            case LinIntArrayTag tag -> outOps.createIntList(Arrays.stream(tag.value()));
            case LinLongArrayTag tag -> outOps.createLongList(Arrays.stream(tag.value()));
        };
    }

    @Override
    public DataResult<Number> getNumberValue(LinTag<?> input) {
        if (input instanceof LinNumberTag<?> tag) {
            Number value = tag.value();
            return DataResult.success(value);
        }
        return DataResult.error(() -> "Not a number");
    }

    @Override
    public LinTag<?> createNumeric(Number i) {
        return LinDoubleTag.of(i.doubleValue());
    }

    @Override
    public LinTag<?> createByte(byte value) {
        return LinByteTag.of(value);
    }

    @Override
    public LinTag<?> createShort(short value) {
        return LinShortTag.of(value);
    }

    @Override
    public LinTag<?> createInt(int value) {
        return LinIntTag.of(value);
    }

    @Override
    public LinTag<?> createLong(long value) {
        return LinLongTag.of(value);
    }

    @Override
    public LinTag<?> createFloat(float value) {
        return LinFloatTag.of(value);
    }

    @Override
    public LinTag<?> createDouble(double value) {
        return LinDoubleTag.of(value);
    }

    @Override
    public DataResult<Boolean> getBooleanValue(LinTag<?> input) {
        return getNumberValue(input).map(value -> value.doubleValue() != 0.0);
    }

    @Override
    public LinTag<?> createBoolean(boolean value) {
        return LinByteTag.of((byte) (value ? 1 : 0));
    }

    @Override
    public DataResult<String> getStringValue(LinTag<?> input) {
        if (input instanceof LinStringTag tag) {
            return DataResult.success(tag.value());
        }
        return DataResult.error(() -> "Not a string");
    }

    @Override
    public LinTag<?> createString(String value) {
        return LinStringTag.of(value);
    }

    @Override
    public DataResult<LinTag<?>> mergeToList(LinTag<?> list, LinTag<?> value) {
        return mergeToList(list, List.of(value));
    }

    @Override
    public DataResult<LinTag<?>> mergeToList(LinTag<?> list, List<LinTag<?>> values) {
        return switch (list) {
            case LinListTag<?> existing -> mergeList(existing.value(), values);
            case LinByteArrayTag array when array.view().hasRemaining() -> mergeBytes(array, values);
            case LinIntArrayTag array when array.view().hasRemaining() -> mergeInts(array, values);
            case LinLongArrayTag array when array.view().hasRemaining() -> mergeLongs(array, values);
            case LinByteArrayTag _, LinIntArrayTag _, LinLongArrayTag _, LinEndTag _ -> mergeList(List.of(), values);
            default -> DataResult.error(() -> "mergeToList called with non-list: " + list, list);
        };
    }

    private static DataResult<LinTag<?>> mergeList(List<? extends LinTag<?>> prefix, List<LinTag<?>> values) {
        if (prefix.isEmpty() && values.isEmpty()) {
            return DataResult.success(LinListTag.empty(LinTagType.endTag()));
        }
        LinTagType<? extends LinTag<?>> elementType =
            prefix.isEmpty() ? values.getFirst().type() : prefix.getFirst().type();
        return mergeListTyped(elementType, prefix, values);
    }

    @SuppressWarnings("unchecked")
    private static <T extends LinTag<?>> DataResult<LinTag<?>> mergeListTyped(
        LinTagType<T> elementType, List<? extends LinTag<?>> prefix, List<LinTag<?>> values
    ) {
        LinListTag.Builder<T> builder = LinListTag.builderWithExpectedSize(
            elementType, prefix.size() + values.size()
        );
        try {
            /*
             * addAll checks every element against elementType and throws on a mismatch, so the
             * casts to the element type are sound.
             */
            builder.addAll((List<? extends T>) prefix);
            builder.addAll((List<? extends T>) values);
            return DataResult.success(builder.build());
        } catch (IllegalArgumentException e) {
            return DataResult.error(e::getMessage);
        }
    }

    // valhalla wen
    private static DataResult<LinTag<?>> mergeBytes(LinByteArrayTag array, List<LinTag<?>> values) {
        ByteBuffer view = array.view();
        int prefix = view.remaining();
        byte[] result = new byte[prefix + values.size()];
        view.get(result, 0, prefix);
        for (int i = 0; i < values.size(); i++) {
            if (!(values.get(i) instanceof LinByteTag tag)) {
                return typeError(LinTagType.byteTag(), values.get(i));
            }
            result[prefix + i] = tag.valueAsByte();
        }
        return DataResult.success(LinByteArrayTag.of(result));
    }

    private static DataResult<LinTag<?>> mergeInts(LinIntArrayTag array, List<LinTag<?>> values) {
        IntBuffer view = array.view();
        int prefix = view.remaining();
        int[] result = new int[prefix + values.size()];
        view.get(result, 0, prefix);
        for (int i = 0; i < values.size(); i++) {
            if (!(values.get(i) instanceof LinIntTag tag)) {
                return typeError(LinTagType.intTag(), values.get(i));
            }
            result[prefix + i] = tag.valueAsInt();
        }
        return DataResult.success(LinIntArrayTag.of(result));
    }

    private static DataResult<LinTag<?>> mergeLongs(LinLongArrayTag array, List<LinTag<?>> values) {
        LongBuffer view = array.view();
        int prefix = view.remaining();
        long[] result = new long[prefix + values.size()];
        view.get(result, 0, prefix);
        for (int i = 0; i < values.size(); i++) {
            if (!(values.get(i) instanceof LinLongTag tag)) {
                return typeError(LinTagType.longTag(), values.get(i));
            }
            result[prefix + i] = tag.valueAsLong();
        }
        return DataResult.success(LinLongArrayTag.of(result));
    }

    private static DataResult<LinTag<?>> typeError(LinTagType<?> expected, LinTag<?> actual) {
        return DataResult.error(
            () -> "Element is not of type " + expected.name() + " but " + actual.type().name()
        );
    }

    @Override
    public DataResult<LinTag<?>> mergeToMap(LinTag<?> map, LinTag<?> key, LinTag<?> value) {
        if (!(map instanceof LinCompoundTag) && !(map instanceof LinEndTag)) {
            return DataResult.error(() -> "mergeToMap called with non-map: " + map, map);
        }
        if (!(key instanceof LinStringTag stringKey)) {
            return DataResult.error(() -> "key is not a string: " + key, map);
        }
        LinCompoundTag.Builder output = builderFrom(map);
        try {
            output.put(stringKey.value(), value);
        } catch (IllegalArgumentException e) {
            return DataResult.error(e::getMessage);
        }
        return DataResult.success(output.build());
    }

    @Override
    public DataResult<LinTag<?>> mergeToMap(LinTag<?> map, MapLike<LinTag<?>> values) {
        if (!(map instanceof LinCompoundTag) && !(map instanceof LinEndTag)) {
            return DataResult.error(() -> "mergeToMap called with non-map: " + map, map);
        }
        LinCompoundTag.Builder output = builderFrom(map);
        List<LinTag<?>> missed = new ArrayList<>();
        try {
            values.entries().forEach(entry -> {
                LinTag<?> key = entry.getFirst();
                if (key instanceof LinStringTag stringKey) {
                    output.put(stringKey.value(), entry.getSecond());
                } else {
                    missed.add(key);
                }
            });
        } catch (IllegalArgumentException e) {
            return DataResult.error(e::getMessage);
        }
        if (!missed.isEmpty()) {
            return DataResult.error(() -> "some keys are not strings: " + missed, output.build());
        }
        return DataResult.success(output.build());
    }

    @Override
    public DataResult<Stream<Pair<LinTag<?>, LinTag<?>>>> getMapValues(LinTag<?> input) {
        if (input instanceof LinCompoundTag tag) {
            return DataResult.success(
                tag.value().entrySet().stream().map(entry ->
                    Pair.of(createString(entry.getKey()), entry.getValue())
                )
            );
        }
        return DataResult.error(() -> "Not a map: " + input);
    }

    @Override
    public DataResult<Consumer<BiConsumer<LinTag<?>, LinTag<?>>>> getMapEntries(LinTag<?> input) {
        if (input instanceof LinCompoundTag tag) {
            return DataResult.success(consumer -> {
                for (Entry<String, LinTag<?>> entry : tag.value().entrySet()) {
                    consumer.accept(createString(entry.getKey()), entry.getValue());
                }
            });
        }
        return DataResult.error(() -> "Not a map: " + input);
    }

    @Override
    public DataResult<MapLike<LinTag<?>>> getMap(LinTag<?> input) {
        if (input instanceof LinCompoundTag tag) {
            return DataResult.success(new LinCompoundTagMapLike(tag));
        }
        return DataResult.error(() -> "Not a map: " + input);
    }

    @Override
    public LinTag<?> createMap(Stream<Pair<LinTag<?>, LinTag<?>>> map) {
        LinCompoundTag.Builder builder = LinCompoundTag.builder();
        map.forEach(entry -> {
            LinTag<?> key = entry.getFirst();
            if (key instanceof LinStringTag stringKey) {
                builder.put(stringKey.value(), entry.getSecond());
            } else {
                throw new UnsupportedOperationException("Cannot create map with non-string key: " + key);
            }
        });
        return builder.build();
    }

    @Override
    public DataResult<Stream<LinTag<?>>> getStream(LinTag<?> input) {
        return switch (input) {
            case LinListTag<?> tag -> DataResult.success(tag.value().stream().map(element -> (LinTag<?>) element));
            case LinByteArrayTag tag -> {
                ByteBuffer values = tag.view();
                yield DataResult.success(
                    IntStream.range(0, values.limit()).mapToObj(i -> LinByteTag.of(values.get(i)))
                );
            }
            case LinIntArrayTag tag ->
                DataResult.success(Arrays.stream(tag.value()).mapToObj(value -> (LinTag<?>) LinIntTag.of(value)));
            case LinLongArrayTag tag ->
                DataResult.success(Arrays.stream(tag.value()).mapToObj(value -> (LinTag<?>) LinLongTag.of(value)));
            default -> DataResult.error(() -> "Not a list");
        };
    }

    @Override
    public DataResult<ByteBuffer> getByteBuffer(LinTag<?> input) {
        if (input instanceof LinByteArrayTag tag) {
            return DataResult.success(tag.view());
        }
        return DynamicOps.super.getByteBuffer(input);
    }

    @Override
    public LinTag<?> createByteList(ByteBuffer input) {
        ByteBuffer wholeBuffer = input.duplicate().clear();
        byte[] bytes = new byte[input.capacity()];
        wholeBuffer.get(0, bytes, 0, bytes.length);
        return LinByteArrayTag.of(bytes);
    }

    @Override
    public DataResult<IntStream> getIntStream(LinTag<?> input) {
        if (input instanceof LinIntArrayTag tag) {
            return DataResult.success(Arrays.stream(tag.value()));
        }
        return DynamicOps.super.getIntStream(input);
    }

    @Override
    public LinTag<?> createIntList(IntStream input) {
        return LinIntArrayTag.of(input.toArray());
    }

    @Override
    public DataResult<LongStream> getLongStream(LinTag<?> input) {
        if (input instanceof LinLongArrayTag tag) {
            return DataResult.success(Arrays.stream(tag.value()));
        }
        return DynamicOps.super.getLongStream(input);
    }

    @Override
    public LinTag<?> createLongList(LongStream input) {
        return LinLongArrayTag.of(input.toArray());
    }

    @Override
    public LinTag<?> createList(Stream<LinTag<?>> input) {
        List<LinTag<?>> elements = input.toList();
        if (elements.isEmpty()) {
            return LinListTag.empty(LinTagType.endTag());
        }
        return createListTagTyped(elements.getFirst().type(), elements);
    }

    @SuppressWarnings("unchecked")
    private static <T extends LinTag<?>> LinListTag<T> createListTagTyped(
        LinTagType<T> elementType, List<LinTag<?>> elements
    ) {
        /*
         * LinListTag.of checks every element against elementType, which is the runtime type
         * of the first element, so casting the homogeneous list to List<T> is sound.
         */
        return LinListTag.of(elementType, (List<T>) elements);
    }

    @Override
    public LinTag<?> remove(LinTag<?> input, String key) {
        if (input instanceof LinCompoundTag tag) {
            return tag.toBuilder().remove(key).build();
        }
        return input;
    }

    @Override
    public RecordBuilder<LinTag<?>> mapBuilder() {
        return new LinRecordBuilder(this);
    }

    @Override
    public String toString() {
        return "lin-bus";
    }

    private static LinCompoundTag.Builder builderFrom(LinTag<?> map) {
        return map instanceof LinCompoundTag compound ? compound.toBuilder() : LinCompoundTag.builder();
    }

}
