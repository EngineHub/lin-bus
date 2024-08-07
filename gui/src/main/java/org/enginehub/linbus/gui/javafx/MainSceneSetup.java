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

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableBooleanValue;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.ToolBar;
import javafx.scene.control.Tooltip;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableView;
import javafx.scene.input.KeyCharacterCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.enginehub.linbus.gui.LinBusGui;
import org.enginehub.linbus.gui.util.ErrorReporter;
import org.jspecify.annotations.Nullable;
import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid;
import org.kordamp.ikonli.javafx.FontIcon;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;
import java.util.concurrent.ExecutorService;

public class MainSceneSetup {

    private static BorderPane mainPane(MenuBar menuBar, Node treeTableView) {
        var pane = new BorderPane();
        pane.setTop(menuBar);
        pane.setCenter(treeTableView);
        return pane;
    }

    private static MenuBar menuBar(Menu fileMenu) {
        var menuBar = new MenuBar(
            fileMenu
        );
        menuBar.setUseSystemMenuBar(true);
        return menuBar;
    }

    private Menu fileMenu(MenuItem openFile, MenuItem saveFile) {
        var exit = new MenuItem("E_xit", FontIcon.of(FontAwesomeSolid.SIGN_OUT_ALT, 16));
        exit.setOnAction(__ -> {
            if (checkExitOkay()) {
                Platform.exit();
            }
        });
        exit.setAccelerator(new KeyCharacterCombination("Q", KeyCombination.SHORTCUT_DOWN));

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

    private MenuItem openFile(Stage stage, ExecutorService backgroundExecutor) {
        var openFile = new MenuItem("_Open...", FontIcon.of(FontAwesomeSolid.FILE_IMPORT, 16));
        openFile.setAccelerator(new KeyCharacterCombination("O", KeyCombination.SHORTCUT_DOWN));
        openFile.setOnAction(__ -> {
            var chooser = new FileChooser();
            chooser.setTitle("Choose an NBT File");
            chooser.setInitialDirectory(new File("."));
            File file = chooser.showOpenDialog(stage);
            if (file == null) {
                return;
            }
            Path path = file.toPath();
            backgroundExecutor.submit(new Task<TreeItem<NbtTreeView.TagEntry>>() {

                @Override
                protected TreeItem<NbtTreeView.TagEntry> call() throws Exception {
                    return NbtTreeView.loadTreeItem(path);
                }

                @Override
                protected void succeeded() {
                    openPath.set(path);
                    TreeItem<NbtTreeView.TagEntry> value = getValue();
                    originalTag.set(value.getValue());
                    treeTableView.setRoot(value);
                }

                @Override
                protected void failed() {
                    ErrorReporter.reportError(
                        ErrorReporter.Level.INFORM, "Failed to open file " + path, getException()
                    );
                }
            });
        });
        return openFile;
    }

    private MenuItem saveFile() {
        var saveFile = new MenuItem("_Save", FontIcon.of(FontAwesomeSolid.SAVE, 16));
        saveFile.disableProperty().bind(openPath.isNull().or(treeTableView.rootProperty().isNull()));
        saveFile.setAccelerator(new KeyCharacterCombination("S", KeyCombination.SHORTCUT_DOWN));
        saveFile.setOnAction(__ -> trySave());
        return saveFile;
    }

    private boolean trySave() {
        Path path = openPath.get();
        TreeItem<NbtTreeView.TagEntry> item = treeTableView.getRoot();
        if (path == null || item == null) {
            return false;
        }
        try {
            NbtTreeView.saveTreeItem(path, item);
        } catch (IOException e) {
            ErrorReporter.reportError(ErrorReporter.Level.INFORM, "Failed to save file " + openPath.get(), e);
            return false;
        }
        // Now that we've saved it, update, so we're not in modified anymore
        originalTag.set(item.getValue());
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(LinBusGui.TITLE_BASE + " - File Saved");
        alert.setHeaderText("File saved");
        alert.setContentText("Saved to " + path);
        alert.show();
        return true;
    }

    public boolean checkExitOkay() {
        if (!isModified.get()) {
            return true;
        }
        var alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(LinBusGui.TITLE_BASE + " - Exit Confirmation");
        alert.setHeaderText("Save changes before exiting?");
        alert.setContentText("You have unsaved changes. Do you want to save them before exiting?");
        alert.getButtonTypes().setAll(ButtonType.YES, ButtonType.NO, ButtonType.CANCEL);
        Optional<ButtonType> buttonTypeResult = alert.showAndWait();
        if (buttonTypeResult.isEmpty()) {
            return false;
        }
        ButtonType buttonType = buttonTypeResult.get();
        if (buttonType == ButtonType.CANCEL) {
            return false;
        }
        if (buttonType == ButtonType.YES) {
            return trySave();
        }
        return true;
    }

    private ToolBar toolBar() {
        var toolBar = new ToolBar(
            moveEntryUp(),
            moveEntryDown()
        );
        toolBar.disableProperty().bind(treeTableView.rootProperty().isNull());
        return toolBar;
    }

    private Button moveEntryUp() {
        var button = new Button(null, FontIcon.of(FontAwesomeSolid.SORT_NUMERIC_UP, 16));
        button.setTooltip(MinorFixes.fixTooltip(new Tooltip("Move entry up")));
        button.setAccessibleText("Move entry up");
        button.disableProperty().bind(
            treeTableView.getSelectionModel().selectedItemProperty().map(item ->
                // Disable if we don't have any parent to move up in
                (item == null || item.getParent() == null) ||
                    // Disable if we're the first child
                    item.getParent().getChildren().getFirst() == item
            )
        );
        button.setOnAction(event -> {
            NbtTreeView.moveEntryUp(treeTableView);
            event.consume();
        });
        return button;
    }

    private Button moveEntryDown() {
        var button = new Button(null, FontIcon.of(FontAwesomeSolid.SORT_NUMERIC_DOWN, 16));
        button.setTooltip(MinorFixes.fixTooltip(new Tooltip("Move entry down")));
        button.setAccessibleText("Move entry down");
        button.disableProperty().bind(
            treeTableView.getSelectionModel().selectedItemProperty().map(item ->
                // Disable if we don't have any parent to move down in
                (item == null || item.getParent() == null) ||
                    // Disable if we're the last child
                    item.getParent().getChildren().getLast() == item
            )
        );
        button.setOnAction(event -> {
            NbtTreeView.moveEntryDown(treeTableView);
            event.consume();
        });
        return button;
    }

    private final TreeTableView<NbtTreeView.TagEntry> treeTableView = NbtTreeView.create();
    private final ObjectProperty<NbtTreeView.TagEntry> originalTag = new SimpleObjectProperty<>(this, "originalTag");
    public final ObjectProperty<@Nullable Path> openPath = new SimpleObjectProperty<>(this, "openPath");
    public final ObservableBooleanValue isModified;
    public final Scene mainScene;

    public MainSceneSetup(Stage stage, ExecutorService backgroundExecutor) {
        openPath.addListener((__, oldPath, newPath) -> {
            if (newPath != null) {
                try {
                    treeTableView.setRoot(NbtTreeView.loadTreeItem(newPath));
                } catch (IOException e) {
                    ErrorReporter.reportError(ErrorReporter.Level.INFORM, "Failed to open file " + newPath, e);
                }
            }
        });
        ObservableValue<NbtTreeView.TagEntry> rootTagEntry = treeTableView.rootProperty().flatMap(TreeItem::valueProperty);
        isModified = originalTag.isNotEqualTo(
            // Promote ObservableValue to ObjectBinding
            Bindings.createObjectBinding(rootTagEntry::getValue, rootTagEntry)
        );

        BorderPane mainPane = mainPane(
            menuBar(fileMenu(
                openFile(stage, backgroundExecutor),
                saveFile()
            )),
            new VBox(
                toolBar(),
                treeTableView
            )
        );
        VBox.setVgrow(treeTableView, Priority.ALWAYS);
        mainScene = new Scene(mainPane, 900, 600);
    }
}
