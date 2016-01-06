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

package net.liveforcode.emojitools.gui.dialogs;

import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import net.liveforcode.emojitools.EmojiTools;
import net.liveforcode.emojitools.gui.dialogs.dialogcontrollers.OperationProgressDialogController;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class OperationProgressDialog {

    private Stage stage;
    private OperationProgressDialogController controller;

    private List<DialogCloseListener> closeListeners = new ArrayList<>();

    public OperationProgressDialog(String headerText) {
        this.stage = new Stage();
        stage.setTitle(headerText);
        stage.getIcons().add(EmojiTools.getLogoImage());
        stage.setResizable(false);
        stage.initModality(Modality.APPLICATION_MODAL);

        stage.setOnCloseRequest(e -> {
            cancel();
            e.consume();
        });

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/GUI/Dialogs/OperationProgressDialog.fxml"));
            Parent root = loader.load();

            this.controller = loader.getController();
            controller.setParent(this);
            controller.setHeaderText(headerText);

            stage.setScene(new Scene(root));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void addCloseListener(DialogCloseListener listener) {
        if (!closeListeners.contains(listener))
            closeListeners.add(listener);
    }

    public void display() {
        stage.showAndWait();
    }

    public void cancel() {
        closeListeners.forEach(DialogCloseListener::onDialogClosing);
        close();
    }

    public void close() {
        stage.close();
    }

    public void bindProgressToProperty(ReadOnlyDoubleProperty property) {
        controller.bindProgressToProperty(property);
    }

    public void bindMessagesToProperty(ReadOnlyStringProperty property) {
        controller.bindMessagesToProperty(property);
    }

    public interface DialogCloseListener {
        void onDialogClosing();
    }

}
