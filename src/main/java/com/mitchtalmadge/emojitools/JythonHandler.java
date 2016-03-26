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

package com.mitchtalmadge.emojitools;

import org.python.core.PyString;
import org.python.core.PySystemState;
import org.python.util.PythonInterpreter;

import java.io.File;

public class JythonHandler {

    private final File tempDirectory;
    private PySystemState pySystemState;
    private PythonInterpreter pythonInterpreter;

    public JythonHandler(File tempDirectory) {
        this.tempDirectory = tempDirectory;
        begin();
    }

    private void begin() {
        //Create Interpreter
        pySystemState = new PySystemState();
        pythonInterpreter = new PythonInterpreter(null, pySystemState);

        //Set encoding to UTF8
        pythonInterpreter.exec("import sys\n" +
                "reload(sys)\n" +
                "sys.setdefaultencoding('UTF8')\n" +
                "print('Encoding: '+sys.getdefaultencoding())");

        //Set Outputs
        pythonInterpreter.setOut(System.out);
        pythonInterpreter.setErr(System.err);

        //Set sys.path
        String pythonScriptsPath = tempDirectory.getAbsolutePath() + "/PythonScripts";
        pySystemState.path.append(new PyString(pythonScriptsPath));
    }

    public void close() {
        pySystemState.close();
        pythonInterpreter.close();
        pySystemState = null;
        pythonInterpreter = null;
    }

    public PySystemState getPySystemState() {
        if (pySystemState == null)
            begin();
        return pySystemState;
    }

    public PythonInterpreter getPythonInterpreter() {
        if (pythonInterpreter == null)
            begin();
        return pythonInterpreter;
    }

    public File getTempDirectory() {
        return tempDirectory;
    }
}
