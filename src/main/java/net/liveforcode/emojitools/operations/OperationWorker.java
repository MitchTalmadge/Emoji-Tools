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

package net.liveforcode.emojitools.operations;

import javafx.application.Platform;
import javafx.concurrent.Task;
import net.liveforcode.emojitools.EmojiTools;
import net.liveforcode.emojitools.JythonHandler;
import net.liveforcode.emojitools.gui.dialogs.OperationProgressDialog;

import java.util.concurrent.ExecutionException;

public abstract class OperationWorker extends Task<Boolean> implements EmojiTools.JythonListener {

    private String threadName;
    private Operation operation;
    private OperationProgressDialog operationProgressDialog;
    private boolean requireJython;
    private JythonHandler jythonHandler;

    private String messageBlock = "";

    public OperationWorker(Operation operation, OperationProgressDialog operationProgressDialog, boolean requireJython) {
        this.operation = operation;
        this.operationProgressDialog = operationProgressDialog;
        this.requireJython = requireJython;
        this.threadName = getClass().getSimpleName() + "Thread";
        operationProgressDialog.addCloseListener(() -> this.cancel(false));
        operationProgressDialog.bindProgressToProperty(this.progressProperty());
        operationProgressDialog.bindMessagesToProperty(this.messageProperty());
    }

    public final void executeWorker() {
        if (requireJython) {
            appendMessageToDialog("Loading Scripts, Please Wait...");
            setProgressIndeterminate();
            EmojiTools.addJythonListener(this);
        } else {
            new Thread(this).start();
        }
        this.operationProgressDialog.display();
    }

    @Override
    protected Boolean call() throws Exception {
        Thread.currentThread().setName(threadName);
        try {
            return doWork();
        } catch (Exception e) {
            EmojiTools.submitError(e);
            return false;
        }
    }

    protected abstract Boolean doWork() throws Exception;

    protected final void setProgressIndeterminate() {
        this.updateProgress(-1, 100);
    }

    /**
     * Appends a message to the ProgressDialog. Should be called from within doWork() method.
     *
     * @param message The message to append.
     */
    protected final void appendMessageToDialog(String message) {
        messageBlock += message + "\n";
        this.updateMessage(messageBlock);

        EmojiTools.getLogManager().logInfo(message);
    }

    /**
     * Shows an error dialog on the JavaFX Application Thread
     */
    protected final void showErrorDialog(String header, String message) {
        Platform.runLater(() -> EmojiTools.showErrorDialog(header, message));
    }

    /**
     * Called when the operation completes.
     */
    @Override
    protected final void done() {
        Platform.runLater(() -> operationProgressDialog.close());
        try {
            if (isCancelled()) {
                System.out.println("Operation " + threadName + " has been cancelled by user.");
                this.operation.done(false);
            } else {
                boolean successfullyCompleted = this.get();
                System.out.println("Operation " + threadName + " was " + (successfullyCompleted ? "" : "not ") + "completed successfully.");
                this.operation.done(successfullyCompleted);
            }
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }

    /**
     * This method will only be called if jythonRequired == true.
     *
     * @param jythonHandler the JythonHandler instance.
     */
    @Override
    public final void onJythonReady(JythonHandler jythonHandler) {
        this.jythonHandler = jythonHandler;
        updateProgress(0, 100);
        new Thread(this).start();
    }

    /**
     * Returns the JythonHandler if jythonRequired was set to true before execution.
     *
     * @return a JythonHandler instance if exists, otherwise null.
     */
    protected final JythonHandler getJythonHandler() {
        return this.jythonHandler;
    }
}
