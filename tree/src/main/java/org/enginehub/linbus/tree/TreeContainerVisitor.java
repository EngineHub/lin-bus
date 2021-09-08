package org.enginehub.linbus.tree;

import org.enginehub.linbus.stream.visitor.LinByteArrayTagVisitor;
import org.enginehub.linbus.stream.visitor.LinByteTagVisitor;
import org.enginehub.linbus.stream.visitor.LinCompoundTagVisitor;
import org.enginehub.linbus.stream.visitor.LinContainerVisitor;
import org.enginehub.linbus.stream.visitor.LinDoubleTagVisitor;
import org.enginehub.linbus.stream.visitor.LinFloatTagVisitor;
import org.enginehub.linbus.stream.visitor.LinIntArrayTagVisitor;
import org.enginehub.linbus.stream.visitor.LinIntTagVisitor;
import org.enginehub.linbus.stream.visitor.LinListTagVisitor;
import org.enginehub.linbus.stream.visitor.LinLongArrayTagVisitor;
import org.enginehub.linbus.stream.visitor.LinLongTagVisitor;
import org.enginehub.linbus.stream.visitor.LinShortTagVisitor;
import org.enginehub.linbus.stream.visitor.LinStringTagVisitor;

import java.util.function.Consumer;

abstract class TreeContainerVisitor<K, T extends LinTag<?, T>> extends TreeVisitor<T> implements LinContainerVisitor<K> {

    protected TreeContainerVisitor(Consumer<T> tagConsumer) {
        super(tagConsumer);
    }

    protected abstract void acceptChild(K key, LinTag<?, ?> tag);

    @Override
    public LinByteArrayTagVisitor visitValueByteArray(K key) {
        return new TreeByteArrayVisitor(t -> acceptChild(key, t));
    }

    @Override
    public LinByteTagVisitor visitValueByte(K key) {
        return new TreeByteVisitor(t -> acceptChild(key, t));
    }

    @Override
    public LinCompoundTagVisitor visitValueCompound(K key) {
        return new TreeCompoundVisitor(t -> acceptChild(key, t));
    }

    @Override
    public LinDoubleTagVisitor visitValueDouble(K key) {
        return new TreeDoubleVisitor(t -> acceptChild(key, t));
    }

    @Override
    public LinFloatTagVisitor visitValueFloat(K key) {
        return new TreeFloatVisitor(t -> acceptChild(key, t));
    }

    @Override
    public LinIntArrayTagVisitor visitValueIntArray(K key) {
        return new TreeIntArrayVisitor(t -> acceptChild(key, t));
    }

    @Override
    public LinIntTagVisitor visitValueInt(K key) {
        return new TreeIntVisitor(t -> acceptChild(key, t));
    }

    @Override
    public LinListTagVisitor visitValueList(K key) {
        return new TreeListVisitor(t -> acceptChild(key, t));
    }

    @Override
    public LinLongArrayTagVisitor visitValueLongArray(K key) {
        return new TreeLongArrayVisitor(t -> acceptChild(key, t));
    }

    @Override
    public LinLongTagVisitor visitValueLong(K key) {
        return new TreeLongVisitor(t -> acceptChild(key, t));
    }

    @Override
    public LinShortTagVisitor visitValueShort(K key) {
        return new TreeShortVisitor(t -> acceptChild(key, t));
    }

    @Override
    public LinStringTagVisitor visitValueString(K key) {
        return new TreeStringVisitor(t -> acceptChild(key, t));
    }

}
