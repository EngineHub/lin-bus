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
import javafx.stage.Stage;
import org.enginehub.linbus.gui.inject.ApplicationComponent;
import org.enginehub.linbus.gui.inject.DaggerApplicationComponent;

public class LinBusGui extends Application {
    public static void main(String[] args) {
        launch(args);
    }

    private final ApplicationComponent component = DaggerApplicationComponent.create();

    @Override
    public void start(Stage stage) {
        stage.setTitle("LIN Bus NBT Editor");
        stage.setScene(
            component.fxDef()
                .primaryStage(stage)
                .build()
                .mainScene()
        );
        stage.show();
    }
}
