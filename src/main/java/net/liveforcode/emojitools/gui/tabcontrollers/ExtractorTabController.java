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

package net.liveforcode.emojitools.gui.tabcontrollers;

import javafx.fxml.FXML;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.stage.FileChooser;
import net.liveforcode.emojitools.EmojiTools;
import net.liveforcode.emojitools.gui.LimitingTextField;
import net.liveforcode.emojitools.gui.dialogs.OverwriteWarningDialog;
import net.liveforcode.emojitools.operations.conversion.ConversionInfo;
import net.liveforcode.emojitools.operations.renaming.RenamingInfo;

import java.io.File;

public class ExtractorTabController extends TabController {

    @FXML
    private ToggleGroup automaticRenaming;

    @FXML
    private RadioButton renameFalseToggle;

    @FXML
    private RadioButton renameTrueToggle;

    @FXML
    private ToggleGroup automaticConversion;

    @FXML
    private RadioButton convertFalseToggle;

    @FXML
    private RadioButton convertTrueToggle;

    @FXML
    private LimitingTextField extractionDirectoryNameField;

    @Override
    void initializeTab() {
        extractionDirectoryNameField.setRegexLimiter("(\\w|[_-])*");
        extractionDirectoryNameField.setMaxLength(32);
        extractionDirectoryNameField.setOnKeyTyped(e -> validateStartButton());
    }

    @Override
    protected void validateStartButton() {
        if (extractionDirectoryNameField.getText().matches("(\\w|[_-])+")) {
            if (selectedFile != null) {
                startButton.setDisable(false);
            } else
                startButton.setDisable(true);
        } else
            startButton.setDisable(true);
    }

    @Override
    protected FileChooser.ExtensionFilter getFileChooserExtensionFilter() {
        return new FileChooser.ExtensionFilter("Emoji Font File (*.ttf)", "*.ttf");
    }

    @Override
    protected boolean validateSelectedFile(File file) {
        return true;
    }

    @Override
    void startOperations() {
        boolean shouldContinue = true;

        File extractionDirectory = new File(EmojiTools.getRootDirectory(), this.extractionDirectoryNameField.getText());
        if (extractionDirectory.exists()) {
            File[] files = extractionDirectory.listFiles();
            if (files != null) {
                if (files.length > 0) {
                    System.out.println("Files will be overwritten. Asking user for confirmation.");
                    if (new OverwriteWarningDialog(extractionDirectory).getResult()) {
                        shouldContinue = EmojiTools.performDeletionOperation(extractionDirectory);
                    }
                } else {
                    System.out.println("No files will be overwritten. Continuing.");
                }
            } else {
                boolean dirCreated = extractionDirectory.mkdir();
                if (!dirCreated) {
                    System.out.println("Could not create extraction directory.");
                    shouldContinue = false;
                }
            }
        } else {
            boolean dirCreated = extractionDirectory.mkdir();
            if (!dirCreated) {
                System.out.println("Could not create extraction directory.");
                shouldContinue = false;
            }
        }

        if (shouldContinue) {
            shouldContinue = EmojiTools.performExtractionOperation(selectedFile, extractionDirectory);
        } else {
            return;
        }

        if (this.renameTrueToggle.isSelected() && shouldContinue) {
            shouldContinue = EmojiTools.performRenamingOperation(extractionDirectory, new RenamingInfo(RenamingInfo.PREFIX_REMOVE_ALL, RenamingInfo.CASE_DONT_CHANGE, false));
        }

        if (this.convertTrueToggle.isSelected() && shouldContinue) {
            shouldContinue = EmojiTools.performConversionOperation(extractionDirectory, new ConversionInfo(ConversionInfo.DIRECTION_CGBI_RGBA));
        }

        //TODO: Show completion dialog, or tell user that it did not complete successfully based on shouldContinue value.

        //new FinishedDialog(this.gui, "Emoji Extraction Complete!", "Your Extracted Emojis can be found in:", extractionDirectoryFile).setVisible(true);
    }
}
