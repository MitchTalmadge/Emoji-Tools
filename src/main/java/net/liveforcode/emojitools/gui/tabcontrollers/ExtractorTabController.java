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
import net.liveforcode.emojitools.conversion.ConversionManager;
import net.liveforcode.emojitools.deletion.DeletionManager;
import net.liveforcode.emojitools.extraction.ExtractionManager;
import net.liveforcode.emojitools.gui.LimitingTextField;
import net.liveforcode.emojitools.gui.dialogs.OverwriteWarningDialog;
import net.liveforcode.emojitools.renaming.RenamingManager;

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
    protected void validateStartButton()
    {
        if(extractionDirectoryNameField.getText().matches("(\\w|[_-])+"))
        {
            if(selectedFile != null)
            {
                startButton.setDisable(false);
            }
            else
                startButton.setDisable(true);
        }
        else
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
        this.operationsCancelled = false;

        File extractionDirectoryFile = new File(EmojiTools.getRootDirectory(), this.extractionDirectoryNameField.getText());
        if (extractionDirectoryFile.exists()) {
            if(new OverwriteWarningDialog(extractionDirectoryFile).getResult())
            {
                /*DeletionDialog deletionDialog = new DeletionDialog(this);
                this.currentOperationManager = new DeletionManager(extractionDirectoryFile, this.gui, deletionDialog);
                currentOperationManager.start();
                deletionDialog.setVisible(true);*/
            }
        }

        if (!operationsCancelled) {
            /*ExtractionDialog extractionDialog = new ExtractionDialog(this);
            this.currentOperationManager = new ExtractionManager(this.selectedFile, extractionDirectoryFile, this.gui, extractionDialog);
            currentOperationManager.start();
            extractionDialog.setVisible(true);*/
        } else {
            return;
        }

        if (this.renameTrueToggle.isSelected() && !operationsCancelled) {
            /*RenamingDialog renamingDialog = new RenamingDialog(this);
            this.currentOperationManager = new RenamingManager(extractionDirectoryFile, this.gui, renamingDialog, new boolean[]{false, true, false, false}, new boolean[]{true, false, false, false});
            currentOperationManager.start();
            renamingDialog.setVisible(true);*/
        }

        if (this.convertTrueToggle.isSelected() && !operationsCancelled) {
            /*ConversionDialog conversionDialog = new ConversionDialog(this);
            this.currentOperationManager = new ConversionManager(extractionDirectoryFile, this.gui, conversionDialog, true);
            currentOperationManager.start();
            conversionDialog.setVisible(true);*/
        }

        //new FinishedDialog(this.gui, "Emoji Extraction Complete!", "Your Extracted Emojis can be found in:", extractionDirectoryFile).setVisible(true);
    }
}
