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

package org.enginehub.linbus.format.snbt.util;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.CharBuffer;

/**
 * Reader that logs all read operations. This class is kept for debugging purposes.
 */
public class TracedReader extends Reader {
    private static final StackWalker WALKER = StackWalker.getInstance();
    private final Reader delegate;

    public TracedReader(Reader delegate) {
        this.delegate = delegate;
    }

    private void printRelevantStack() {
        var frames = WALKER.walk(s ->
            s.dropWhile(f -> !isRelevantStackFrame(f))
                .takeWhile(this::isRelevantStackFrame)
                .toList()
        );
        for (var frame : frames) {
            System.err.println("  " + frame);
        }
    }

    private boolean isRelevantStackFrame(StackWalker.StackFrame frame) {
        if (frame.getClassName().equals(getClass().getName())) {
            return false;
        }
        return frame.getClassName().startsWith("org.enginehub.linbus");
    }

    @Override
    public int read(@NotNull CharBuffer target) throws IOException {
        int result = delegate.read(target);
        System.err.println("read(" + target + "): " + result);
        printRelevantStack();
        return result;
    }

    @Override
    public int read() throws IOException {
        int result = delegate.read();
        if (result != -1) {
            System.err.println("read(): " + (char) result);
        } else {
            System.err.println("read(): " + result);
        }
        printRelevantStack();
        return result;
    }

    @Override
    public int read(char @NotNull [] cbuf) throws IOException {
        int result = delegate.read(cbuf);
        System.err.println("read(char[].length = " + cbuf.length + "): " + result);
        printRelevantStack();
        return result;
    }

    @Override
    public int read(char @NotNull [] cbuf, int off, int len) throws IOException {
        int result = delegate.read(cbuf, off, len);
        System.err.println("read(char[].length = " + cbuf.length + ", off = " + off + ", len = " + len + "): " + result);
        printRelevantStack();
        return result;
    }

    @Override
    public long skip(long n) throws IOException {
        long result = delegate.skip(n);
        System.err.println("skip(" + n + "): " + result);
        printRelevantStack();
        return result;
    }

    @Override
    public boolean ready() throws IOException {
        boolean result = delegate.ready();
        System.err.println("ready(): " + result);
        printRelevantStack();
        return result;
    }

    @Override
    public boolean markSupported() {
        boolean result = delegate.markSupported();
        System.err.println("markSupported(): " + result);
        printRelevantStack();
        return result;
    }

    @Override
    public void mark(int readAheadLimit) throws IOException {
        delegate.mark(readAheadLimit);
        System.err.println("mark(" + readAheadLimit + ")");
        printRelevantStack();
    }

    @Override
    public void reset() throws IOException {
        delegate.reset();
        System.err.println("reset()");
        printRelevantStack();
    }

    @Override
    public void close() throws IOException {
        delegate.close();
        System.err.println("close()");
        printRelevantStack();
    }

    @Override
    public long transferTo(Writer out) throws IOException {
        long result = delegate.transferTo(out);
        System.err.println("transferTo(" + out + "): " + result);
        printRelevantStack();
        return result;
    }
}
