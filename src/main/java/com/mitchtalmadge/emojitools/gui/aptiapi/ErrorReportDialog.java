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

package com.mitchtalmadge.emojitools.gui.aptiapi;

import com.aptitekk.aptiapi.AptiAPI;
import com.aptitekk.aptiapi.ErrorReport;
import com.mitchtalmadge.emojitools.EmojiTools;
import com.mitchtalmadge.emojitools.gui.aptiapi.controllers.ErrorReportDialogController;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import com.mitchtalmadge.emojitools.EmojiTools;
import com.mitchtalmadge.emojitools.gui.aptiapi.controllers.ErrorReportDialogController;

import java.io.IOException;

public class ErrorReportDialog {

    private final Stage stage;
    private ErrorReportDialogController controller;
    private ErrorReport errorReport;
    private AptiAPI aptiAPI;
    private boolean result;

    public ErrorReportDialog(ErrorReport errorReport) {
        this.errorReport = errorReport;

        this.stage = new Stage();
        stage.setTitle("Emoji Tools has Crashed!");
        stage.getIcons().add(EmojiTools.getLogoImage());
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setResizable(false);

        stage.setOnCloseRequest(e -> {
            setResult(false);
            e.consume();
        });

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/GUI/AptiAPI/ErrorReportDialog.fxml"));
            Parent root = loader.load();

            this.controller = loader.getController();
            controller.setParent(this);

            stage.setScene(new Scene(root));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean getResult() {
        stage.setOnShown(e -> EmojiTools.setStageLocationRelativeToMainGui(stage));
        EmojiTools.getLogManager().logInfo("ErrorReportDialog displayed.");
        stage.showAndWait();
        return result;
    }

    public void setResult(boolean result) {
        this.result = result;
        stage.close();
    }

    public ErrorReport getErrorReport() {
        return errorReport;
    }

    public AptiAPI getAptiAPI() {
        return aptiAPI;
    }

}
