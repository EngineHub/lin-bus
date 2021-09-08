package org.enginehub.linbus.tree;

import org.enginehub.linbus.stream.visitor.LinStringTagVisitor;

import java.util.function.Consumer;

class TreeStringVisitor extends TreeVisitor<LinStringTag> implements LinStringTagVisitor {
    protected TreeStringVisitor(Consumer<LinStringTag> tagConsumer) {
        super(tagConsumer);
    }

    @Override
    public void visitString(String value) {
        tagFinished(new LinStringTag(value));
    }
}
