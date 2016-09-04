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

package com.mitchtalmadge.emojitools.gui.dialogs.dialogcontrollers;

import com.mitchtalmadge.emojitools.EmojiTools;
import com.sun.glass.ui.Application;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.stage.*;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

public class ErrorReportDialogController {

    @FXML
    protected Button openLogDirButton;

    @FXML
    protected Button openGitHubButton;

    @FXML
    protected Button closeButton;

    @FXML
    protected void onOpenLogDirButtonFired(ActionEvent event) {
        try {
            Desktop.getDesktop().open(EmojiTools.getLogManager().getLogFile().getParentFile());
        } catch (IOException ignored) {
        }
    }

    @FXML
    protected void onOpenGitHubButtonFired(ActionEvent event) {
        try {
            Desktop.getDesktop().browse(new URI("https://github.com/MitchTalmadge/Emoji-Tools/issues"));
        } catch (IOException | URISyntaxException ignored) {
        }
    }

    @FXML
    protected void onCloseButtonFired(ActionEvent event) {
        closeButton.getScene().getWindow().hide();
        Platform.exit();
    }

}
