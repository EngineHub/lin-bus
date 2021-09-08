package org.enginehub.linbus.tree;

import org.enginehub.linbus.stream.visitor.LinLongTagVisitor;

import java.util.function.Consumer;

class TreeLongVisitor extends TreeVisitor<LinLongTag> implements LinLongTagVisitor {
    protected TreeLongVisitor(Consumer<LinLongTag> tagConsumer) {
        super(tagConsumer);
    }

    @Override
    public void visitLong(long value) {
        tagFinished(new LinLongTag(value));
    }
}
