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

package net.liveforcode.emojitools.gui.aptiapi.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.web.WebView;
import net.liveforcode.emojitools.gui.aptiapi.UpdateNoticeDialog;

public class UpdateNoticeDialogController {

    @FXML
    protected Label headerLabel;

    @FXML
    protected WebView changeLogView;

    @FXML
    protected Button goToDownloadsButton;

    @FXML
    protected Button remindMeLaterButton;

    private UpdateNoticeDialog parent;
    private String downloadUrl;

    @FXML
    protected void onGoToDownloadsButtonFired(ActionEvent event) {

    }

    @FXML
    protected void onRemindMeLaterButtonFired(ActionEvent event) {

    }

    public void setParent(UpdateNoticeDialog parent) {
        this.parent = parent;
    }

    public void setHeaderLabelText(String headerLabelText) {
        this.headerLabel.setText(headerLabelText);
    }

    public void setChangeLogText(String changeLogText) {
        this.changeLogView.getEngine().loadContent(changeLogText);
    }

    public void setDownloadUrl(String downloadUrl) {
        this.downloadUrl = downloadUrl;
    }
}
