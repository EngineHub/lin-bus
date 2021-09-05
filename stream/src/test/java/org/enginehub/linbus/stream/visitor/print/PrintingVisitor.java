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

package org.enginehub.linbus.stream.visitor.print;

import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class PrintingVisitor {
    private static final StackWalker WALKER = StackWalker.getInstance();

    private final String context;

    protected PrintingVisitor(String context) {
        this.context = context;
    }

    protected final String nest(String child) {
        return context + "." + child;
    }

    protected final void print(Object... content) {
        System.err.println(
            Stream.of(content)
                .map(String::valueOf)
                .collect(Collectors.joining(", ", getPrefix() + "(", ")"))
        );
    }

    private String getPrefix() {
        var callingMethod = WALKER.walk(s -> s.skip(2).findFirst().orElseThrow());
        return context + "#" + callingMethod.getMethodName();
    }

    @Override
    public String toString() {
        return "PrintingVisitor{" + context + "}";
    }
}
