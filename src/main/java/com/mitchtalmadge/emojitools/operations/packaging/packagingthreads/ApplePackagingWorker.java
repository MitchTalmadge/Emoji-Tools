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

package com.mitchtalmadge.emojitools.operations.packaging.packagingthreads;

import com.mitchtalmadge.emojitools.EmojiTools;
import com.mitchtalmadge.emojitools.gui.dialogs.OperationProgressDialog;
import com.mitchtalmadge.emojitools.operations.Operation;
import com.mitchtalmadge.emojitools.operations.OperationWorker;
import org.python.core.PyList;
import org.python.core.PyType;

import java.io.File;
import java.util.ArrayList;

public class ApplePackagingWorker extends OperationWorker {

    private final File packagingDirectory;
    private final short[] resolutions;

    public ApplePackagingWorker(Operation operation, OperationProgressDialog operationProgressDialog, File packagingDirectory, short[] resolutions) {
        super(operation, operationProgressDialog, true);
        this.packagingDirectory = packagingDirectory;
        this.resolutions = resolutions;
    }

    @Override
    protected Boolean doWork() throws Exception {
        File outputDirectory = new File(EmojiTools.getRootDirectory(), "Output");
        if (!outputDirectory.exists() && !outputDirectory.mkdir()) {
            showErrorDialog("Unable to Create Directory", "Emoji Tools was unable to create a required directory. Does it have permission?");
            return false;
        }

        File originalFont = new File(packagingDirectory, "Original.ttf");
        if (!originalFont.exists()) {
            showErrorDialog("Missing File", "Emoji Tools could not find a required file. Did you remove the Original.ttf file?");
            return false;
        }

        File[] pngFiles = packagingDirectory.listFiles(file -> file.getName().toLowerCase().endsWith(".png"));
        if (pngFiles == null) {
            showErrorDialog("Packaging Failed (Error Code 1)", "An internal error occurred. Please contact the developer for help.");
            return false;
        }

        if (pngFiles.length == 0) {
            showErrorDialog("No Emojis Found!", "No Emojis were found in the selected directory.");
            return false;
        }

        ////////////////////////////// STEP 1: Package. //////////////////////////////

        if (isCancelled())
            return false;

        appendMessageToDialog("Building font... (This can take a while - Please wait)");

        //---- ttx.py ----//

        if (isCancelled())
            return false;

        //Set sys.argv
        ArrayList<String> argvList = new ArrayList<>();
        argvList.add("addSbixImages.py"); //Python Script Name
        argvList.add(originalFont.getAbsolutePath()); //Original font location
        argvList.add(outputDirectory.getAbsolutePath()); //Output directory location

        StringBuilder stringBuilder = new StringBuilder();
        for (short resolution : resolutions) {
            stringBuilder.append(resolution).append(",");
        }
        stringBuilder.deleteCharAt(stringBuilder.length() - 1); //Remove last comma
        argvList.add(new String(stringBuilder)); //Resolutions list

        getJythonHandler().getPySystemState().argv = new PyList(PyType.fromClass(String.class), argvList);

        //Execute
        getJythonHandler().getPythonInterpreter().execfile(getJythonHandler().getTempDirectory().getAbsolutePath() + "/PythonScripts/addSbixImages.py");

        updateProgress(100, 100);
        return true;
    }
}
