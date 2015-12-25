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

package net.liveforcode.emojitools.renaming;

import net.liveforcode.emojitools.oldgui.RenamingDialog;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
                    newFileName = changePrefix(file.getName(), "");
                else if (prefixButtons[2])
                    newFileName = changePrefix(file.getName(), "uni");
                else if (prefixButtons[3])
                    newFileName = changePrefix(file.getName(), "u");
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

    private String changePrefix(String fileName, String newPrefix) {
        String[] fileNameSplit = fileName.split("\\.");
        String[] unicodeNames = fileNameSplit[0].split("_");
        StringBuilder stringBuilder = new StringBuilder();

        for(int i = 0; i < unicodeNames.length; i++)
        {
            String name = unicodeNames[i].replaceAll("(?i)(_?)(uni?|u?)([A-Fa-f0-9]+)", "$1"+newPrefix+"$3");
            if(i > 0)
                stringBuilder.append("_").append(name);
            else
                stringBuilder.append(name);
        }

        StringBuilder newFileNameBuilder = new StringBuilder(stringBuilder.toString());
        for(int i = 1; i < fileNameSplit.length; i++)
        {
            newFileNameBuilder.append(".").append(fileNameSplit[i]);
        }
        return newFileNameBuilder.toString();
    }

    private String capitalize(String fileName, boolean capitalizePrefix) {
        String[] fileNameSplit = fileName.split("\\.");
        String[] unicodeNames = fileNameSplit[0].split("_");
        StringBuilder stringBuilder = new StringBuilder();

        Pattern pattern = Pattern.compile("(?i)(_?)(uni?|u?)([A-Fa-f0-9]+)");
        for(int i = 0; i < unicodeNames.length; i++)
        {
            Matcher matcher = pattern.matcher(unicodeNames[i]);
            StringBuffer stringBuffer = new StringBuffer();
            while(matcher.find())
            {
                if(capitalizePrefix)
                    matcher.appendReplacement(stringBuffer, matcher.group(1) + (matcher.group(2) + matcher.group(3)).toUpperCase());
                else
                    matcher.appendReplacement(stringBuffer, matcher.group(1) + matcher.group(2) + matcher.group(3).toUpperCase());
            }
            matcher.appendTail(stringBuffer);

            if(i > 0)
                stringBuilder.append("_").append(stringBuffer.toString());
            else
                stringBuilder.append(stringBuffer.toString());
        }

        StringBuilder newFileNameBuilder = new StringBuilder(stringBuilder.toString());
        for(int i = 1; i < fileNameSplit.length; i++)
        {
            newFileNameBuilder.append(".").append(fileNameSplit[i]);
        }
        return newFileNameBuilder.toString();
    }

    private void updateProgress() {
        renamingDialog.setProgress((int) (((double) currentFileNum / totalFileNum) * 100));
    }

    public void endRenaming() {
        running = false;
    }
}
