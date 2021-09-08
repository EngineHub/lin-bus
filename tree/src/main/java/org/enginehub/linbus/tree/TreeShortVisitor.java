package org.enginehub.linbus.tree;

import org.enginehub.linbus.stream.visitor.LinShortTagVisitor;

import java.util.function.Consumer;

class TreeShortVisitor extends TreeVisitor<LinShortTag> implements LinShortTagVisitor {
    protected TreeShortVisitor(Consumer<LinShortTag> tagConsumer) {
        super(tagConsumer);
    }

    @Override
    public void visitShort(short value) {
        tagFinished(new LinShortTag(value));
    }
}
