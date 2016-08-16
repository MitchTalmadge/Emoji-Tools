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

package com.mitchtalmadge.emojitools.operations;

public abstract class Operation {

    boolean successfullyCompleted = false;

    protected abstract OperationWorker getWorker();

    /**
     * Runs the operation.
     *
     * @return True if operation is completed successfully, False if unsuccessful or cancelled.
     */
    public boolean runOperation() {

        OperationWorker worker = getWorker();

        if (worker == null) {
            System.out.println("Tried to perform operation, but no worker was given!");
            return false;
        }

        worker.executeWorker();

        //Cleanup
        worker = null;
        System.gc();

        return successfullyCompleted;
    }

    public void done(boolean successfullyCompleted) {
        this.successfullyCompleted = successfullyCompleted;
    }
}
