/*
 * Copyright (c) EngineHub <https://enginehub.org>
 * Copyright (c) contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package org.enginehub.linbus.tree;

import org.enginehub.linbus.stream.visitor.LinCompoundTagVisitor;

import java.util.function.Consumer;

class TreeCompoundVisitor extends TreeContainerVisitor<String, LinCompoundTag> implements LinCompoundTagVisitor {
    private final LinCompoundTag.Builder builder = LinCompoundTag.builder();

    protected TreeCompoundVisitor(Consumer<LinCompoundTag> tagConsumer) {
        super(tagConsumer);
    }

    protected void acceptChild(String name, LinTag<?, ?> tag) {
        builder.put(name, tag);
    }

    @Override
    public void visitEnd() {
        tagFinished(builder.build());
    }
}
