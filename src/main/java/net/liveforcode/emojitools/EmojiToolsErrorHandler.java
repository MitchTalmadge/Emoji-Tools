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

package net.liveforcode.emojitools;

import com.aptitekk.aptiapi.AptiAPI;
import com.aptitekk.aptiapi.AptiAPIErrorHandler;
import com.aptitekk.aptiapi.ErrorReport;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.ReadOnlyStringProperty;
import net.liveforcode.emojitools.gui.aptiapi.ErrorReportDialog;
import net.liveforcode.emojitools.gui.dialogs.OperationProgressDialog;

public class EmojiToolsErrorHandler extends AptiAPIErrorHandler {

    private OperationProgressDialog progressDialog;

    public EmojiToolsErrorHandler(AptiAPI aptiAPI) {
        super(aptiAPI);
    }

    @Override
    public ErrorReport onErrorOccurred(Thread t, Throwable e) {
        ErrorReport errorReport = new ErrorReport(t, e);
        errorReport.setVersion(new Versioning().getVersionString());

        EmojiTools.getLogManager().logSevere("AN ERROR HAS OCCURRED!");
        EmojiTools.getLogManager().logSevere("Thread Name: " + t.getName());
        EmojiTools.getLogManager().logSevere("Exception:\n" + errorReport.getStackTrace());

        boolean sendReport = new ErrorReportDialog(errorReport).getResult();

        if (sendReport) {
            this.progressDialog = new OperationProgressDialog("Sending Error Report...");
            return errorReport;
        }
        return null;
    }

    @Override
    public void bindProperties(ReadOnlyDoubleProperty progressProperty, ReadOnlyStringProperty messageProperty) {
        //TODO: Allow for Cancelling
        progressDialog.bindProgressToProperty(progressProperty);
        progressDialog.bindMessagesToProperty(messageProperty);
    }

    @Override
    public void onSendingStarted() {
        progressDialog.display();
    }

    @Override
    public void onSendingComplete(boolean completedSuccessfully) {
        progressDialog.close();
        if (completedSuccessfully)
            EmojiTools.showInfoDialog("Error Report Sent!", "We have received your error report. Thank you!");
        else
            EmojiTools.showErrorDialog("Error Report Could Not be Sent.", "We were unable to receive your error report. Sorry for the inconvenience.");
        shutDown();
    }

    @Override
    public void shutDown() {
        Platform.exit();
    }

}
