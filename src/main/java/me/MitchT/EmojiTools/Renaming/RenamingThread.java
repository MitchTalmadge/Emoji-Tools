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

package me.MitchT.EmojiTools.Renaming;

import me.MitchT.EmojiTools.GUI.RenamingDialog;

import java.io.File;

class RenamingThread extends Thread {

    private final File renameFile;
    private final RenamingManager renamingManager;
    private final RenamingDialog renamingDialog;

    private final boolean[] prefixButtons;
    private final boolean[] capitalizationButtons;

    private boolean running = true;


    private int totalFileNum = 0;
    private int currentFileNum = 0;

    public RenamingThread(File renameFile, RenamingManager renamingManager, RenamingDialog renamingDialog, boolean[] prefixButtons, boolean[] capitalizationButtons) {
        super("RenamingThread");
        this.renameFile = renameFile;
        this.renamingManager = renamingManager;
        this.renamingDialog = renamingDialog;

        this.prefixButtons = prefixButtons;
        this.capitalizationButtons = capitalizationButtons;
    }

    @Override
    public void run() {

        File[] files;

        if (renameFile.isDirectory()) {
            files = renameFile.listFiles();
        } else {
            files = new File[]{renameFile};
        }

        totalFileNum = files.length;

        for (File file : files) {
            if (file.isDirectory())
                continue;
            if (!file.getName().toLowerCase().endsWith(".png"))
                continue;
            if (!running) {
                this.renamingDialog.dispose();
                return;
            }

            this.currentFileNum++;
            String newFileName = file.getName();

            if (prefixButtons != null) {
                if (prefixButtons[1])
                    newFileName = stripPrefixes(file);
                else if (prefixButtons[2])
                    newFileName = changePrefix(file, "uni");
                else if (prefixButtons[3])
                    newFileName = changePrefix(file, "u");
            }

            if (capitalizationButtons != null) {
                if (capitalizationButtons[1]) {
                    newFileName = capitalize((newFileName.equals("")) ? file.getName() : newFileName, (prefixButtons != null && prefixButtons[1]) || !capitalizationButtons[3]);
                } else if (capitalizationButtons[2]) {
                    newFileName = newFileName.toLowerCase();
                }
            }

            System.out.println("Renaming " + file.getName() + " to " + newFileName);
            renamingDialog.appendToStatus("Renaming " + file.getName() + " to " + newFileName);
            updateProgress();

            file.renameTo(new File(file.getParent(), newFileName));
        }

        updateProgress();

        renamingDialog.dispose();
    }

    private String stripPrefixes(File file) {
        if (file.getName().startsWith("uni") || file.getName().startsWith("UNI"))
            return file.getName().substring(3, file.getName().length());
        else if (file.getName().startsWith("u") || file.getName().startsWith("U"))
            return file.getName().substring(1, file.getName().length());
        return file.getName();
    }

    private String changePrefix(File file, String newPrefix) {
        if (file.getName().startsWith("uni") || file.getName().startsWith("UNI"))
            return newPrefix + file.getName().substring(3, file.getName().length());
        else if (file.getName().startsWith("u") || file.getName().startsWith("U"))
            return newPrefix + file.getName().substring(1, file.getName().length());
        else
            return newPrefix + file.getName();
    }

    private String capitalize(String fileName, boolean capitalizePrefix) {
        String capitalized = "";
        if (!capitalizePrefix) {
            if (fileName.startsWith("uni") || fileName.startsWith("UNI"))
                capitalized = "uni" + fileName.substring(3, fileName.length()).toUpperCase();
            else if (fileName.startsWith("u") || fileName.startsWith("U"))
                capitalized = "u" + fileName.substring(1, fileName.length()).toUpperCase();
            if (capitalized.length() > 0) {
                capitalized = capitalized.substring(0, capitalized.length() - 3) + capitalized.substring(capitalized.length() - 3, capitalized.length()).toLowerCase();
                return capitalized;
            }
        }
        capitalized = fileName.toUpperCase();
        capitalized = capitalized.substring(0, capitalized.length() - 3) + capitalized.substring(capitalized.length() - 3, capitalized.length()).toLowerCase();
        return capitalized;
    }

    private void updateProgress() {
        renamingDialog.setProgress((int) (((double) currentFileNum / totalFileNum) * 100));
    }

    public void endRenaming() {
        running = false;
    }
}
