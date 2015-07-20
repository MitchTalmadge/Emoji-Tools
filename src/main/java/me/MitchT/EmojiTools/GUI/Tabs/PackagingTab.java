package me.MitchT.EmojiTools.GUI.Tabs;

import me.MitchT.EmojiTools.EmojiTools;
import me.MitchT.EmojiTools.GUI.EmojiToolsGUI;
import me.MitchT.EmojiTools.GUI.FinishedDialog;
import me.MitchT.EmojiTools.GUI.PackagingDialog;
import me.MitchT.EmojiTools.GUI.RenamingDialog;
import me.MitchT.EmojiTools.Packaging.PackagingManager;
import me.MitchT.EmojiTools.Renaming.RenamingManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;

public class PackagingTab extends OperationTab implements ActionListener {
    //Output Type Finals
    public static final int ANDROID = 0;
    public static final int IOS = 1;
    public static final int OSX = 2;
    private final EmojiToolsGUI gui;
    private final String defaultFileNameFieldText = "Folder Name";
    private JPanel contentPane;
    private JTextField fileNameField;
    private JButton browseButton;
    private JRadioButton outputRadioButton1;
    private JRadioButton outputRadioButton2;
    private JButton startPackagingButton;
    private JButton openRootDirectoryButton;
    private JRadioButton outputRadioButton3;
    private File packagingFile;

    public PackagingTab(EmojiToolsGUI gui) {
        this.gui = gui;

        this.setLayout(new BorderLayout());
        this.add(this.contentPane, BorderLayout.CENTER);

        this.browseButton.addActionListener(this);

        this.outputRadioButton1.addActionListener(this);
        this.outputRadioButton1.addActionListener(this);

        this.openRootDirectoryButton.addActionListener(this);
        this.startPackagingButton.addActionListener(this);
    }

    private void startPackaging() {
        this.cancelled = false;

        if (!this.packagingFile.exists()) {
            this.packagingFile = null;
            this.fileNameField.setText(defaultFileNameFieldText);
            this.updateStartButton();
            return;
        }

        int outputType;
        if (this.outputRadioButton1.isSelected())
            outputType = ANDROID;
        else if (this.outputRadioButton2.isSelected())
            outputType = IOS;
        else
            outputType = OSX;

        RenamingDialog renamingDialog = new RenamingDialog(this, this.gui.getLogo());
        this.currentOperationManager = new RenamingManager(this.packagingFile, this.gui, renamingDialog, new boolean[]{false, false, true, false}, new boolean[]{false, false, true, true});
        currentOperationManager.start();
        renamingDialog.setVisible(true);

        if (!cancelled) {
            PackagingDialog packagingDialog = new PackagingDialog(this, this.gui.getLogo());
            this.currentOperationManager = new PackagingManager(this.gui, this.packagingFile, packagingDialog, outputType);
            currentOperationManager.start();
            packagingDialog.setVisible(true);
        }

        new FinishedDialog(this.gui, this.gui.getLogo(), "Emoji Packaging Complete!", "Your Packaged Emoji Font can be found in:", new File(EmojiTools.getRootDirectory(), "Output")).setVisible(true);
    }

    @Override
    public void stopOperations() {
        if (this.currentOperationManager != null)
            this.currentOperationManager.stop();
        this.cancelled = true;
    }

    private void openFileChooser() {
        this.fileNameField.setText(defaultFileNameFieldText);

        JFileChooser fileChooser = new JFileChooser(EmojiTools.getRootDirectory());
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        int returnVal = fileChooser.showOpenDialog(this);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            this.fileNameField.setText(fileChooser.getSelectedFile().getName());
            this.packagingFile = fileChooser.getSelectedFile();
        }
        updateStartButton();
    }

    private void updateStartButton() {
        if (!this.fileNameField.getText().equals(defaultFileNameFieldText))
            this.startPackagingButton.setEnabled(true);
        else
            this.startPackagingButton.setEnabled(false);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource().equals(this.browseButton))
            openFileChooser();
        else if (e.getSource().equals(this.openRootDirectoryButton)) {
            Desktop desktop = Desktop.getDesktop();
            try {
                desktop.open(EmojiTools.getRootDirectory());
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        } else if (e.getSource().equals(this.startPackagingButton)) {
            startPackaging();
        }
        updateStartButton();
    }
}
