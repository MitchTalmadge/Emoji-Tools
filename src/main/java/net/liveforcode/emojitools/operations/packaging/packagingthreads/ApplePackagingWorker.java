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

package net.liveforcode.emojitools.operations.packaging.packagingthreads;

import net.liveforcode.emojitools.EmojiTools;
import net.liveforcode.emojitools.gui.dialogs.OperationProgressDialog;
import net.liveforcode.emojitools.operations.Operation;
import net.liveforcode.emojitools.operations.OperationWorker;
import org.python.core.PyList;
import org.python.core.PyType;

import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;

public class ApplePackagingWorker extends OperationWorker {

    private final File packagingDirectory;

    public ApplePackagingWorker(Operation operation, OperationProgressDialog operationProgressDialog, File packagingDirectory) {
        super(operation, operationProgressDialog, true);
        this.packagingDirectory = packagingDirectory;
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

        //Copy Script
        File script = new File(getJythonHandler().getTempDirectory().getAbsolutePath() + "/PythonScripts/addSbixImages.py");
        if (script.exists()) {
            File newScriptLocation = new File(outputDirectory, "addSbixImages.py");
            if (newScriptLocation.exists()) {
                if (!newScriptLocation.delete()) {
                    showErrorDialog("Could not Delete File!", "Emoji Tools could not delete a file that must be deleted. Does it have permission?");
                    return false;
                } else
                    Files.copy(script.toPath(), new File(outputDirectory, "addSbixImages.py").toPath());
            }
        }

        //Set sys.argv
        ArrayList<String> argvList = new ArrayList<>();
        argvList.add("addSbixImages.py"); //Python Script Name
        argvList.add(originalFont.getAbsolutePath()); //Original font location
        argvList.add(outputDirectory.getAbsolutePath()); //Output directory location

        getJythonHandler().getPySystemState().argv = new PyList(PyType.fromClass(String.class), argvList);

        //Execute
        getJythonHandler().getPythonInterpreter().execfile(outputDirectory.getAbsolutePath() + "/addSbixImages.py");

        updateProgress(100, 100);
        return true;
    }
}
