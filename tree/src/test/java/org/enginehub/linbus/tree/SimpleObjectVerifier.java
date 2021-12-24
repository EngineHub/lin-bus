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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Helper for verifying equals and hashCode implementations.
 */
public class SimpleObjectVerifier {
    public static <T> void assertEqualsHashCodeImplementation(T base, T equal, T alsoEqual, T notEqual) {
        // Reflexive
        assertTrue(base.equals(base));
        // Symmetric
        assertTrue(base.equals(equal));
        assertTrue(equal.equals(base));
        assertTrue(!base.equals(notEqual));
        assertTrue(!notEqual.equals(base));
        // Transitive
        assertTrue(base.equals(equal) && equal.equals(alsoEqual) && base.equals(alsoEqual));
        assertTrue(!base.equals(notEqual) && !equal.equals(notEqual) && !alsoEqual.equals(notEqual));
        // Never equal to null
        assertTrue(!base.equals(null));

        // Never equal to a different type
        assertTrue(!base.equals(new Object()));

        // HashCode is consistent with equals
        assertEquals(base.hashCode(), base.hashCode());
        assertEquals(base.hashCode(), equal.hashCode());
        assertEquals(base.hashCode(), alsoEqual.hashCode());
    }
}
