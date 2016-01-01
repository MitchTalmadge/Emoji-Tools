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

import java.io.File;
import java.io.FileOutputStream;

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
    }

    @Override
    protected FileChooser.ExtensionFilter getFileChooserExtensionFilter() {
        return null;
    }

    @Override
    protected boolean validateSelectedFile(File chooserFile) {
        //TODO: Check if folder contains Emojis
        return true;
    }

    @Override
    void startOperations() {

    }
}
