package org.enginehub.linbus.stream;

import org.enginehub.linbus.stream.visitor.LinCompoundTagVisitor;
import org.enginehub.linbus.stream.visitor.LinRootVisitor;

import java.io.DataOutput;

public class LinNbtWriter implements LinRootVisitor {
    private final DataOutput dataOutput;

    public LinNbtWriter(DataOutput dataOutput) {
        this.dataOutput = dataOutput;
    }

    @Override
    public LinCompoundTagVisitor visitValue(String name) {
        return null;
    }
}
