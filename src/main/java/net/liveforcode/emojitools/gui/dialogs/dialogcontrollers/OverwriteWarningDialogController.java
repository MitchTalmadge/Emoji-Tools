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

package net.liveforcode.emojitools.gui.dialogs.dialogcontrollers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

public class OverwriteWarningDialogController {

    @FXML
    private Button cancelButton;

    @FXML
    private Button continueButton;

    @FXML
    private TextField extractionDirectoryNameField;

    private File extractionDirectoryFile;
    private ResultListener resultListener;

    @FXML
    private void onCancelButtonFired(ActionEvent actionEvent) {
        if(resultListener != null)
            resultListener.onResultAcquired(false);
    }

    @FXML
    private void onContinueButtonFired(ActionEvent actionEvent) {
        if(resultListener != null)
            resultListener.onResultAcquired(true);
    }

    public void setResultListener(ResultListener listener)
    {
        this.resultListener = listener;
    }

    public void setExtractionDirectoryFile(File extractionDirectoryFile) {
        this.extractionDirectoryFile = extractionDirectoryFile;
        this.extractionDirectoryNameField.setText(extractionDirectoryFile.getName());
    }

    public interface ResultListener {
        void onResultAcquired(boolean result);
    }
}
