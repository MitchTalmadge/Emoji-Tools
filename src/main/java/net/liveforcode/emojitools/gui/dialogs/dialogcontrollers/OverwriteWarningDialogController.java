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

package net.liveforcode.emojitools.gui.dialogs.dialogcontrollers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import net.liveforcode.emojitools.EmojiTools;
import net.liveforcode.emojitools.gui.dialogs.OverwriteWarningDialog;

import java.io.File;

public class OverwriteWarningDialogController {

    @FXML
    protected Button cancelButton;

    @FXML
    protected Button continueButton;

    @FXML
    protected TextField extractionDirectoryNameField;

    private File extractionDirectoryFile;
    private OverwriteWarningDialog parent;

    @FXML
    protected void onCancelButtonFired(ActionEvent actionEvent) {
        parent.onResultAcquired(false);
    }

    @FXML
    protected void onContinueButtonFired(ActionEvent actionEvent) {
        EmojiTools.getLogManager().logInfo("OverwriteWarningDialog: User continued.");
        parent.onResultAcquired(true);
    }

    public void setExtractionDirectoryFile(File extractionDirectoryFile) {
        this.extractionDirectoryFile = extractionDirectoryFile;
        this.extractionDirectoryNameField.setText(extractionDirectoryFile.getName());
    }

    public void setParent(OverwriteWarningDialog parent) {
        this.parent = parent;
    }
}
