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

package net.liveforcode.emojitools;

import com.AptiTekk2.AptiAPI2.AptiAPI;
import com.AptiTekk2.AptiAPI2.ErrorHandler;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import org.python.core.PyString;
import org.python.core.PySystemState;
import org.python.util.PythonInterpreter;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.ExecutionException;

public class EmojiTools extends Application {

    private static final Image logoImage = new Image(EmojiTools.class.getResourceAsStream("/Images/EmojiToolsLogo.png"));
    private static final AptiAPI aptiAPI = new AptiAPI(new Versioning(), logoImage);
    private static final ErrorHandler errorHandler = aptiAPI.getErrorHandler();

    private static final ArrayList<JythonListener> jythonListenerList = new ArrayList<>();
    private static JythonHandler jythonHandler;

    public static void main(String[] args) {
        System.setProperty("python.cachedir.skip", "false");
        System.setProperty("python.console.encoding", "UTF-8");

        Thread.setDefaultUncaughtExceptionHandler(errorHandler);

        String fontName = null;
        if (args.length > 0)
            fontName = args[0];

        final File font = new File(getRootDirectory() + "/" + fontName);

        new JythonLoader().execute();

        launch(args);

        aptiAPI.checkForUpdates();
    }

    public static void shutdown() {

    }

    public static Image getLogoImage() {
        return logoImage;
    }

    public static void submitError(Thread thread, Throwable throwable) {
        errorHandler.uncaughtException(thread, throwable);
    }

    public static File getRootDirectory() {
        try {
            return new File(EmojiTools.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath()).getParentFile().getAbsoluteFile();
        } catch (URISyntaxException e) {
            submitError(Thread.currentThread(), e);
            return null;
        }
    }

    public static void addJythonListener(JythonListener listener) {
        if (!jythonListenerList.contains(listener))
            jythonListenerList.add(listener);

        if (jythonHandler != null)
            listener.onJythonReady(jythonHandler);
    }

    private static void notifyListenersJythonReady(JythonHandler jythonHandler) {
        EmojiTools.jythonHandler = jythonHandler;

        Iterator<JythonListener> iterator = jythonListenerList.iterator();

        while (iterator.hasNext()) {
            JythonListener listener = iterator.next();
            if (listener != null)
                listener.onJythonReady(jythonHandler);
            else
                iterator.remove();
        }
    }

    @Override
    public void start(Stage stage) throws Exception {
        stage.setTitle(new Versioning().getProgramNameWithVersion());
        stage.setResizable(false);
        stage.getIcons().add(EmojiTools.getLogoImage());

        Parent root = FXMLLoader.load(getClass().getResource("/GUI/MainGUI.fxml"));

        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }

    @Override
    public void stop() throws Exception {
        //TODO: Stop tab operations

        if (jythonHandler != null) {
            jythonHandler.getPythonInterpreter().close();

            if (jythonHandler.getTempDirectory().exists()) {
                try {
                    org.apache.commons.io.FileUtils.deleteDirectory(jythonHandler.getTempDirectory());
                } catch (IOException e) {
                    EmojiTools.submitError(Thread.currentThread(), e);
                }
            }
        }
    }

    public interface JythonListener {
        void onJythonReady(JythonHandler jythonHandler);
    }

    private static class JythonLoader extends SwingWorker<JythonHandler, Void> {
        @Override
        protected JythonHandler doInBackground() throws Exception {
            File tempDirectory = extractScriptsToTempDir();

            //Create Interpreter
            PySystemState pySystemState = new PySystemState();
            PythonInterpreter pythonInterpreter = new PythonInterpreter(null, pySystemState);

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

            return new JythonHandler(pySystemState, pythonInterpreter, tempDirectory);
        }

        private File extractScriptsToTempDir() throws Exception {
            File tempFolder = new File(getRootDirectory().getAbsolutePath() + "/tmp");
            tempFolder.mkdir();

            FileUtils.copyResourcesRecursively(getClass().getResource("/PythonScripts"), tempFolder);
            FileUtils.copyResourcesRecursively(getClass().getResource("/FontTemplates"), tempFolder);

            return tempFolder;
        }

        @Override
        protected void done() {
            try {
                notifyListenersJythonReady(get());
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }
    }

}
