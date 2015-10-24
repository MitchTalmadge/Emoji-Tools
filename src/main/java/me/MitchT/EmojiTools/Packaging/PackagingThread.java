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

package me.MitchT.EmojiTools.Packaging;

import me.MitchT.EmojiTools.ConsoleManager;
import me.MitchT.EmojiTools.EmojiTools;
import me.MitchT.EmojiTools.FileUtils;
import me.MitchT.EmojiTools.GUI.EmojiToolsGUI;
import me.MitchT.EmojiTools.GUI.PackagingDialog;
import me.MitchT.EmojiTools.GUI.Tabs.PackagingTab;
import org.python.core.PyList;
import org.python.core.PyString;
import org.python.core.PySystemState;
import org.python.core.PyType;
import org.python.util.PythonInterpreter;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

class PackagingThread extends Thread implements ConsoleManager.ConsoleListener {

    private final EmojiToolsGUI gui;
    private final File pngDirectory;
    private final PackagingManager packagingManager;
    private final PackagingDialog packagingDialog;
    private final int outputType;
    String[] vowels = {"a", "e", "i", "o", "u"};
    private boolean running = true;

    public PackagingThread(EmojiToolsGUI gui, File pngDirectory, PackagingManager packagingManager, PackagingDialog packagingDialog, int outputType) {
        super("PackagingThread");
        this.gui = gui;
        this.pngDirectory = pngDirectory;
        this.packagingManager = packagingManager;
        this.packagingDialog = packagingDialog;
        this.outputType = outputType;
    }

    @Override
    public void run() {
        if (outputType == PackagingTab.ANDROID) { //TODO: Implement iOS and OSX Emoji Fonts
            gui.getConsoleManager().addConsoleListener(this);

            File outputDirectory = new File(EmojiTools.getRootDirectory(), "Output");
            if (!outputDirectory.exists())
                outputDirectory.mkdir();

            packagingDialog.setIndeterminate(true);

            packagingDialog.appendToStatus("Compiling Scripts... (This can take a minute. Please Wait...)");

            PySystemState systemState = new PySystemState();
            PythonInterpreter pythonInterpreter = new PythonInterpreter(null, systemState);

            pythonInterpreter.exec("import sys\n" +
                    "reload(sys)\n" +
                    "sys.setdefaultencoding('UTF8')\n" +
                    "print('Encoding: '+sys.getdefaultencoding())"); //Set encoding to UTF8

            //Set Outputs
            pythonInterpreter.setOut(System.out);
            pythonInterpreter.setErr(System.err);

            try {
                packagingDialog.setIndeterminate(false);

                packagingDialog.appendToStatus("Extracting Scripts...");

                File tempFolder = extractToTemp();

                //Set sys.path
                String pythonScriptsPath;

                pythonScriptsPath = tempFolder.getAbsolutePath() + "/PythonScripts";

                systemState.path.append(new PyString(pythonScriptsPath));

                //Options.verbose = Py.DEBUG;

                //---- add_glyphs.py ----//

                packagingDialog.setProgress(25);

                packagingDialog.appendToStatus("Running add_glyphs.py...");

                //Set sys.argv
                String fontTemplatePath;
                fontTemplatePath = tempFolder.getAbsolutePath() + "/FontTemplates/NotoColorEmoji.tmpl.ttx";

                ArrayList<String> argvList = new ArrayList<>();
                argvList.add("add_glyphs.py");                                      //Python Script Name
                argvList.add(fontTemplatePath);                                     //Template Path
                argvList.add(tempFolder.getAbsolutePath() + "/NotoColorEmoji.ttx"); //Output ttx path
                argvList.add(pngDirectory.getAbsolutePath() + "/uni");              //Prefix Path

                systemState.argv = new PyList(PyType.fromClass(String.class), argvList);

                /*pythonInterpreter.exec("import sys\n" +
                        "print sys.path\n" +
                        "print sys.argv");*/

                if (!running)
                    return;

                //Execute
                pythonInterpreter.execfile(tempFolder.getAbsolutePath() + "/PythonScripts/add_glyphs.py");

                //---- package.py ----//

                packagingDialog.setProgress(50);

                packagingDialog.appendToStatus("Running package.py...");

                //Set sys.argv
                argvList = new ArrayList<>();
                argvList.add("package.py");                                                 //Python Script Name
                argvList.add("-o");                                                         //Output flag
                argvList.add(tempFolder.getAbsolutePath() + "/NotoColorEmoji.empty.ttf");   //Output empty ttf path
                argvList.add(tempFolder.getAbsolutePath() + "/NotoColorEmoji.ttx");         //ttx path

                systemState.argv = new PyList(PyType.fromClass(String.class), argvList);

                if (!running)
                    return;

                //Execute
                pythonInterpreter.execfile(tempFolder.getAbsolutePath() + "/PythonScripts/package.py");

                //---- emoji_builder.py.py ----//

                packagingDialog.setProgress(75);

                packagingDialog.appendToStatus("Running emoji_builder.py...");

                //Set sys.argv
                argvList = new ArrayList<>();
                argvList.add("emoji_builder.py");                                           //Python Script Name
                argvList.add(tempFolder.getAbsolutePath() + "/NotoColorEmoji.empty.ttf");   //Empty ttf path
                argvList.add(outputDirectory.getAbsolutePath() + "/NotoColorEmoji.ttf");    //Output ttf path
                argvList.add(pngDirectory.getAbsolutePath() + "/uni");                      //Prefix Path

                systemState.argv = new PyList(PyType.fromClass(String.class), argvList);

                if (!running)
                    return;

                //Execute
                pythonInterpreter.execfile(tempFolder.getAbsolutePath() + "/PythonScripts/emoji_builder.py");

                packagingDialog.setProgress(100);

            } catch (Exception e) {
                EmojiTools.submitError(Thread.currentThread(), e);
            } finally {
                pythonInterpreter.close();
                gui.getConsoleManager().removeConsoleListener(this);

                if (new File(EmojiTools.getRootDirectory(), "tmp").exists()) {
                    try {
                        org.apache.commons.io.FileUtils.deleteDirectory(new File(EmojiTools.getRootDirectory(), "tmp"));
                    } catch (IOException e) {
                        EmojiTools.submitError(Thread.currentThread(), e);
                    }
                }

                packagingDialog.dispose();
            }
        }
    }

    private File extractToTemp() throws Exception {
        File tempFolder = new File(EmojiTools.getRootDirectory().getAbsolutePath() + "/tmp");
        tempFolder.mkdir();

        FileUtils.copyResourcesRecursively(getClass().getResource("/PythonScripts"), tempFolder);
        FileUtils.copyResourcesRecursively(getClass().getResource("/FontTemplates"), tempFolder);

        return tempFolder;
    }

    public void endPackaging() {
        running = false;
    }

    @Override
    public void write(String message) {
        this.packagingDialog.writeToStatus(message);
    }
}
