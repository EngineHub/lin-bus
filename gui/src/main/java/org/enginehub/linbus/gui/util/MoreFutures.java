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

package org.enginehub.linbus.gui.util;

import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class MoreFutures {
    public static <T>CompletableFuture<T> create(BiConsumer<Consumer<T>, Consumer<? extends Throwable>> body) {
        var future = new CompletableFuture<T>();
        try {
            body.accept(future::complete, future::completeExceptionally);
        } catch (Exception t) { // Not catching Throwable since Error should just propagate immediately
            future.completeExceptionally(t);
        }
        return future;
    }

    private MoreFutures() {
    }
}
