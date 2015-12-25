/*
 * Emoji Tools helps users and developers of Android, iOS, and OS X extract, modify, and repackage Emoji fonts.
 * Copyright (C) 2015 Mitch Talmadge
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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Contact Mitch Talmadge at mitcht@liveforcode.net
 */

package net.liveforcode.EmojiTools2.GUI.TabControllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import net.liveforcode.EmojiTools2.EmojiTools;
import net.liveforcode.EmojiTools2.OperationManager;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public abstract class TabController implements Initializable {

    @FXML
    Label filePathTitleLabel;

    @FXML
    TextField filePathField;

    @FXML
    Button browseButton;

    @FXML
    Button openRootDirectoryButton;

    @FXML
    Button startButton;

    OperationManager currentOperationManager;
    boolean operationsCancelled;
    File selectedFile;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        initializeTab();
    }

    abstract void initializeTab();

    @FXML
    void onBrowseButtonFired(ActionEvent actionEvent) {
        FileChooser.ExtensionFilter extensionFilter = getFileChooserExtensionFilter();
        File chooserFile;
        if (extensionFilter != null) {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle(filePathTitleLabel.getText());
            fileChooser.getExtensionFilters().add(extensionFilter);
            fileChooser.setInitialDirectory(EmojiTools.getRootDirectory());
            chooserFile = fileChooser.showOpenDialog(browseButton.getScene().getWindow());
        } else {
            DirectoryChooser directoryChooser = new DirectoryChooser();
            directoryChooser.setTitle(filePathTitleLabel.getText());
            directoryChooser.setInitialDirectory(EmojiTools.getRootDirectory());
            chooserFile = directoryChooser.showDialog(browseButton.getScene().getWindow());
        }

        if (chooserFile != null) {
            if (validateSelectedFile(chooserFile)) {
                this.selectedFile = chooserFile;
                this.filePathField.setText(selectedFile.getName());
            }
        }
    }

    protected abstract FileChooser.ExtensionFilter getFileChooserExtensionFilter();

    protected abstract boolean validateSelectedFile(File file);

    @FXML
    void onOpenRootDirectoryButtonFired(ActionEvent actionEvent) {
        Desktop desktop = Desktop.getDesktop();
        try {
            desktop.open(EmojiTools.getRootDirectory());
        } catch (IOException e1) {
            EmojiTools.submitError(Thread.currentThread(), e1);
        }
    }

    @FXML
    void onStartButtonFired(ActionEvent actionEvent) {
        startOperations();
    }

    abstract void startOperations();

    public void stopOperations() {
        if (this.currentOperationManager != null)
            this.currentOperationManager.stop();
        this.operationsCancelled = true;
    }
}
