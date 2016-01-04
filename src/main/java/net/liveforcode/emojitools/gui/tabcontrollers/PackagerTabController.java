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

import javafx.stage.FileChooser;
import net.liveforcode.emojitools.EmojiTools;
import net.liveforcode.emojitools.gui.dialogs.OperationFinishedDialog;
import net.liveforcode.emojitools.operations.conversion.ConversionInfo;
import net.liveforcode.emojitools.operations.renaming.RenamingInfo;

import java.io.File;

public class PackagerTabController extends TabController {

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

        //Check for Emojis
        boolean pngFileFound = false;
        for (File file : files)
            if (file.getName().endsWith(".png"))
                pngFileFound = true; //We found a png file, good enough.
        if (!pngFileFound)
            return false; //TODO: Say directory contains no emojis.

        //Check for .ttx file
        File ttxFile = null;
        for (File file : files) {
            if (file.getName().endsWith(".ttx")) {
                ttxFile = file;
                break;
            }
        }

        return ttxFile != null;

    }

    @Override
    void startOperations() {
        boolean shouldContinue = true;

        shouldContinue = EmojiTools.performRenamingOperation(selectedFile, new RenamingInfo(RenamingInfo.PREFIX_SET_UNI, RenamingInfo.CASE_LOWER, false));

        if (shouldContinue) {
            shouldContinue = EmojiTools.performConversionOperation(selectedFile, new ConversionInfo(ConversionInfo.DIRECTION_CGBI_RGBA));
        }

        if (shouldContinue) {
            shouldContinue = EmojiTools.performPackagingOperation(selectedFile);
        }

        if (shouldContinue)
            new OperationFinishedDialog("Packaging Complete!", "Your packaged emoji font can be found in:", new File(EmojiTools.getRootDirectory(), "Output")).display();
        //TODO: Show unsuccessful dialog
    }
}
