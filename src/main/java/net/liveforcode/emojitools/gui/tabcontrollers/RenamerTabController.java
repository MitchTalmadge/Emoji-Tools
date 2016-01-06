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

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.stage.FileChooser;
import net.liveforcode.emojitools.EmojiTools;
import net.liveforcode.emojitools.gui.dialogs.OperationFinishedDialog;
import net.liveforcode.emojitools.operations.renaming.RenamingInfo;

import java.io.File;

public class RenamerTabController extends TabController {

    @FXML
    private RadioButton prefixesRemoveAllToggle;

    @FXML
    private ToggleGroup prefixes;

    @FXML
    private RadioButton prefixesNoChangeToggle;

    @FXML
    private RadioButton prefixesSetUniToggle;

    @FXML
    private RadioButton prefixesSetUToggle;

    @FXML
    private ToggleGroup caseGroup;

    @FXML
    private RadioButton caseNoChangeToggle;

    @FXML
    private RadioButton caseUpperToggle;

    @FXML
    private RadioButton caseLowerToggle;

    @FXML
    private CheckBox casePrefixOppositeCheckBox;

    @FXML
    private TextField exampleOutputField;

    @Override
    void initializeTab() {
        updateExample();
        updatePrefixOppositeCaseCheckbox();
    }

    @Override
    protected void validateStartButton() {
        if ((!this.prefixesNoChangeToggle.isSelected() || !this.caseNoChangeToggle.isSelected()) &&
                this.selectedFile != null)
            this.startButton.setDisable(false);
        else
            this.startButton.setDisable(true);
    }

    private void updateExample() {
        String uniPrefix = "";
        String uPrefix = "";
        int usePrefix = 0; //0 = both, 1 = none, 2 = uni, 3 = u

        if (this.prefixesNoChangeToggle.isSelected() || this.prefixesSetUniToggle.isSelected() || this.prefixesSetUToggle.isSelected()) {
            usePrefix = 0;
        } else if (this.prefixesRemoveAllToggle.isSelected()) {
            usePrefix = 1;
        }

        if (this.caseNoChangeToggle.isSelected()) {
            if (this.prefixesSetUniToggle.isSelected()) {
                uPrefix = "uni1F60D.png";
                uniPrefix = "uni1f60d.png";
            } else if (this.prefixesSetUToggle.isSelected()) {
                uPrefix = "u1F60D.png";
                uniPrefix = "u1f60d.png";
            } else {
                uPrefix = "u1F60D.png";
                uniPrefix = "uni1f60d.png";
            }
        } else if (this.caseUpperToggle.isSelected()) {
            if (usePrefix == 0 && !this.prefixesNoChangeToggle.isSelected())
                usePrefix = (this.prefixesSetUniToggle.isSelected()) ? 2 : 3;

            if (this.casePrefixOppositeCheckBox.isSelected()) {
                uPrefix = "u1F60D.png";
                uniPrefix = "uni1F60D.png";
            } else {
                uPrefix = "U1F60D.png";
                uniPrefix = "UNI1F60D.png";
            }
        } else if (this.caseLowerToggle.isSelected()) {
            if (usePrefix == 0 && !this.prefixesNoChangeToggle.isSelected())
                usePrefix = (this.prefixesSetUniToggle.isSelected()) ? 2 : 3;

            if (this.casePrefixOppositeCheckBox.isSelected()) {
                uPrefix = "U1f60d.png";
                uniPrefix = "UNI1f60d.png";
            } else {
                uPrefix = "u1f60d.png";
                uniPrefix = "uni1f60d.png";
            }
        }

        switch (usePrefix) {
            case 0:
                this.exampleOutputField.setText(uniPrefix + " or " + uPrefix);
                break;
            case 1:
                if (this.caseUpperToggle.isSelected() || this.caseLowerToggle.isSelected())
                    this.exampleOutputField.setText(uniPrefix.substring(3, uniPrefix.length()));
                else
                    this.exampleOutputField.setText(uniPrefix.substring(3, uniPrefix.length()) + " or " + uPrefix.substring(1, uPrefix.length()));
                break;
            case 2:
                this.exampleOutputField.setText(uniPrefix);
                break;
            case 3:
                this.exampleOutputField.setText(uPrefix);
                break;
            default:
                this.exampleOutputField.setText(uniPrefix + " or " + uPrefix);
                break;
        }

    }

    private void updatePrefixOppositeCaseCheckbox() {
        if (prefixesRemoveAllToggle.isSelected())
            casePrefixOppositeCheckBox.setDisable(true);
        else if (caseNoChangeToggle.isSelected())
            casePrefixOppositeCheckBox.setDisable(true);
        else
            casePrefixOppositeCheckBox.setDisable(false);
    }

    @Override
    protected FileChooser.ExtensionFilter getFileChooserExtensionFilter() {
        return null;
    }

    @Override
    protected boolean validateSelectedFile(File chooserFile) {
        if (!chooserFile.isDirectory())
        {
            EmojiTools.showErrorDialog("Invalid Directory", "Please choose a directory, not a file.");
            return false;
        }
        File[] files = chooserFile.listFiles();
        if (files == null)
        {
            EmojiTools.showErrorDialog("Invalid Directory", "Please choose a directory, not a file.");
            return false;
        }
        if (files.length == 0)
        {
            EmojiTools.showWarningDialog("Empty Directory", "The chosen directory is empty. There is nothing to rename.");
            return false;
        }
        for (File file : files)
            if (file.getName().endsWith(".png"))
                return true; //We found a png file, good enough.
        {
            EmojiTools.showErrorDialog("Directory Contains No Emojis", "The chosen directory contains no emojis. Please pick a directory containing emojis.");
            return false;
        }
    }

    @Override
    void startOperations() {
        boolean shouldContinue = true;

        int prefixOption = 0;

        if (prefixesNoChangeToggle.isSelected())
            prefixOption = RenamingInfo.PREFIX_DONT_CHANGE;
        else if (prefixesRemoveAllToggle.isSelected())
            prefixOption = RenamingInfo.PREFIX_REMOVE_ALL;
        else if (prefixesSetUniToggle.isSelected())
            prefixOption = RenamingInfo.PREFIX_SET_UNI;
        else if (prefixesSetUToggle.isSelected())
            prefixOption = RenamingInfo.PREFIX_SET_U;

        int caseOption = 0;

        if (caseNoChangeToggle.isSelected())
            caseOption = RenamingInfo.CASE_DONT_CHANGE;
        else if (caseUpperToggle.isSelected())
            caseOption = RenamingInfo.CASE_UPPER;
        else if (caseLowerToggle.isSelected())
            caseOption = RenamingInfo.CASE_LOWER;

        shouldContinue = EmojiTools.performRenamingOperation(selectedFile, new RenamingInfo(prefixOption, caseOption, casePrefixOppositeCheckBox.isSelected()));

        if (shouldContinue)
            new OperationFinishedDialog("Renaming Complete!", "Your renamed emojis can be found in:", selectedFile).display();
        else
            EmojiTools.showErrorDialog("Renaming Unsuccessful.", "Renaming was cancelled or unsuccessful.");
    }

    @FXML
    void onCaseTogglesChanged(ActionEvent event) {
        updatePrefixOppositeCaseCheckbox();
        updateExample();
        validateStartButton();
    }

    @FXML
    void onPrefixTogglesChanged(ActionEvent event) {
        updatePrefixOppositeCaseCheckbox();
        updateExample();
        validateStartButton();
    }

}
