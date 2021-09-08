package org.enginehub.linbus.tree;

import java.util.function.Consumer;

abstract class TreeVisitor<T extends LinTag<?, T>> {
    private final Consumer<T> tagConsumer;

    protected TreeVisitor(Consumer<T> tagConsumer) {
        this.tagConsumer = tagConsumer;
    }

    protected final void tagFinished(T tag) {
        tagConsumer.accept(tag);
    }
}
