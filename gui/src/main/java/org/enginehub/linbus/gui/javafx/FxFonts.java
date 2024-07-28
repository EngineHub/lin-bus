package org.enginehub.linbus.gui.javafx;

import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;

public final class FxFonts {
    public static final Font DEFAULT = Font.getDefault();
    public static final Font ITALIC = Font.font(DEFAULT.getFamily(), FontPosture.ITALIC, DEFAULT.getSize());

    public record AndString(Font font, String string) {
    }

    private FxFonts() {
    }
}
