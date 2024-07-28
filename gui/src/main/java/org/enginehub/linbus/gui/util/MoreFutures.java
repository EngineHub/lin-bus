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
