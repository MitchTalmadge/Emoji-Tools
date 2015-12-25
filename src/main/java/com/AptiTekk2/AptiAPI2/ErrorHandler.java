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

package com.AptiTekk2.AptiAPI2;

import com.AptiTekk2.AptiAPI2.GUI.ErrorReportDialog;

public class ErrorHandler implements Thread.UncaughtExceptionHandler {

    private final AptiAPI aptiAPI;

    public ErrorHandler(AptiAPI aptiAPI) {
        this.aptiAPI = aptiAPI;
    }

    @Override
    public void uncaughtException(Thread t, Throwable e) {
        ErrorReport errorReport = new ErrorReport(t, e);
        errorReport.setVersion(aptiAPI.getVersioningDetails().getVersionString());

        System.out.println("ERROR OCCURRED!");
        System.out.println("Thread Name: " + t.getName());
        System.out.println("Exception:\n" + errorReport.getStackTrace());

        new ErrorReportDialog(aptiAPI, errorReport).setVisible(true);
    }
}
