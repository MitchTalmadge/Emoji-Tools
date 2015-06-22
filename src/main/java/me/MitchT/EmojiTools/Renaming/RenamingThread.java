package me.MitchT.EmojiTools.Renaming;

import me.MitchT.EmojiTools.GUI.RenamingDialog;

import java.io.File;

class RenamingThread extends Thread {

    private final File renameFile;
    private final File outputDir;
    private final RenamingManager conversionManager;
    private final RenamingDialog renamingDialog;
    private boolean running = true;


    private int totalFileNum = 0;
    private int currentFileNum = 0;

    public RenamingThread(File renameFile, File outputDir, RenamingManager conversionManager, RenamingDialog renamingDialog) {
        this.renameFile = renameFile;
        this.outputDir = outputDir;
        this.conversionManager = conversionManager;
        this.renamingDialog = renamingDialog;
    }

    @Override
    public void run() {

        File[] files;

        if (renameFile.isDirectory()) {
            files = renameFile.listFiles();
        } else {
            files = new File[]{renameFile};
        }

        totalFileNum = files.length;

        for (File file : files) {
            if (!running) {
                this.renamingDialog.dispose();
                return;
            }
            this.currentFileNum++;
            String newFileName = "";

            if (file.getName().startsWith("uni"))
                newFileName = file.getName().substring(3, file.getName().length());
            else if (file.getName().startsWith("u"))
                newFileName = file.getName().substring(1, file.getName().length());

            System.out.println("Renaming " + file.getName() + " to " + newFileName);
            renamingDialog.appendToStatus("Renaming " + file.getName() + " to " + newFileName);
            updateProgress();

            file.renameTo(new File(file.getParent(), newFileName));
        }

        updateProgress();

        renamingDialog.dispose();
    }

    private void updateProgress() {
        renamingDialog.setProgress((int) (((double) currentFileNum / totalFileNum) * 100));
    }

    public void endRenaming() {
        running = false;
    }
}
