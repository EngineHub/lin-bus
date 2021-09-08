package org.enginehub.linbus.tree;

import org.enginehub.linbus.stream.visitor.LinIntTagVisitor;

import java.util.function.Consumer;

class TreeIntVisitor extends TreeVisitor<LinIntTag> implements LinIntTagVisitor {
    protected TreeIntVisitor(Consumer<LinIntTag> tagConsumer) {
        super(tagConsumer);
    }

    @Override
    public void visitInt(int value) {
        tagFinished(new LinIntTag(value));
    }
}
