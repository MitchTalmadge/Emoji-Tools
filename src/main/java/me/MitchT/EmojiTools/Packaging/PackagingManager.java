package me.MitchT.EmojiTools.Packaging;

import me.MitchT.EmojiTools.GUI.EmojiToolsGUI;
import me.MitchT.EmojiTools.GUI.PackagingDialog;
import me.MitchT.EmojiTools.OperationManager;

import java.io.File;

public class PackagingManager extends OperationManager {

    private final EmojiToolsGUI gui;

    private final PackagingThread packagingThread;
    private final PackagingDialog packagingDialog;

    public PackagingManager(EmojiToolsGUI gui, File pngDirectory, PackagingDialog packagingDialog) {
        this.gui = gui;
        this.packagingDialog = packagingDialog;

        this.packagingThread = new PackagingThread(gui, pngDirectory, this, packagingDialog);
    }

    @Override
    public void start() {
        this.packagingThread.start();
    }

    @Override
    public void stop() {
        if (packagingThread != null && packagingThread.isAlive()) {
            packagingThread.endPackaging();
        }
    }

}
