package org.enginehub.linbus.tree;

import org.enginehub.linbus.common.LinTagId;
import org.enginehub.linbus.stream.visitor.LinListTagVisitor;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

class TreeListVisitor extends TreeContainerVisitor<Integer, LinListTag<@NotNull LinTag<?, ?>>> implements LinListTagVisitor {
    private LinListTag.Builder<@NotNull LinTag<?, ?>> builder;

    protected TreeListVisitor(Consumer<LinListTag<@NotNull LinTag<?, ?>>> tagConsumer) {
        super(tagConsumer);
    }

    @Override
    protected void acceptChild(Integer key, LinTag<?, ?> tag) {
        builder.add(tag);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void visitSizeAndType(int size, LinTagId type) {
        builder = LinListTag.builder((LinTagType<LinTag<?, ?>>) LinTagType.fromId(type));
    }

    @Override
    public void visitEnd() {
        tagFinished(builder.build());
    }
}
