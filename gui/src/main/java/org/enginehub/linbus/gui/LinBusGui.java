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

import javafx.application.Application;
import javafx.beans.binding.Bindings;
import javafx.stage.Stage;
import org.enginehub.linbus.gui.javafx.MainSceneSetup;
import org.tinylog.Logger;

import java.nio.file.Path;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class LinBusGui extends Application {
    public static final String TITLE_BASE = "lin-bus NBT Editor";

    private final ExecutorService backgroundExecutor = Executors.newCachedThreadPool();

    public static void main(String[] args) {
        launch(args);
    }


    @Override
    public void start(Stage stage) {
        MainSceneSetup factory = new MainSceneSetup(stage, backgroundExecutor);
        factory.mainScene.getStylesheets().add(
            Objects.requireNonNull(
                getClass().getResource("/org/enginehub/linbus/gui/css/all.css"),
                "Could not find CSS file"
            ).toExternalForm()
        );

        stage.setOnCloseRequest(event -> {
            if (!factory.checkExitOkay()) {
                event.consume();
            }
        });
        stage.titleProperty().bind(Bindings.createStringBinding(() -> {
            Path path = factory.openPath.get();
            if (path == null) {
                return TITLE_BASE;
            }
            boolean modified = factory.isModified.get();
            return TITLE_BASE + " - " + path.getFileName() + (modified ? " (modified)" : "");
        }, factory.openPath, factory.isModified));
        stage.setScene(factory.mainScene);
        stage.show();
    }

    @Override public void stop() throws Exception {
        backgroundExecutor.shutdownNow();
        if (!backgroundExecutor.awaitTermination(1, TimeUnit.MINUTES)) {
            Logger.error("Background executor did not shut down in time");
        }
    }
}
