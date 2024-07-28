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

package org.enginehub.linbus.gui.util;

import com.google.common.base.Throwables;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.stage.Screen;
import org.jspecify.annotations.Nullable;
import org.tinylog.Logger;

import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.stream.Stream;

public final class ErrorReporter {
    public enum Level {
        TRACK,
        INFORM,
        DIE,
        ;

        private void logMessage(String message, Throwable t) {
            switch (this) {
                case TRACK -> Logger.info(t, message);
                case INFORM -> Logger.warn(t, message);
                case DIE -> Logger.error(t, message);
            }
        }
    }

    private ErrorReporter() {
    }

    public static void reportError(Level level, String message, Throwable t) {
        level.logMessage(message, t);
        if (level.compareTo(Level.INFORM) < 0) {
            return;
        }
        var cf = new CompletableFuture<@Nullable Void>();
        cf.whenComplete((value, t2) -> {
            if (t2 != null) {
                Logger.warn("Error displaying error to user", t2);
            }
            if (level == Level.DIE) {
                Platform.exit();
            }
        });
        Platform.runLater(() -> {
            try {
                var alert = initAlert(message, t);
                alert.setOnHidden(e -> cf.complete(null));
                alert.show();
            } catch (Throwable t2) {
                cf.completeExceptionally(t2);
            }
        });
    }

    private static Alert initAlert(String message, Throwable t) {
        var alert = new Alert(Alert.AlertType.ERROR);

        var contentPane = new BorderPane();

        contentPane.setTop(new Label(message + ":"));
        BorderPane.setMargin(contentPane.getTop(), new Insets(8));

        var stackTrace = Throwables.getStackTraceAsString(t);
        var stackTraceLines = stackTrace.split("\r?\n");
        var maxCols = Stream.of(stackTraceLines).mapToInt(String::length).max().orElseThrow();

        var exceptionTextArea = new TextArea(stackTrace);
        exceptionTextArea.setPrefRowCount(30);
        exceptionTextArea.setPrefColumnCount((int) (maxCols * 1.05));
        exceptionTextArea.setStyle("-fx-font-family: 'monospaced';");
        contentPane.setCenter(exceptionTextArea);
        BorderPane.setMargin(contentPane.getCenter(), new Insets(8));

        alert.getDialogPane().setContent(contentPane);
        var screenBounds = Screen.getPrimary().getVisualBounds();
        alert.getDialogPane().setMaxSize(
            screenBounds.getWidth() - 50,
            screenBounds.getHeight() - 100
        );
        return alert;
    }

    /**
     * Create a partially applied variant of {@link #reportError(Level, String, Throwable)}, using the {@link Throwable}
     * from a later call to perform the full call.
     *
     * <p>
     * This is intended for use with {@link java.util.concurrent.CompletionStage#whenComplete(BiConsumer)}.
     * </p>
     *
     * @param level the level to log at
     * @param message the message to include
     *
     * @return a {@link BiConsumer} that will call {@link #reportError(Level, String, Throwable)} if given a non-null
     * {@link Throwable}
     */
    public static BiConsumer<Object, @Nullable Throwable> bind(Level level, String message) {
        return (value, t) -> {
            if (t != null) {
                reportError(level, message, t);
            }
        };
    }
}
