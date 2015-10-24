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

package me.MitchT.EmojiTools;

import com.AptiTekk.AptiAPI.AptiAPI;
import com.AptiTekk.AptiAPI.AptiAPIListener;
import me.MitchT.EmojiTools.GUI.ErrorReportDialog;

import javax.swing.*;

public class ErrorHandler implements Thread.UncaughtExceptionHandler, AptiAPIListener {

    private final AptiAPI aptiAPI;

    public ErrorHandler() {
        this.aptiAPI = new AptiAPI(Versioning.APTIAPI_PROJECT_ID);
        this.aptiAPI.addAPIListener(this);
    }

    @Override
    public void uncaughtException(Thread t, Throwable e) {
        ErrorReport errorReport = new ErrorReport(t, e);

        System.out.println("ERROR OCCURRED!");
        System.out.println("Thread Name: " + t.getName());
        System.out.println("Exception:\n" + errorReport.getStackTrace());

        new ErrorReportDialog(this, null, errorReport).setVisible(true);
    }

    public void sendErrorReport(ErrorReport errorReport) {
        this.aptiAPI.sendErrorReport(errorReport.generateReport());
    }

    @Override
    public void displayInfo(final String message) {
        JOptionPane.showMessageDialog(null, message, "Info", JOptionPane.INFORMATION_MESSAGE);
    }

    @Override
    public void displayError(final String message) {
        JOptionPane.showMessageDialog(null, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    @Override
    public void shutdown() {
    }
}
