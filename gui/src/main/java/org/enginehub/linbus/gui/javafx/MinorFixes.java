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
