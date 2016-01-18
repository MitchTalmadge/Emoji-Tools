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

import com.aptitekk.aptiapi.ErrorReport;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import net.liveforcode.emojitools.EmojiTools;
import net.liveforcode.emojitools.gui.aptiapi.ErrorReportDetailsDialog;
import net.liveforcode.emojitools.gui.aptiapi.ErrorReportDialog;

public class ErrorReportDialogController {

    @FXML
    protected TextArea descriptionTextArea;

    @FXML
    protected TextField nameField;

    @FXML
    protected Button viewDetailsButton;

    @FXML
    protected TextField emailField;

    @FXML
    protected Button dontSendReportButton;

    @FXML
    protected Button sendReportButton;

    private ErrorReportDialog parent;
    private boolean logAttached = true;

    @FXML
    protected void onSendReportButtonFired(ActionEvent event) {
        populateReport(logAttached);
        parent.setResult(true);
    }

    @FXML
    protected void onDontSendReportButtonFired(ActionEvent event) {
        parent.setResult(false);
    }

    @FXML
    protected void onViewDetailsButtonFired(ActionEvent event) {
        populateReport(true);
        new ErrorReportDetailsDialog(this, parent.getErrorReport()).display();
    }

    private void populateReport(boolean logAttached)
    {
        ErrorReport errorReport = parent.getErrorReport();
        String description = descriptionTextArea.getText();
        description = description.replaceAll("\\n+", "\\n");
        errorReport.setDescription(description);
        errorReport.setName(nameField.getText());
        errorReport.setEmail(emailField.getText());

        if (logAttached) {
            errorReport.setLogFile(EmojiTools.getLogManager().getLogFile());
        }
    }

    public void setParent(ErrorReportDialog parent) {
        this.parent = parent;
    }

    public boolean isLogAttached() {
        return logAttached;
    }

    public void setLogAttached(boolean logAttached) {
        this.logAttached = logAttached;
    }
}
