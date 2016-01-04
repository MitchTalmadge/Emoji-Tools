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

package net.liveforcode.emojitools.gui.dialogs.dialogcontrollers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;

import java.awt.*;
import java.io.File;
import java.io.IOException;

public class OperationFinishedDialogController {

    @FXML
    protected Button openOutputDirectoryButton;

    @FXML
    protected Label headerLabel;

    @FXML
    protected Button okButton;

    @FXML
    protected Label descriptionLabel;

    @FXML
    protected TextArea outputDirectoryArea;

    protected File outputDirectory;

    @FXML
    protected void onOkButtonFired(ActionEvent event) {
        ((Stage) this.okButton.getScene().getWindow()).close();
    }

    @FXML
    protected void onOpenOutputDirectoryButtonFired(ActionEvent event) {
        try {
            Desktop.getDesktop().browse(outputDirectory.toURI());
        } catch (IOException e) {
            e.printStackTrace();
        }
        ((Stage) this.openOutputDirectoryButton.getScene().getWindow()).close();
    }

    public void setHeaderText(String headerText) {
        this.headerLabel.setText(headerText);
    }

    public void setDescriptionText(String descriptionText) {
        this.descriptionLabel.setText(descriptionText);
    }

    public void setOutputDirectory(File outputDirectory) {
        this.outputDirectory = outputDirectory;
        this.outputDirectoryArea.setText(outputDirectory.getAbsolutePath());
    }
}
