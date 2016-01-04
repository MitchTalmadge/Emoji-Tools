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
import net.liveforcode.emojitools.operations.conversion.ConversionInfo;

import java.io.File;

public class ConverterTabController extends TabController {

    @FXML
    private RadioButton conversionCgBItoRGBAToggle;

    @FXML
    private RadioButton conversionRGBAtoCgBIToggle;

    @FXML
    private ToggleGroup direction;

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
        if (!chooserFile.isDirectory()) //TODO: Tell user to pick a directory (this shouldn't happen anyway)
            return false;
        File[] files = chooserFile.listFiles();
        if (files == null) //TODO: Tell user to pick a directory (this shouldn't happen anyway)
            return false;
        if (files.length == 0) //TODO: Say directory is empty
            return false;
        for (File file : files)
            if (file.getName().endsWith(".png"))
                return true; //We found a png file, good enough.
        //TODO: Say directory contains no emojis.
        return false; //No png files found
    }

    @Override
    void startOperations() {
        boolean shouldContinue = true;

        int conversionDirection = 0;
        if (conversionCgBItoRGBAToggle.isSelected())
            conversionDirection = ConversionInfo.DIRECTION_CGBI_RGBA;
        else if (conversionRGBAtoCgBIToggle.isSelected())
            conversionDirection = ConversionInfo.DIRECTION_RGBA_CGBI;

        shouldContinue = EmojiTools.performConversionOperation(selectedFile, new ConversionInfo(conversionDirection));

        //TODO: Show completion dialog, or tell user that it did not complete successfully based on shouldContinue value.

        //new FinishedDialog(this.gui, "Emoji Conversion Complete!", "Your Converted Emojis can be found in:", conversionFile).setVisible(true);
    }
}
