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

package net.liveforcode.emojitools.operations.renaming;

import net.liveforcode.emojitools.gui.dialogs.OperationProgressDialog;
import net.liveforcode.emojitools.operations.Operation;
import net.liveforcode.emojitools.operations.OperationWorker;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RenamingWorker extends OperationWorker {

    private final File renamingDirectory;
    private final RenamingInfo renamingInfo;

    private int currentFileNum = 0;

    public RenamingWorker(Operation operation, OperationProgressDialog operationProgressDialog, File renamingDirectory, RenamingInfo renamingInfo) {
        super(operation, operationProgressDialog, false);
        this.renamingDirectory = renamingDirectory;
        this.renamingInfo = renamingInfo;
    }

    @Override
    protected Boolean doWork() throws Exception {
        File[] files = renamingDirectory.listFiles();

        if (files == null)
            return false;

        for (File file : files) {
            if (file.isDirectory())
                continue;
            if (!file.getName().toLowerCase().endsWith(".png"))
                continue;
            if (isCancelled()) {
                return false;
            }

            this.currentFileNum++;
            String newFileName = file.getName();

            switch (renamingInfo.getPrefixOption()) {
                case RenamingInfo.PREFIX_DONT_CHANGE:
                    break;
                case RenamingInfo.PREFIX_REMOVE_ALL:
                    newFileName = changePrefix(file.getName(), "");
                    break;
                case RenamingInfo.PREFIX_SET_UNI:
                    newFileName = changePrefix(file.getName(), "uni");
                    break;
                case RenamingInfo.PREFIX_SET_U:
                    newFileName = changePrefix(file.getName(), "u");
                    break;
            }

            switch (renamingInfo.getCaseOption()) {
                case RenamingInfo.CASE_DONT_CHANGE:
                    break;
                case RenamingInfo.CASE_UPPER:
                case RenamingInfo.CASE_LOWER:
                    newFileName = changeCase(newFileName, renamingInfo.getCaseOption() == RenamingInfo.CASE_UPPER, renamingInfo.isOppositePrefixCase());
                    break;
            }

            appendMessageToDialog("Renaming " + file.getName() + " to " + newFileName);
            updateProgress(currentFileNum, files.length);

            boolean renamed = file.renameTo(new File(file.getParent(), newFileName));
            if (!renamed)
                return false;
        }

        updateProgress(currentFileNum, files.length);

        return true;
    }

    /**
     * Changes the prefix of the unicode file name.
     * @param fileName The file name to modify.
     * @param newPrefix The new prefix for the file name. ("uni" or "u")
     * @return The modified file name.
     */
    private String changePrefix(String fileName, String newPrefix) {
        String[] fileNameSplit = fileName.split("\\.");
        String[] unicodeNames = fileNameSplit[0].split("_");
        StringBuilder stringBuilder = new StringBuilder();

        for (int i = 0; i < unicodeNames.length; i++) {
            String name = unicodeNames[i].replaceAll("(?i)(_?)(uni?|u?)([A-Fa-f0-9]+)", "$1" + newPrefix + "$3");
            if (i > 0)
                stringBuilder.append("_").append(name);
            else
                stringBuilder.append(name);
        }

        StringBuilder newFileNameBuilder = new StringBuilder(stringBuilder.toString());
        for (int i = 1; i < fileNameSplit.length; i++) {
            newFileNameBuilder.append(".").append(fileNameSplit[i]);
        }
        return newFileNameBuilder.toString();
    }

    /**
     * Changes the case of the unicode file name.
     * @param fileName The file name to modify.
     * @param upperCase Setting to true will result in uppercase; false will result in lowercase.
     * @param oppositePrefixCase Setting to true will result in a prefix with the opposite case of that specified in upperCase parameter.
     * @return The modified file name.
     */
    private String changeCase(String fileName, boolean upperCase, boolean oppositePrefixCase) {
        String[] fileNameSplit = fileName.split("\\.");
        String[] unicodeNames = fileNameSplit[0].split("_");
        StringBuilder stringBuilder = new StringBuilder();

        Pattern pattern = Pattern.compile("(?i)(_?)(uni?|u?)([A-Fa-f0-9]+)");
        for (int i = 0; i < unicodeNames.length; i++) {
            Matcher matcher = pattern.matcher(unicodeNames[i]);
            StringBuffer stringBuffer = new StringBuffer();
            while (matcher.find()) {
                if (oppositePrefixCase)
                    matcher.appendReplacement(stringBuffer, matcher.group(1) + (upperCase ? matcher.group(2).toLowerCase() : matcher.group(2).toUpperCase()) + (upperCase ? matcher.group(3).toUpperCase() : matcher.group(3).toLowerCase()));
                else
                    matcher.appendReplacement(stringBuffer, matcher.group(1) + (upperCase ? (matcher.group(2) + matcher.group(3)).toUpperCase() : (matcher.group(2) + matcher.group(3)).toLowerCase()));
            }
            matcher.appendTail(stringBuffer);

            if (i > 0)
                stringBuilder.append("_").append(stringBuffer.toString());
            else
                stringBuilder.append(stringBuffer.toString());
        }

        StringBuilder newFileNameBuilder = new StringBuilder(stringBuilder.toString());
        for (int i = 1; i < fileNameSplit.length; i++) {
            newFileNameBuilder.append(".").append(fileNameSplit[i]);
        }
        return newFileNameBuilder.toString();
    }

}
