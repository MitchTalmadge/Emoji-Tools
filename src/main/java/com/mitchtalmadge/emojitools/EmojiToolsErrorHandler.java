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

package com.mitchtalmadge.emojitools;

import com.mitchtalmadge.emojitools.gui.dialogs.ErrorReportDialog;
import com.mitchtalmadge.emojitools.gui.dialogs.OperationProgressDialog;

public class EmojiToolsErrorHandler {

    private OperationProgressDialog progressDialog;

    public void onErrorOccurred(Thread t, Throwable e) {
        EmojiTools.getLogManager().logSevere("AN ERROR HAS OCCURRED!");
        EmojiTools.getLogManager().logSevere("Thread Name: " + t.getName());
        EmojiTools.getLogManager().logSevere("Exception:\n" + e.getStackTrace());

        boolean sendReport = new ErrorReportDialog().getResult();

        if (sendReport) {
            this.progressDialog = new OperationProgressDialog("Sending Error Report...");
        }
    }

}
