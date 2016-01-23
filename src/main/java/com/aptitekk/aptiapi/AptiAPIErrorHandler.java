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
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.ReadOnlyStringProperty;

public abstract class AptiAPIErrorHandler implements Thread.UncaughtExceptionHandler {

    private final AptiAPI aptiAPI;

    public AptiAPIErrorHandler(AptiAPI aptiAPI) {
        this.aptiAPI = aptiAPI;
    }

    @Override
    public void uncaughtException(Thread t, Throwable e) {
        try {
            Platform.runLater(() -> {
                ErrorReport errorReport = onErrorOccurred(t, e);
                if (errorReport != null) {
                    aptiAPI.sendErrorReport(errorReport);
                } else
                    shutDown();
            });
        } catch (IllegalStateException ex) {
            ErrorReport errorReport = onErrorOccurred(t, e);
            if (errorReport != null) {
                aptiAPI.sendErrorReport(errorReport);
            } else
                shutDown();
        }
    }

    /**
     * Called when an error occurs in the program. Determines whether a report should be sent or not.
     *
     * @param t The thread where the error occurred.
     * @param e The throwable (exception).
     * @return An ErrorReport object if one should be sent. Null otherwise.
     */
    public abstract ErrorReport onErrorOccurred(Thread t, Throwable e);

    /**
     * Called before report sending has started, to allow for binding the task properties.
     *
     * @param progressProperty This property contains the current progress of sending.
     * @param messageProperty  This property contains any messages sent during sending.
     */
    public abstract void bindProperties(ReadOnlyDoubleProperty progressProperty, ReadOnlyStringProperty messageProperty);

    /**
     * Called when report sending has started.
     */
    public abstract void onSendingStarted();

    /**
     * Called after sending has completed.
     *
     * @param completedSuccessfully Whether sending completed successfully or not.
     */
    public abstract void onSendingComplete(boolean completedSuccessfully);

    /**
     * Called after the error has been handled and the program should close.
     * All program cleanup operations should take place when this method is called,
     * and Platform.exit() or System.exit(int) should be called within this method.
     */
    public abstract void shutDown();
}
