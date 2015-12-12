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

package net.liveforcode.EmojiTools;

import org.python.core.PySystemState;
import org.python.util.PythonInterpreter;

import java.io.File;

public class JythonHandler {

    private final PySystemState pySystemState;
    private final PythonInterpreter pythonInterpreter;
    private final File scriptDirectory;

    public JythonHandler(PySystemState pySystemState, PythonInterpreter pythonInterpreter, File scriptDirectory) {
        this.pySystemState = pySystemState;
        this.pythonInterpreter = pythonInterpreter;
        this.scriptDirectory = scriptDirectory;
    }

    public PySystemState getPySystemState() {
        return pySystemState;
    }

    public PythonInterpreter getPythonInterpreter() {
        return pythonInterpreter;
    }

    public File getScriptDirectory() {
        return scriptDirectory;
    }
}
