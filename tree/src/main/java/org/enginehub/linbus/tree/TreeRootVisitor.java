package org.enginehub.linbus.tree;

import org.enginehub.linbus.stream.visitor.LinCompoundTagVisitor;
import org.enginehub.linbus.stream.visitor.LinRootVisitor;

class TreeRootVisitor implements LinRootVisitor {
    private LinRootEntry result;

    @Override
    public LinCompoundTagVisitor visitValue(String name) {
        return new TreeCompoundVisitor(t -> result = new LinRootEntry(name, t));
    }

    public LinRootEntry result() {
        return result;
    }
}
