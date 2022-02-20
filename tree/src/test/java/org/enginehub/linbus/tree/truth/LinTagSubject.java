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

package org.enginehub.linbus.tree.truth;

import com.google.common.truth.ComparableSubject;
import com.google.common.truth.DoubleSubject;
import com.google.common.truth.Fact;
import com.google.common.truth.FailureMetadata;
import com.google.common.truth.FloatSubject;
import com.google.common.truth.IntegerSubject;
import com.google.common.truth.IterableSubject;
import com.google.common.truth.LongSubject;
import com.google.common.truth.MapSubject;
import com.google.common.truth.PrimitiveByteArraySubject;
import com.google.common.truth.PrimitiveIntArraySubject;
import com.google.common.truth.PrimitiveLongArraySubject;
import com.google.common.truth.StringSubject;
import com.google.common.truth.Subject;
import org.checkerframework.checker.nullness.qual.Nullable;
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
import org.enginehub.linbus.tree.LinShortTag;
import org.enginehub.linbus.tree.LinStringTag;
import org.enginehub.linbus.tree.LinTag;
import org.enginehub.linbus.tree.LinTagType;

import static com.google.common.truth.Truth.assertAbout;

public class LinTagSubject extends Subject {
    public static Factory<LinTagSubject, LinTag<?>> linTags() {
        return LinTagSubject::new;
    }

    public static LinTagSubject assertThat(@Nullable LinTag<?> tag) {
        return assertAbout(linTags()).that(tag);
    }

    private final LinTag<?> tag;

    protected LinTagSubject(FailureMetadata metadata, @Nullable LinTag<?> actual) {
        super(metadata, actual);
        this.tag = actual;
    }

    public void isOfType(LinTagType<?> type) {
        isNotNull();
        if (this.tag.type() != type) {
            failWithActual(
                Fact.fact("expected", type),
                Fact.fact("but was", this.tag.type())
            );
        }
    }

    public PrimitiveByteArraySubject byteArrayValue() {
        isOfType(LinTagType.byteArrayTag());
        return check("byteArrayValue()").that(((LinByteArrayTag) tag).value());
    }

    public ComparableSubject<Byte> byteValue() {
        isOfType(LinTagType.byteTag());
        return check("byteValue()").that(((LinByteTag) tag).value());
    }

    public MapSubject compoundValue() {
        isOfType(LinTagType.compoundTag());
        return check("compoundValue()").that(((LinCompoundTag) tag).value());
    }

    public DoubleSubject doubleValue() {
        isOfType(LinTagType.doubleTag());
        return check("doubleValue()").that(((LinDoubleTag) tag).value());
    }

    public Subject endValue() {
        isOfType(LinTagType.endTag());
        return check("endValue()").that(((LinEndTag) tag).value());
    }

    public FloatSubject floatValue() {
        isOfType(LinTagType.floatTag());
        return check("floatValue()").that(((LinFloatTag) tag).value());
    }

    public PrimitiveIntArraySubject intArrayValue() {
        isOfType(LinTagType.intArrayTag());
        return check("intArrayValue()").that(((LinIntArrayTag) tag).value());
    }

    public IntegerSubject intValue() {
        isOfType(LinTagType.intTag());
        return check("intValue()").that(((LinIntTag) tag).value());
    }

    public IterableSubject listValue() {
        isOfType(LinTagType.listTag());
        return check("listValue()").that(((LinListTag<?>) tag).value());
    }

    public PrimitiveLongArraySubject longArrayValue() {
        isOfType(LinTagType.longArrayTag());
        return check("longArrayValue()").that(((LinLongArrayTag) tag).value());
    }

    public LongSubject longValue() {
        isOfType(LinTagType.longTag());
        return check("longValue()").that(((LinLongTag) tag).value());
    }

    public ComparableSubject<Short> shortValue() {
        isOfType(LinTagType.shortTag());
        return check("shortValue()").that(((LinShortTag) tag).value());
    }

    public StringSubject stringValue() {
        isOfType(LinTagType.stringTag());
        return check("stringValue()").that(((LinStringTag) tag).value());
    }

    public LinTagSubject getTagByKey(String key) {
        isOfType(LinTagType.compoundTag());
        return check("[%s]", key)
            .about(linTags())
            .that(((LinCompoundTag) tag).value().get(key));
    }

    public LinTagSubject getTagByIndex(int index) {
        isOfType(LinTagType.listTag());
        return check("[%s]", index)
            .about(linTags())
            .that(((LinListTag<?>) tag).value().get(index));
    }
}
