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

package org.enginehub.linbus.format.snbt;

import org.enginehub.linbus.format.snbt.impl.LinSnbtReader;
import org.enginehub.linbus.format.snbt.impl.LinSnbtWriter;
import org.enginehub.linbus.stream.token.LinToken;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.UncheckedIOException;
import java.util.Iterator;
import java.util.function.Function;

/**
 * Reads and writes NBT streams.
 */
public class LinSnbtStreams {
    /**
     * Read a stream of NBT tokens from a {@link Reader}.
     *
     * <p>
     * If you wish to use the input afterwards, you must provide a reader that {@linkplain Reader#markSupported()
     * supports marking}, otherwise the input may be read further than expected.
     * </p>
     *
     * <p>
     * The input will not be closed by this method. The caller is responsible for managing the lifetime of the input.
     * </p>
     *
     * @param input the input to read from
     * @return the stream of NBT tokens
     */
    public static Iterator<? extends @NotNull LinToken> read(@NotNull Reader input) {
        return new LinSnbtReader(input);
    }

    /**
     * Read a stream of NBT tokens from a string.
     *
     * @param input the input to read from
     * @return the stream of NBT tokens
     */
    public static Iterator<? extends @NotNull LinToken> readFromString(@NotNull String input) {
        return read(new StringReader(input));
    }

    /**
     * Read a stream of NBT tokens from a {@link Reader}.
     *
     * <p>
     * If you wish to use the input afterwards, you must provide a reader that {@linkplain Reader#markSupported()
     * supports marking}, otherwise the input may be read further than expected.
     * </p>
     *
     * <p>
     * The input will not be closed by this method. The caller is responsible for managing the lifetime of the input.
     * </p>
     *
     * @param input the input to read from
     * @return the stream of NBT tokens
     * @throws IOException if an I/O error occurs ({@link UncheckedIOException} is unwrapped)
     */
    public static <R> R readUsing(@NotNull Reader input, @NotNull Function<? super @NotNull Iterator<? extends @NotNull LinToken>, R> transform) throws IOException {
        try {
            return transform.apply(read(input));
        } catch (UncheckedIOException e) {
            throw e.getCause();
        }
    }

    /**
     * Read a stream of NBT tokens from a string.
     *
     * @param input the input to read from
     * @return the stream of NBT tokens
     */
    public static <R> R readFromStringUsing(@NotNull String input, @NotNull Function<? super @NotNull Iterator<? extends @NotNull LinToken>, R> transform) {
        return transform.apply(readFromString(input));
    }

    /**
     * Write a stream of NBT tokens to a {@link Appendable}.
     *
     * <p>
     * The output will not be closed by this method. The caller is responsible for managing the lifetime of the output.
     * </p>
     *
     * @param output the output to write to
     * @param tokens the stream of NBT tokens
     * @throws IOException if an I/O error occurs
     */
    public static void write(@NotNull Appendable output, @NotNull Iterator<? extends @NotNull LinToken> tokens) throws IOException {
        new LinSnbtWriter().write(output, tokens);
    }

    /**
     * Write a stream of NBT tokens to a string.
     *
     * <p>
     * The output will not be closed by this method. The caller is responsible for managing the lifetime of the output.
     * </p>
     *
     * @param tokens the stream of NBT tokens
     */
    public static String writeToString(@NotNull Iterator<? extends @NotNull LinToken> tokens) {
        var builder = new StringBuilder();
        try {
            write(builder, tokens);
        } catch (IOException e) {
            throw new AssertionError("No I/O to perform, so shouldn't throw an I/O exception", e);
        }
        return builder.toString();
    }
}
