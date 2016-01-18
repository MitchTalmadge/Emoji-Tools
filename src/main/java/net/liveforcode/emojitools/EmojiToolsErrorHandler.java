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
import net.liveforcode.emojitools.gui.aptiapi.ErrorReportDialog;

public class EmojiToolsErrorHandler extends AptiAPIErrorHandler {

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

        if (sendReport)
            return errorReport;
        return null;
    }

    @Override
    public void shutDown() {
        Platform.exit();
    }

}
