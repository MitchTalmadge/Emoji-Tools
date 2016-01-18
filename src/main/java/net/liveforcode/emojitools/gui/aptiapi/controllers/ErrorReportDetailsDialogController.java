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
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextArea;
import net.liveforcode.emojitools.gui.aptiapi.ErrorReportDetailsDialog;

public class ErrorReportDetailsDialogController {

    @FXML
    protected TextArea detailsTextArea;

    @FXML
    protected CheckBox includeLogCheckbox;

    @FXML
    protected TextArea logTextArea;

    @FXML
    protected Button okButton;

    protected ErrorReportDetailsDialog parent;

    @FXML
    protected void onOkButtonFired(ActionEvent event) {
        parent.returnToErrorReportDialog();
    }

    public void setParent(ErrorReportDetailsDialog parent) {
        this.parent = parent;
    }

    public void setDetails(String details)
    {
        this.detailsTextArea.setText(details);
    }

    public void setLogText(String logText)
    {
        this.logTextArea.setText(logText);
    }

    public boolean getShouldIncludeLog() {
        return this.includeLogCheckbox.isSelected();
    }

    public void setShouldIncludeLog(boolean shouldIncludeLog) {
        this.includeLogCheckbox.setSelected(shouldIncludeLog);
    }

}
