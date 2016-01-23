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

package net.liveforcode.emojitools;

import com.aptitekk.aptiapi.AptiAPI;
import com.aptitekk.aptiapi.AptiAPIListener;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.image.Image;
import javafx.stage.Modality;
import javafx.stage.Stage;
import net.liveforcode.emojitools.operations.conversion.ConversionInfo;
import net.liveforcode.emojitools.operations.conversion.ConversionOperation;
import net.liveforcode.emojitools.operations.deletion.DeletionOperation;
import net.liveforcode.emojitools.operations.extraction.ExtractionOperation;
import net.liveforcode.emojitools.operations.packaging.PackagingOperation;
import net.liveforcode.emojitools.operations.renaming.RenamingInfo;
import net.liveforcode.emojitools.operations.renaming.RenamingOperation;
import net.liveforcode.emojitools.operations.resizing.ResizingInfo;
import net.liveforcode.emojitools.operations.resizing.ResizingOperation;
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

public class EmojiTools extends Application implements AptiAPIListener {

    private static final Image logoImage = new Image(EmojiTools.class.getResourceAsStream("/Images/EmojiToolsLogo.png"));
    private static final AptiAPI aptiAPI = new AptiAPI(new Versioning(), logoImage);

    private static final ArrayList<JythonListener> jythonListenerList = new ArrayList<>();
    private static JythonHandler jythonHandler;

    private static Stage mainGuiStage;
    private static LogManager logManager;

    public static void main(String[] args) {
        System.setProperty("python.cachedir.skip", "false");
        System.setProperty("python.console.encoding", "UTF-8");

        try {
            logManager = new LogManager(new File(getRootDirectory(), "logs"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        aptiAPI.setErrorHandler(new EmojiToolsErrorHandler(aptiAPI));
        aptiAPI.setUpdateHandler(new EmojiToolsUpdateHandler());

        new JythonLoader().execute();

        launch(args);
    }

    /**
     * Gets the LogManager instance, which handles logging to files.
     *
     * @return The LogManager instance.
     */
    public static LogManager getLogManager() {
        return logManager;
    }

    /**
     * Gets the logo image used for icons in the GUI.
     *
     * @return The Image object containing the logo.
     */
    public static Image getLogoImage() {
        return logoImage;
    }

    /**
     * Calculates the center X position for a stage based upon the main GUI's X position and the stage's width.
     *
     * @param stageWidth The width of the stage.
     * @return The calculated X position.
     */
    public static int getGuiCenterXPos(double stageWidth) {
        return (int) (mainGuiStage.getX() + (mainGuiStage.getWidth() / 2) - (stageWidth / 2));
    }

    /**
     * Calculates the center Y position for a stage based upon the main GUI's Y position and the stage's height.
     *
     * @param stageHeight The height of the stage.
     * @return The calculated Y position.
     */
    public static int getGuiCenterYPos(double stageHeight) {
        return (int) (mainGuiStage.getY() + (mainGuiStage.getHeight() / 2) - (stageHeight / 2));
    }

    /**
     * Sets the specified stage's location to the center of the main GUI.
     *
     * @param stage The stage to move.
     */
    public static void setStageLocationRelativeToMainGui(Stage stage) {
        stage.setX(getGuiCenterXPos(stage.getWidth()));
        stage.setY(getGuiCenterYPos(stage.getHeight()));
    }

    /**
     * Creates an error report and notifies the user, asking them if they want to send or not, then
     * immediately closes the program.
     *
     * @param throwable The exception thrown.
     */
    public static void submitError(Throwable throwable) {
        aptiAPI.getErrorHandler().uncaughtException(Thread.currentThread(), throwable);
    }

    /**
     * The root directory refers to the directory immediately next to the running jar or exe file.
     *
     * @return The root directory file.
     */
    public static File getRootDirectory() {
        try {
            return new File(EmojiTools.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath()).getParentFile().getAbsoluteFile();
        } catch (URISyntaxException e) {
            submitError(e);
            return null;
        }
    }

    /**
     * Adds a JythonListener, to be notified when Jython is ready to be used.
     *
     * @param listener The listener to notify.
     */
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

    /**
     * Starts an Operation to delete all files within the specified directory with a ProgressDialog.
     *
     * @param deletionDirectory The directory to delete files from.
     * @return True if operation completed successfully, False if unsuccessful or cancelled.
     */
    public static boolean performDeletionOperation(File deletionDirectory) {
        return new DeletionOperation(deletionDirectory).runOperation();
    }

    /**
     * Starts an Operation to extract all emojis within the specified emoji font file with a ProgressDialog.
     *
     * @param fontFile            The emoji font file to extract emojis from.
     * @param extractionDirectory The directory to extract emojis into.
     * @return True if operation completed successfully, False if unsuccessful or cancelled.
     */
    public static boolean performExtractionOperation(File fontFile, File extractionDirectory) {
        return new ExtractionOperation(fontFile, extractionDirectory).runOperation();
    }

    /**
     * Starts an Operation to rename all files within the specified directory with a ProgressDialog.
     *
     * @param renamingDirectory The directory containing emojis to rename.
     * @param renamingInfo      The RenamingInfo object that specifies how renaming should be performed.
     * @return True if operation completed successfully, False if unsuccessful or cancelled.
     */
    public static boolean performRenamingOperation(File renamingDirectory, RenamingInfo renamingInfo) {
        return new RenamingOperation(renamingDirectory, renamingInfo).runOperation();
    }

    /**
     * Starts an Operation to convert all files within the specified directory with a ProgressDialog.
     *
     * @param conversionDirectory The directory containing emojis to convert.
     * @param conversionInfo      The ConversionInfo object that specifies how conversion should be performed.
     * @return True if operation completed successfully, False if unsuccessful or cancelled.
     */
    public static boolean performConversionOperation(File conversionDirectory, ConversionInfo conversionInfo) {
        return new ConversionOperation(conversionDirectory, conversionInfo).runOperation();
    }

    /**
     * Starts an Operation to package all emojis within the specified directory into a new emoji font with a ProgressDialog.
     *
     * @param packagingDirectory The directory containing emojis to package.
     * @return True if operation completed successfully, False if unsuccessful or cancelled.
     */
    public static boolean performPackagingOperation(File packagingDirectory) {
        return new PackagingOperation(packagingDirectory).runOperation();
    }

    /**
     * Starts an Operation to resize all emojis within a source directory into a destination directory.
     *
     * @param sourceDirectory      The directory containing emojis to resize.
     * @param destinationDirectory The directory where the resized emojis should be placed.
     * @param resizingInfo         The ResizingInfo object that specifies how resizing should be performed.
     * @return True if operation completed successfully, False if unsuccessful or cancelled.
     */
    public static boolean performResizingOperation(File sourceDirectory, File destinationDirectory, ResizingInfo resizingInfo) {
        return new ResizingOperation(sourceDirectory, destinationDirectory, resizingInfo).runOperation();
    }


    /**
     * Displays an information dialog with the specified header and message.
     *
     * @param header  The header text. Can be null for no header.
     * @param message The message.
     */
    public static void showInfoDialog(String header, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
        stage.getIcons().add(getLogoImage());
        stage.setOnShown(e -> EmojiTools.setStageLocationRelativeToMainGui(stage));

        if (header != null) {
            alert.setTitle(header);
            alert.setHeaderText(header);
        }
        alert.setContentText(message);

        if (getLogManager() != null)
            getLogManager().logInfo("Alert Shown: " + header + " - " + message);

        alert.initModality(Modality.APPLICATION_MODAL);
        alert.showAndWait();
    }

    /**
     * Displays a warning dialog with the specified header and message.
     *
     * @param header  The header text. Can be null for no header.
     * @param message The message.
     */
    public static void showWarningDialog(String header, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
        stage.getIcons().add(getLogoImage());
        stage.setOnShown(e -> EmojiTools.setStageLocationRelativeToMainGui(stage));

        if (header != null) {
            alert.setTitle(header);
            alert.setHeaderText(header);
        }
        alert.setContentText(message);

        if (getLogManager() != null)
            getLogManager().logWarning("Alert Shown: " + header + " - " + message);

        alert.initModality(Modality.APPLICATION_MODAL);
        alert.showAndWait();
    }

    /**
     * Displays an error dialog with the specified header and message.
     *
     * @param header  The header text. Can be null for no header.
     * @param message The message.
     */
    public static void showErrorDialog(String header, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
        stage.getIcons().add(getLogoImage());
        stage.setOnShown(e -> EmojiTools.setStageLocationRelativeToMainGui(stage));

        if (header != null) {
            alert.setTitle(header);
            alert.setHeaderText(header);
        }
        alert.setContentText(message);

        if (getLogManager() != null)
            getLogManager().logSevere("Alert Shown: " + header + " - " + message);

        alert.initModality(Modality.APPLICATION_MODAL);
        alert.showAndWait();
    }

    @Override
    public void start(Stage stage) throws Exception {
        aptiAPI.addAPIListener(this);

        try {
            mainGuiStage = stage;
            stage.setTitle(new Versioning().getProgramNameWithVersion());
            stage.setResizable(false);
            stage.getIcons().add(EmojiTools.getLogoImage());

            Parent root = FXMLLoader.load(getClass().getResource("/GUI/MainGUI.fxml"));

            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();

            getLogManager().logInfo("Main GUI Displayed.");
        } catch (Exception e) {
            submitError(e);
        }

        aptiAPI.checkForUpdates();
    }

    @Override
    public void stop() throws Exception {
        try {
            getLogManager().logInfo("Emoji Tools is stopping...");
            if (jythonHandler != null) {
                getLogManager().logInfo("Cleaning up jythonHandler...");
                jythonHandler.getPythonInterpreter().close();

                if (jythonHandler.getTempDirectory().exists()) {
                    org.apache.commons.io.FileUtils.deleteDirectory(jythonHandler.getTempDirectory());
                }
            }
            getLogManager().logInfo("Emoji Tools stopped gracefully.");
        } catch (Exception e) {
            submitError(e);
        }
    }

    @Override
    public void aptiApiInfo(String message) {
        getLogManager().logInfo(message);
    }

    @Override
    public void aptiApiError(String message) {
        getLogManager().logSevere(message);
    }

    @Override
    public void shutdown() {
        Platform.exit();
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
