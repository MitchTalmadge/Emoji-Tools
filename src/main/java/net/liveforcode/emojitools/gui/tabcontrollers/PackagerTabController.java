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
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import net.liveforcode.emojitools.EmojiTools;
import net.liveforcode.emojitools.gui.dialogs.OperationFinishedDialog;
import net.liveforcode.emojitools.operations.FontType;
import net.liveforcode.emojitools.operations.packaging.PackagingInfo;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class PackagerTabController extends TabController {

    @FXML
    protected VBox deviceSelectionBox;

    @FXML
    protected RadioButton androidDeviceToggle;

    @FXML
    protected RadioButton iosDeviceToggle;

    @FXML
    protected RadioButton osxDeviceToggle;

    @FXML
    protected ToggleGroup deviceToggleGroup;

    private FontType fontType;
    private short[] resolutions;

    @Override
    void initializeTab() {
        validateFontOptions();
    }

    @Override
    protected void validateStartButton() {
        if (selectedFile != null) {
            if (fontType != null && this.deviceToggleGroup.getSelectedToggle() != null) {
                startButton.setDisable(false);
                return;
            }
        }
        startButton.setDisable(true);
    }

    private void validateFontOptions() {
        if (fontType == null) {
            this.deviceSelectionBox.setVisible(false);
            this.deviceSelectionBox.setDisable(true);
        } else {
            this.deviceSelectionBox.setVisible(true);
            this.deviceSelectionBox.setDisable(false);
            switch (fontType) {
                case GOOGLE:
                    this.androidDeviceToggle.setDisable(false);
                    this.androidDeviceToggle.setSelected(true);
                    this.iosDeviceToggle.setDisable(true);
                    this.osxDeviceToggle.setDisable(true);
                    break;
                case APPLE:
                    this.androidDeviceToggle.setDisable(true);
                    this.iosDeviceToggle.setDisable(false);
                    this.iosDeviceToggle.setSelected(true);
                    this.osxDeviceToggle.setDisable(false);
                    break;
                default:
                    this.deviceSelectionBox.setVisible(false);
                    this.deviceSelectionBox.setDisable(true);
            }
        }
    }

    @Override
    protected FileChooser.ExtensionFilter getFileChooserExtensionFilter() {
        return null;
    }

    @Override
    protected boolean validateSelectedFile(File chooserFile) {
        this.fontType = null;
        validateFontOptions();

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

        File emojiToolsInfo = new File(chooserFile, FontType.FONT_PROPERTIES_FILE_NAME);
        if (!emojiToolsInfo.exists()) {
            EmojiTools.showErrorDialog("Invalid Directory", "The chosen directory was not created by extracting emojis with Emoji Tools, or " + FontType.FONT_PROPERTIES_FILE_NAME + " is missing.");
            return false;
        }

        try {
            Properties properties = new Properties();
            properties.load(new FileInputStream(emojiToolsInfo));

            String fontTypeValue = properties.getProperty(FontType.FONT_PROPERTY_NAME);
            if (fontTypeValue == null) {
                EmojiTools.showErrorDialog("Corrupted File", FontType.FONT_PROPERTIES_FILE_NAME + " is corrupted or was modified. Packaging cannot be performed.");
                return false;
            }
            this.fontType = FontType.valueOf(fontTypeValue);

            String resolutionsValue = properties.getProperty(FontType.FONT_RESOLUTIONS_PROPERTY_NAME);
            if(resolutionsValue != null)
            {
                String[] resolutionsSplit = resolutionsValue.split(",");
                short[] resolutions = new short[resolutionsSplit.length];
                for(int i = 0; i < resolutionsSplit.length; i++)
                {
                    resolutions[i] = Short.parseShort(resolutionsSplit[i]);
                }

                this.resolutions = resolutions;
            }
            validateFontOptions();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return true;
    }

    @Override
    void startOperations() {
        int deviceType = 0;
        if (androidDeviceToggle.isSelected())
            deviceType = PackagingInfo.DEVICE_ANDROID;
        else if (iosDeviceToggle.isSelected())
            deviceType = PackagingInfo.DEVICE_IOS;
        else if (osxDeviceToggle.isSelected())
            deviceType = PackagingInfo.DEVICE_OSX;

        if (EmojiTools.performPackagingOperation(selectedFile, new PackagingInfo(deviceType, resolutions)))
            new OperationFinishedDialog("Packaging Complete!", "Your packaged emoji font can be found in:", new File(EmojiTools.getRootDirectory(), "Output")).display();
        else
            EmojiTools.showErrorDialog("Packaging Unsuccessful.", "Packaging was cancelled or unsuccessful.");
    }
}
