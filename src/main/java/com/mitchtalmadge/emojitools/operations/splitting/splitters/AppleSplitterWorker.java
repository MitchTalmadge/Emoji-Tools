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

package com.mitchtalmadge.emojitools.operations.splitting.splitters;

import com.mitchtalmadge.emojitools.gui.dialogs.OperationProgressDialog;
import com.mitchtalmadge.emojitools.operations.Operation;
import com.mitchtalmadge.emojitools.operations.OperationWorker;
import org.python.core.PyList;
import org.python.core.PyType;

import java.io.File;
import java.util.ArrayList;

public class AppleSplitterWorker extends OperationWorker {

    private final File fontFile;
    private final File extractionDirectory;

    public AppleSplitterWorker(Operation operation, OperationProgressDialog operationProgressDialog, File fontFile, File extractionDirectory) {
        super(operation, operationProgressDialog, true);
        this.fontFile = fontFile;
        this.extractionDirectory = extractionDirectory;
    }

    @Override
    protected Boolean doWork() throws Exception {
        //---- ttx.py ----//
        setProgressIndeterminate();
        appendMessageToDialog("Splitting Emoji Font... Please wait...");
        appendMessageToDialog("****************");
        appendMessageToDialog("**** This may take upwards of 10 minutes.\n**** Thank you for your patience.");
        appendMessageToDialog("****************");

        for (int i = 0; i < 2; i++) {
            appendMessageToDialog("Decompiling font " + (i + 1) + " of 2 from TTC file...");

            //Set sys.argv
            ArrayList<String> argvList = new ArrayList<>();
            argvList.add("package.py");                             //Python Script Name
            argvList.add("-y");                                     //Choose font file in ttc
            argvList.add("" + i);                                   //Number (0/1)
            argvList.add("-o");                                     //Output flag
            argvList.add(extractionDirectory.getAbsolutePath()
                    + "/" + "font_" + (i + 1) + ".ttx");            //Output ttx path
            argvList.add(fontFile.getAbsolutePath());               //Input ttc path

            getJythonHandler().getPySystemState().argv = new PyList(PyType.fromClass(String.class), argvList);

            if (isCancelled())
                return false;

            //Execute
            getJythonHandler().getPythonInterpreter().execfile(getJythonHandler().getTempDirectory().getAbsolutePath()
                    + "/PythonScripts/package.py");

            getJythonHandler().getPythonInterpreter().cleanup();

            appendMessageToDialog("Compiling Font " + (i + 1) + " of 2 into new TTF file...");

            //Set sys.argv
            argvList = new ArrayList<>();
            argvList.add("package.py");                                                                     //Python Script Name
            argvList.add("-o");                                                                             //Output flag
            argvList.add(extractionDirectory.getAbsolutePath()
                    + "/" + "font_" + (i + 1) + ".ttf");                                                    //Output ttf path
            argvList.add(new File(extractionDirectory, "font_" + (i + 1) + ".ttx").getAbsolutePath());      //Input ttx path

            getJythonHandler().getPySystemState().argv = new PyList(PyType.fromClass(String.class), argvList);

            if (isCancelled())
                return false;

            //Execute
            getJythonHandler().getPythonInterpreter().execfile(getJythonHandler().getTempDirectory().getAbsolutePath()
                    + "/PythonScripts/package.py");

            getJythonHandler().getPythonInterpreter().cleanup();

            new File(extractionDirectory, "font_" + (i + 1) + ".ttx").delete();

            appendMessageToDialog("Font " + (i + 1) + " of 2 is done splitting.");
        }

        return true;
    }
}
