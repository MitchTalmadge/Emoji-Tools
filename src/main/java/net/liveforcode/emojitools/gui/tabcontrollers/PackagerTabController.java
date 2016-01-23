/*
 * Emoji Tools helps users and developers of Android, iOS, and OS X extract, modify, and repackage Emoji fonts.
 * Copyright (C) 2015 - 2016 Mitch Talmadge
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

package net.liveforcode.emojitools.gui.tabcontrollers;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import net.liveforcode.emojitools.EmojiTools;
import net.liveforcode.emojitools.gui.dialogs.OperationFinishedDialog;

import java.io.File;

public class PackagerTabController extends TabController {

    @FXML
    public VBox deviceSelectionBox;

    @FXML
    public RadioButton androidDeviceToggle;

    @FXML
    public RadioButton iosDeviceToggle;

    @FXML
    public RadioButton osxDeviceToggle;

    @FXML
    protected Label fontInfoLabel;

    @Override
    void initializeTab() {

    }

    @Override
    protected void validateStartButton() {
        if (selectedFile != null)
            startButton.setDisable(false);
        else
            startButton.setDisable(true);
    }

    @Override
    protected FileChooser.ExtensionFilter getFileChooserExtensionFilter() {
        return null;
    }

    @Override
    protected boolean validateSelectedFile(File chooserFile) {
        if (!chooserFile.isDirectory()) {
            EmojiTools.showErrorDialog("Invalid Directory", "Please choose a directory, not a file.");
            return false;
        }
        File[] files = chooserFile.listFiles();
        if (files == null) {
            EmojiTools.showErrorDialog("Invalid Directory", "Please choose a directory, not a file.");
            return false;
        }
        if (files.length == 0) {
            EmojiTools.showWarningDialog("Empty Directory", "The chosen directory is empty. There is nothing to package.");
            return false;
        }
        boolean pngFileFound = false;
        for (File file : files)
            if (file.getName().endsWith(".png"))
                pngFileFound = true; //We found a png file, good enough.
        if (!pngFileFound) {
            EmojiTools.showWarningDialog("Directory Contains No Emojis", "The chosen directory contains no emojis. Please pick a directory containing emojis.");
            return false;
        }

        return true;
    }

    @Override
    void startOperations() {
        if (EmojiTools.performPackagingOperation(selectedFile))
            new OperationFinishedDialog("Packaging Complete!", "Your packaged emoji font can be found in:", new File(EmojiTools.getRootDirectory(), "Output")).display();
        else
            EmojiTools.showErrorDialog("Packaging Unsuccessful.", "Packaging was cancelled or unsuccessful.");
    }
}
