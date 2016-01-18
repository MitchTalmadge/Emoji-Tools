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

package com.aptitekk.aptiapi;

import javafx.application.Platform;

public abstract class AptiAPIErrorHandler implements Thread.UncaughtExceptionHandler {

    private final AptiAPI aptiAPI;

    public AptiAPIErrorHandler(AptiAPI aptiAPI) {
        this.aptiAPI = aptiAPI;
    }

    @Override
    public void uncaughtException(Thread t, Throwable e) {
        ErrorReport errorReport = onErrorOccurred(t, e);
        if(errorReport != null)
        {
            aptiAPI.sendErrorReport(errorReport);
            shutDown();
        }
    }

    /**
     * Called when an error occurs in the program. Determines whether a report should be sent or not.
     * @param t The thread where the error occurred.
     * @param e The throwable (exception).
     * @return An ErrorReport object if one should be sent. Null otherwise.
     */
    public abstract ErrorReport onErrorOccurred(Thread t, Throwable e);

    /**
     * Called after the error has been handled and the program should close.
     * All program cleanup operations should take place when this method is called,
     * and Platform.exit() or System.exit(int) should be called within this method.
     */
    public abstract void shutDown();
}
