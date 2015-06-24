package me.MitchT.EmojiTools.Renaming;

import me.MitchT.EmojiTools.GUI.EmojiToolsGUI;
import me.MitchT.EmojiTools.GUI.RenamingDialog;
import me.MitchT.EmojiTools.OperationManager;

import java.io.File;

public class RenamingManager extends OperationManager {

    private final EmojiToolsGUI gui;

    private final RenamingThread renamingThread;


    public RenamingManager(File renameFile, EmojiToolsGUI gui, RenamingDialog renamingDialog, boolean[] prefixButtons, boolean[] capitalizationButtons) {
        this.gui = gui;

        this.renamingThread = new RenamingThread(renameFile, this, renamingDialog, prefixButtons, capitalizationButtons);
    }

    public void showMessageDialog(String message) {
        this.gui.showMessageDialog(message);
    }

    @Override
    public void start() {
        renamingThread.start();
    }

    @Override
    public void stop() {
        if (renamingThread != null && renamingThread.isAlive()) {
            renamingThread.endRenaming();
        }
    }
}
