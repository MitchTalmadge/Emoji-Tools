/*
 * Copyright (C) 2015 - 2016 Mitch Talmadge (https://mitchtalmadge.com/)
 * Emoji Tools helps users and developers of Android, iOS, and OS X extract, modify, and repackage Emoji fonts.
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
 */

package com.mitchtalmadge.emojitools.gui.dialogs;

import com.mitchtalmadge.emojitools.EmojiTools;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import com.mitchtalmadge.emojitools.EmojiTools;
import com.mitchtalmadge.emojitools.gui.dialogs.dialogcontrollers.OverwriteWarningDialogController;

import java.io.File;
import java.io.IOException;

public class OverwriteWarningDialog {

    private File extractionDirectoryFile;
    private OverwriteWarningDialogController controller;

    private Stage stage;
    private boolean result = false;

    public OverwriteWarningDialog(File extractionDirectoryFile) {
        this.extractionDirectoryFile = extractionDirectoryFile;

        this.stage = new Stage();
        stage.setTitle("Overwrite Warning");
        stage.getIcons().add(EmojiTools.getLogoImage());
        stage.setResizable(false);
        stage.initModality(Modality.APPLICATION_MODAL);

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/GUI/Dialogs/OverwriteWarningDialog.fxml"));
            Parent root = loader.load();

            this.controller = loader.getController();
            controller.setParent(this);
            controller.setExtractionDirectoryFile(extractionDirectoryFile);

            stage.setScene(new Scene(root));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean getResult() {
        stage.setOnShown(e -> EmojiTools.setStageLocationRelativeToMainGui(stage));
        EmojiTools.getLogManager().logInfo("OverwriteWarningDialog displayed.");
        stage.showAndWait();
        return result;
    }

    public void onResultAcquired(boolean result) {
        if(!result)
            EmojiTools.getLogManager().logInfo("OverwriteWarningDialog: User cancelled.");
        this.result = result;
        stage.close();
    }
}
