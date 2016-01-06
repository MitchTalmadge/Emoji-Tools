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

package net.liveforcode.emojitools.operations.deletion;

import net.liveforcode.emojitools.gui.dialogs.OperationProgressDialog;
import net.liveforcode.emojitools.operations.Operation;
import net.liveforcode.emojitools.operations.OperationWorker;

import java.io.File;

public class DeletionWorker extends OperationWorker {

    private int totalFileNum = 0;
    private int currentFileNum = 0;
    private File deletionDirectory;

    public DeletionWorker(Operation operation, OperationProgressDialog operationProgressDialog, File deletionDirectory) {
        super(operation, operationProgressDialog, false);
        this.deletionDirectory = deletionDirectory;
    }

    @Override
    protected Boolean doWork() throws Exception {
        totalFileNum = countFilesRecursive(totalFileNum, deletionDirectory);

        System.out.println("# of Files to Delete: " + totalFileNum);

        if (isCancelled()) {
            return false;
        }

        deleteFilesRecursive(deletionDirectory);

        return true;
    }

    private int countFilesRecursive(int fileCount, File dir) {
        fileCount += dir.listFiles().length;
        for (File file : dir.listFiles()) {
            if (isCancelled())
                break;
            if (file.isDirectory())
                fileCount = countFilesRecursive(fileCount, file);
        }
        return fileCount;
    }

    private void deleteFilesRecursive(File dir) {
        for (File file : dir.listFiles()) {
            if (isCancelled()) {
                return;
            }
            this.currentFileNum++;

            if (file.isDirectory())
                deleteFilesRecursive(file);

            if (!file.equals(deletionDirectory))
                file.delete();

            appendMessageToDialog("Deleting " + file.getName());
            updateProgress(currentFileNum, totalFileNum);
        }
        updateProgress(currentFileNum, totalFileNum);
    }

}
