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
import org.jspecify.annotations.Nullable;

import static com.google.common.truth.Truth.assertAbout;

public class LinTagSubject extends Subject {
    public static Factory<LinTagSubject, LinTag<?>> linTags() {
        return LinTagSubject::new;
    }

    public static LinTagSubject assertThat(@Nullable LinTag<?> tag) {
        return assertAbout(linTags()).that(tag);
    }

    private final @Nullable LinTag<?> tag;

    protected LinTagSubject(FailureMetadata metadata, @Nullable LinTag<?> actual) {
        super(metadata, actual);
        this.tag = actual;
    }

    public void isOfType(LinTagType<?> type) {
        isNotNull();
        assert tag != null : "checked by isNotNull()";
        if (this.tag.type() != type) {
            failWithActual(
                Fact.fact("expected", type),
                Fact.fact("but was", this.tag.type())
            );
        }
    }

    public PrimitiveByteArraySubject byteArrayValue() {
        isOfType(LinTagType.byteArrayTag());
        assert tag != null : "checked by isOfType()";
        return check("byteArrayValue()").that(((LinByteArrayTag) tag).value());
    }

    public ComparableSubject<Byte> byteValue() {
        isOfType(LinTagType.byteTag());
        assert tag != null : "checked by isOfType()";
        return check("byteValue()").that(((LinByteTag) tag).value());
    }

    public MapSubject compoundValue() {
        isOfType(LinTagType.compoundTag());
        assert tag != null : "checked by isOfType()";
        return check("compoundValue()").that(((LinCompoundTag) tag).value());
    }

    public DoubleSubject doubleValue() {
        isOfType(LinTagType.doubleTag());
        assert tag != null : "checked by isOfType()";
        return check("doubleValue()").that(((LinDoubleTag) tag).value());
    }

    public Subject endValue() {
        isOfType(LinTagType.endTag());
        assert tag != null : "checked by isOfType()";
        return check("endValue()").that(((LinEndTag) tag).value());
    }

    public FloatSubject floatValue() {
        isOfType(LinTagType.floatTag());
        assert tag != null : "checked by isOfType()";
        return check("floatValue()").that(((LinFloatTag) tag).value());
    }

    public PrimitiveIntArraySubject intArrayValue() {
        isOfType(LinTagType.intArrayTag());
        assert tag != null : "checked by isOfType()";
        return check("intArrayValue()").that(((LinIntArrayTag) tag).value());
    }

    public IntegerSubject intValue() {
        isOfType(LinTagType.intTag());
        assert tag != null : "checked by isOfType()";
        return check("intValue()").that(((LinIntTag) tag).value());
    }

    public IterableSubject listValue() {
        isOfType(LinTagType.listTag());
        assert tag != null : "checked by isOfType()";
        return check("listValue()").that(((LinListTag<?>) tag).value());
    }

    public PrimitiveLongArraySubject longArrayValue() {
        isOfType(LinTagType.longArrayTag());
        assert tag != null : "checked by isOfType()";
        return check("longArrayValue()").that(((LinLongArrayTag) tag).value());
    }

    public LongSubject longValue() {
        isOfType(LinTagType.longTag());
        assert tag != null : "checked by isOfType()";
        return check("longValue()").that(((LinLongTag) tag).value());
    }

    public ComparableSubject<Short> shortValue() {
        isOfType(LinTagType.shortTag());
        assert tag != null : "checked by isOfType()";
        return check("shortValue()").that(((LinShortTag) tag).value());
    }

    public StringSubject stringValue() {
        isOfType(LinTagType.stringTag());
        assert tag != null : "checked by isOfType()";
        return check("stringValue()").that(((LinStringTag) tag).value());
    }

    public LinTagSubject getTagByKey(String key) {
        isOfType(LinTagType.compoundTag());
        assert tag != null : "checked by isOfType()";
        return check("[%s]", key)
            .about(linTags())
            .that(((LinCompoundTag) tag).value().get(key));
    }

    public LinTagSubject getTagByIndex(int index) {
        isOfType(LinTagType.listTag());
        assert tag != null : "checked by isOfType()";
        return check("[%s]", index)
            .about(linTags())
            .that(((LinListTag<?>) tag).value().get(index));
    }
}
