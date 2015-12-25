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

package net.liveforcode.emojitools.gui2.tabcontrollers2;

import javafx.fxml.FXML;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.stage.FileChooser;

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
    private TextField extractionDirectoryNameField;

    @Override
    void initializeTab() {
        extractionDirectoryNameField = new TextField("ExtractedEmojis")
        {
            @Override
            public void replaceText(int start, int end, String text) {
                if(text.matches("(\\w|[_-])+"))
                    super.replaceText(start, end, text);
            }

            @Override
            public void replaceSelection(String replacement) {
                if(replacement.matches("(\\w|[_-])+"))
                    super.replaceSelection(replacement);
            }
        };
    }

    private void validateStartButton()
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

    }
}
