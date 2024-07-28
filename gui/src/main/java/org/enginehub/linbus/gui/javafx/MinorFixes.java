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

package org.enginehub.linbus.gui.javafx;

import javafx.scene.control.Tooltip;
import javafx.util.Duration;

/**
 * A collection of minor fixes to the JavaFX GUI.
 */
public final class MinorFixes {
    public static Tooltip fixTooltip(Tooltip tooltip) {
        tooltip.setShowDelay(Duration.millis(500));
        return tooltip;
    }

    private MinorFixes() {
    }
}
