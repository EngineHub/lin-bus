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

package org.enginehub.linbus.gui;

public enum OS {
    WINDOWS,
    MAC_OS,
    LINUX,
    ;

    private static final OS detected;

    static {
        String osName = System.getProperty("os.name");
        if (osName.startsWith("Windows")) {
            detected = WINDOWS;
        } else if (osName.startsWith("Linux") || osName.startsWith("FreeBSD") || osName.startsWith("SunOS") || osName.startsWith("Unix")) {
            detected = LINUX;
        } else if (osName.startsWith("Mac OS X") || osName.startsWith("Darwin")) {
            detected = MAC_OS;
        } else {
            throw new AssertionError("Unknown platform: " + osName);
        }
    }

    public static OS detected() {
        return detected;
    }
}
