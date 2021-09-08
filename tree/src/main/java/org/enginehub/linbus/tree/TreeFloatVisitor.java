package org.enginehub.linbus.tree;

import org.enginehub.linbus.stream.visitor.LinFloatTagVisitor;

import java.util.function.Consumer;

class TreeFloatVisitor extends TreeVisitor<LinFloatTag> implements LinFloatTagVisitor {
    protected TreeFloatVisitor(Consumer<LinFloatTag> tagConsumer) {
        super(tagConsumer);
    }

    @Override
    public void visitFloat(float value) {
        tagFinished(new LinFloatTag(value));
    }
}
