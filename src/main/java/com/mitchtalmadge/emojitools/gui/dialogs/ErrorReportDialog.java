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
import com.mitchtalmadge.emojitools.gui.dialogs.dialogcontrollers.ErrorReportDialogController;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;

public class ErrorReportDialog {

    private final Stage stage;

    public ErrorReportDialog() {
        this.stage = new Stage();
        stage.setTitle("Emoji Tools has Crashed!");
        stage.getIcons().add(EmojiTools.getLogoImage());
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setResizable(false);

        stage.setOnCloseRequest(e -> {
            Platform.exit();
        });

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/GUI/Dialogs/ErrorReportDialog.fxml"));
            Parent root = loader.load();

            stage.setScene(new Scene(root));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void display() {
        stage.setOnShown(e -> EmojiTools.setStageLocationRelativeToMainGui(stage));
        EmojiTools.getLogManager().logInfo("ErrorReportDialog displayed.");
        stage.showAndWait();
    }

}
