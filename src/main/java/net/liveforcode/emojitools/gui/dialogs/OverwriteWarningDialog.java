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

package net.liveforcode.emojitools.gui.dialogs;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import net.liveforcode.emojitools.EmojiTools;
import net.liveforcode.emojitools.gui.dialogs.dialogcontrollers.OverwriteWarningDialogController;

import java.io.File;
import java.io.IOException;

public class OverwriteWarningDialog implements OverwriteWarningDialogController.ResultListener {

    private File extractionDirectoryFile;

    private Stage stage;
    private boolean result = false;

    public OverwriteWarningDialog(File extractionDirectoryFile) {
        this.extractionDirectoryFile = extractionDirectoryFile;
    }

    public boolean getResult() {
        this.stage = new Stage();
        stage.setTitle("Overwrite Warning");
        stage.getIcons().add(EmojiTools.getLogoImage());

        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("/GUI/Dialogs/OverwriteWarningDialog.fxml"));
            Parent root = loader.load();
            OverwriteWarningDialogController controller = loader.getController();
            controller.setExtractionDirectoryFile(extractionDirectoryFile);

            controller.setResultListener(this);

            stage.setScene(new Scene(root));
            stage.setResizable(false);
            stage.initModality(Modality.APPLICATION_MODAL);

            stage.showAndWait();

            return result;
        } catch (IOException e) {
            e.printStackTrace();
            return result;
        }
    }

    @Override
    public void onResultAcquired(boolean result) {
        this.result = result;
        stage.close();
    }
}
