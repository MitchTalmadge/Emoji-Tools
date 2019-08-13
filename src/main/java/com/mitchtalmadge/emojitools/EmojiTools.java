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

package com.mitchtalmadge.emojitools;

import com.mitchtalmadge.emojitools.gui.dialogs.ErrorReportDialog;
import com.mitchtalmadge.emojitools.operations.conversion.ConversionInfo;
import com.mitchtalmadge.emojitools.operations.conversion.ConversionOperation;
import com.mitchtalmadge.emojitools.operations.deletion.DeletionOperation;
import com.mitchtalmadge.emojitools.operations.extraction.ExtractionOperation;
import com.mitchtalmadge.emojitools.operations.packaging.PackagingInfo;
import com.mitchtalmadge.emojitools.operations.packaging.PackagingOperation;
import com.mitchtalmadge.emojitools.operations.renaming.RenamingInfo;
import com.mitchtalmadge.emojitools.operations.renaming.RenamingOperation;
import com.mitchtalmadge.emojitools.operations.resizing.ResizingInfo;
import com.mitchtalmadge.emojitools.operations.resizing.ResizingOperation;
import com.mitchtalmadge.emojitools.operations.splitting.SplittingOperation;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.image.Image;
import javafx.stage.Modality;
import javafx.stage.Stage;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.ExecutionException;

public class EmojiTools extends Application implements Thread.UncaughtExceptionHandler {

    private static final Image logoImage = new Image(EmojiTools.class.getResourceAsStream("/Images/EmojiToolsLogo.png"));

    private static final ArrayList<JythonListener> jythonListenerList = new ArrayList<>();
    private static JythonHandler jythonHandler;

    private static Stage mainGuiStage;
    private static LogManager logManager;

    static void init(String[] args) {
        System.setProperty("python.cachedir.skip", "false");
        System.setProperty("python.console.encoding", "UTF-8");

        try {
            logManager = new LogManager(new File(getRootDirectory(), "logs"));
        } catch (IOException e) {
            e.printStackTrace();
        }

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
     * Creates an error report and notifies the user, then
     * immediately closes the program.
     *
     * @param e The exception thrown.
     */
    public static void submitError(Throwable e) {
        submitError(Thread.currentThread(), e);
    }

    /**
     * Creates an error report and notifies the user, then
     * immediately closes the program.
     *
     * @param t The origin thread of the exception.
     * @param e The exception thrown.
     */
    public static void submitError(Thread t, Throwable e) {
        EmojiTools.getLogManager().logSevere("AN ERROR HAS OCCURRED!");
        EmojiTools.getLogManager().logSevere("Thread Name: " + t);

        StringWriter stringWriter = new StringWriter();
        e.printStackTrace(new PrintWriter(stringWriter));
        EmojiTools.getLogManager().logSevere("Exception:\n" + stringWriter.toString());

        new ErrorReportDialog().display();
    }

    @Override
    public void uncaughtException(Thread t, Throwable e) {
        submitError(t, e);
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
     * @param packagingInfo      The PackagingInfo object that specifies how resizing should be performed.
     * @return True if operation completed successfully, False if unsuccessful or cancelled.
     */
    public static boolean performPackagingOperation(File packagingDirectory, PackagingInfo packagingInfo) {
        return new PackagingOperation(packagingDirectory, packagingInfo).runOperation();
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
     * Starts an Operation to split a ttc emoji font file into two ttf emoji font files with a ProgressDialog.
     *
     * @param fontFile            The emoji font file to split.
     * @param extractionDirectory The directory to split the fonts into.
     * @return True if operation completed successfully, False if unsuccessful or cancelled.
     */
    public static boolean performSplittingOperation(File fontFile, File extractionDirectory) {
        return new SplittingOperation(fontFile, extractionDirectory).runOperation();
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
        try {
            Thread.setDefaultUncaughtExceptionHandler(this);

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

    public interface JythonListener {
        void onJythonReady(JythonHandler jythonHandler);
    }

    private static class JythonLoader extends SwingWorker<JythonHandler, Void> {
        @Override
        protected JythonHandler doInBackground() throws Exception {
            return new JythonHandler(extractScriptsToTempDir());
        }

        private File extractScriptsToTempDir() throws Exception {
            File tempFolder = new File(getRootDirectory().getAbsolutePath() + "/tmp");
            if (tempFolder.exists())
                org.apache.commons.io.FileUtils.deleteDirectory(tempFolder);
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
