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

import com.google.common.truth.FailureMetadata;
import com.google.common.truth.IntStreamSubject;
import com.google.common.truth.LongStreamSubject;
import com.google.common.truth.StreamSubject;
import com.google.common.truth.StringSubject;
import com.google.common.truth.Subject;
import com.mojang.serialization.DataResult;
import org.jspecify.annotations.Nullable;

import java.util.Optional;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;

import static com.google.common.truth.Fact.fact;
import static com.google.common.truth.Fact.simpleFact;
import static com.google.common.truth.Truth.assertAbout;

final class DataResultSubject<T> extends Subject {

    static <T> Subject.Factory<DataResultSubject<T>, DataResult<T>> dataResults() {
        return DataResultSubject::new;
    }

    static <T> DataResultSubject<T> assertThat(@Nullable DataResult<T> actual) {
        return assertAbout(DataResultSubject.<T>dataResults()).that(actual);
    }

    private final @Nullable DataResult<T> actual;

    private DataResultSubject(FailureMetadata metadata, @Nullable DataResult<T> actual) {
        super(metadata, actual);
        this.actual = actual;
    }

    void isError() {
        isNotNull();
        assert actual != null;
        if (actual.error().isEmpty()) {
            failWithActual(simpleFact("expected an error result"));
        }
    }

    StringSubject hasErrorWithMessageThat() {
        isError();
        assert actual != null;
        return check("error().message()").that(actual.error().orElseThrow().message());
    }

    Subject hasResultThat() {
        return check("result()").that(resultOrFail());
    }

    StreamSubject hasStreamResultThat() {
        return check("result()").that((Stream<?>) resultOrFail());
    }

    IntStreamSubject hasIntStreamResultThat() {
        return check("result()").that((IntStream) resultOrFail());
    }

    LongStreamSubject hasLongStreamResultThat() {
        return check("result()").that((LongStream) resultOrFail());
    }

    T resultOrFail() {
        isNotNull();
        assert actual != null;
        Optional<T> result = actual.result();
        if (result.isEmpty()) {
            failWithoutActual(
                simpleFact("expected a success result"),
                fact("but had error", actual.error().map(DataResult.Error::message).orElse("(none)"))
            );
            throw new AssertionError("unreachable");
        }
        return result.get();
    }
}
