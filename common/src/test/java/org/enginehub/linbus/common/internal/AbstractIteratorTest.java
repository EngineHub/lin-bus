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

package org.enginehub.linbus.common.internal;

import org.junit.jupiter.api.Test;

import java.util.NoSuchElementException;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class AbstractIteratorTest {
    @Test
    void simpleTest() {
        var shouldEnd = new AtomicBoolean();
        var iterator = new AbstractIterator<>() {
            @Override
            protected Object computeNext() {
                if (shouldEnd.getPlain()) {
                    return end();
                }
                return "A simple test!";
            }
        };

        assertTrue(iterator.hasNext());
        assertEquals("A simple test!", iterator.next());
        // We don't need to call hasNext to get the next value
        assertEquals("A simple test!", iterator.next());
        // Prep the next item before ending...
        assertTrue(iterator.hasNext());
        shouldEnd.set(true);
        assertEquals("A simple test!", iterator.next());
        // And now it is done.
        assertFalse(iterator.hasNext());
        assertThrows(NoSuchElementException.class, iterator::next);
    }
}
