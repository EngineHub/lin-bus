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

package org.enginehub.linbus.gui.fxdef;

import dagger.Module;
import dagger.Provides;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.BorderPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.enginehub.linbus.gui.OS;
import org.enginehub.linbus.gui.fx.FxEventLoop;
import org.enginehub.linbus.gui.model.NbtTreeModel;
import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid;
import org.kordamp.ikonli.javafx.FontIcon;

import javax.inject.Qualifier;
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Module
public class LinMain {
    @Qualifier
    @Retention(RetentionPolicy.CLASS)
    @Target({ElementType.METHOD, ElementType.PARAMETER})
    public @interface Def {
        String value() default "";
    }

    @Provides
    @FxDefScope
    @Def
    public static Scene scene(@FxDefScope @Def("mainPane") BorderPane mainPane) {
        return new Scene(mainPane, 900, 600);
    }

    @Provides
    @FxDefScope
    @Def("mainPane")
    public static BorderPane mainPane(@FxDefScope @Def MenuBar menuBar) {
        var pane = new BorderPane();
        pane.setTop(menuBar);
        return pane;
    }

    @Provides
    @FxDefScope
    @Def
    public static MenuBar menuBar(@FxDefScope @Def("file") Menu fileMenu) {
        var menuBar = new MenuBar(
            fileMenu
        );
        menuBar.setUseSystemMenuBar(true);
        return menuBar;
    }

    @Provides
    @FxDefScope
    @Def("file")
    public static Menu fileMenu(
        @FxDefScope @Def("openFile") MenuItem openFile,
        @FxDefScope @Def("saveFile") MenuItem saveFile
    ) {
        var exit = new MenuItem("E_xit", FontIcon.of(FontAwesomeSolid.SIGN_OUT_ALT, 16));
        exit.setOnAction(__ -> Platform.exit());
        exit.setAccelerator(KeyCombination.valueOf(OS.detected() == OS.MAC_OS ? "Meta+Q" : "Alt+F4"));

        return new Menu(
            "_File",
            null,
            openFile,
            new SeparatorMenuItem(),
            saveFile,
            new SeparatorMenuItem(),
            exit
        );
    }

    @Provides
    @FxDefScope
    @Def("openFile")
    public static MenuItem openFile(
        Stage stage,
        @FxEventLoop Thread.Builder eventLoop
    ) {
        var openFile = new MenuItem("_Open...", FontIcon.of(FontAwesomeSolid.FILE_IMPORT, 16));
        openFile.setAccelerator(KeyCombination.valueOf("Shortcut+O"));
        openFile.setOnAction(__ -> eventLoop.start(() -> {
            var chooser = new FileChooser();
            chooser.setTitle("Choose an NBT File");
            chooser.setInitialDirectory(new File("."));
            var file = chooser.showOpenDialog(stage);
            if (file == null) {
                return;
            }
            try {
                Thread.ofVirtual().start(() -> {
                    NbtTreeModel model;
                    try {
                        model = NbtTreeModel.loadTreeModel(file.toPath());
                    } catch (IOException e) {
                        throw new UncheckedIOException(e);
                    }
                    System.err.println(model);
                }).join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException(e);
            }
        }));
        return openFile;
    }

    @Provides
    @FxDefScope
    @Def("saveFile")
    public static MenuItem saveFile() {
        var saveFile = new MenuItem("_Save", FontIcon.of(FontAwesomeSolid.SAVE, 16));
        saveFile.setAccelerator(KeyCombination.valueOf("Shortcut+S"));
        saveFile.setOnAction(__ -> {
            throw new UnsupportedOperationException("TODO");
        });
        return saveFile;
    }
}
