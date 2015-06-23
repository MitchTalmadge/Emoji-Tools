package me.MitchT.EmojiTools.Deletion;

import me.MitchT.EmojiTools.GUI.DeletionDialog;

import java.io.File;

class DeletionThread extends Thread {

    private final File extractionDirectory;
    private final DeletionManager conversionManager;
    private final DeletionDialog deletionDialog;
    private boolean running = true;


    private int totalFileNum = 0;
    private int currentFileNum = 0;

    public DeletionThread(File extractionDirectory, DeletionManager conversionManager, DeletionDialog deletionDialog) {
        this.extractionDirectory = extractionDirectory;
        this.conversionManager = conversionManager;
        this.deletionDialog = deletionDialog;
    }

    @Override
    public void run() {

        File[] files = extractionDirectory.listFiles();

        totalFileNum = files.length;

        for (File file : files) {
            if (!running) {
                this.deletionDialog.dispose();
                return;
            }
            this.currentFileNum++;

            file.delete();

            System.out.println("Deleting " + file.getName());
            deletionDialog.appendToStatus("Deleting " + file.getName());
            updateProgress();
        }

        updateProgress();

        deletionDialog.dispose();
    }

    private void updateProgress() {
        deletionDialog.setProgress((int) (((double) currentFileNum / totalFileNum) * 100));
    }

    public void endRenaming() {
        running = false;
    }
}
