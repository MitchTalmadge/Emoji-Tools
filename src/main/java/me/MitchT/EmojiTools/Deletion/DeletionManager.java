package me.MitchT.EmojiTools.Deletion;

import me.MitchT.EmojiTools.GUI.DeletionDialog;
import me.MitchT.EmojiTools.GUI.EmojiToolsGUI;
import me.MitchT.EmojiTools.OperationManager;

import java.io.File;

public class DeletionManager extends OperationManager {

    private final EmojiToolsGUI gui;

    private final DeletionThread deletionThread;


    public DeletionManager(File extractionDirectory, EmojiToolsGUI gui, DeletionDialog deletionDialog) {
        this.gui = gui;

        this.deletionThread = new DeletionThread(extractionDirectory, this, deletionDialog);
    }

    public void showMessageDialog(String message) {
        this.gui.showMessageDialog(message);
    }

    @Override
    public void start() {
        deletionThread.start();
    }

    @Override
    public void stop() {
        if (deletionThread != null && deletionThread.isAlive()) {
            deletionThread.endDeletion();
        }
    }
}
