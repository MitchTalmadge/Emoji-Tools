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

package net.liveforcode.EmojiTools.Deletion;

import net.liveforcode.EmojiTools.OldGUI.DeletionDialog;

import java.io.File;

class DeletionThread extends Thread {

    private final File extractionDirectory;
    private final DeletionManager deletionManager;
    private final DeletionDialog deletionDialog;
    private boolean running = true;


    private int totalFileNum = 0;
    private int currentFileNum = 0;

    public DeletionThread(File extractionDirectory, DeletionManager deletionManager, DeletionDialog deletionDialog) {
        super("DeletionThread");
        this.extractionDirectory = extractionDirectory;
        this.deletionManager = deletionManager;
        this.deletionDialog = deletionDialog;
    }

    @Override
    public void run() {

        totalFileNum = countFilesRecursive(totalFileNum, extractionDirectory);

        System.out.println(totalFileNum);

        if (!running) {
            this.deletionDialog.dispose();
            return;
        }

        deleteFilesRecursive(extractionDirectory);

        deletionDialog.dispose();
    }

    private int countFilesRecursive(int fileCount, File dir) {
        fileCount += dir.listFiles().length;
        for (File file : dir.listFiles()) {
            if (!running)
                return 0;
            if (file.isDirectory())
                fileCount = countFilesRecursive(fileCount, file);
        }
        return fileCount;
    }

    private void deleteFilesRecursive(File dir) {
        for (File file : dir.listFiles()) {
            if (!running) {
                return;
            }
            this.currentFileNum++;

            if (file.isDirectory())
                deleteFilesRecursive(file);

            if (!file.equals(extractionDirectory))
                file.delete();

            System.out.println("Deleting " + file.getName());
            deletionDialog.appendToStatus("Deleting " + file.getName());
            updateProgress();
        }
        updateProgress();
    }

    private void updateProgress() {
        deletionDialog.setProgress((int) (((double) currentFileNum / totalFileNum) * 100));
    }

    public void endDeletion() {
        running = false;
    }
}
