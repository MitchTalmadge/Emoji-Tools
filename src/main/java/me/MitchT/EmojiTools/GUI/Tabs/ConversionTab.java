package me.MitchT.EmojiTools.GUI.Tabs;

import me.MitchT.EmojiTools.Conversion.ConversionManager;
import me.MitchT.EmojiTools.EmojiTools;
import me.MitchT.EmojiTools.GUI.ConversionDialog;
import me.MitchT.EmojiTools.GUI.EmojiToolsGUI;
import me.MitchT.EmojiTools.OperationManager;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;

public class ConversionTab extends OperationTab implements ActionListener {
    private final EmojiToolsGUI gui;
    private JPanel contentPane;
    private JTextField fileNameField;
    private JButton browseButton;
    private JRadioButton conversionRadioButton1;
    private JRadioButton conversionRadioButton2;
    private JButton startRenamingButton;
    private JButton openRootDirectoryButton;

    private OperationManager currentOperationManager;
    private boolean cancelled;
    private File conversionFile;

    public ConversionTab(EmojiToolsGUI gui) {
        this.gui = gui;

        this.setLayout(new BorderLayout());
        this.add(this.contentPane, BorderLayout.CENTER);

        this.browseButton.addActionListener(this);

        this.conversionRadioButton1.addActionListener(this);
        this.conversionRadioButton1.addActionListener(this);

        this.openRootDirectoryButton.addActionListener(this);
        this.startRenamingButton.addActionListener(this);
    }

    private void startConversion() {
        this.cancelled = false;

        ConversionDialog conversionDialog = new ConversionDialog(this, this.gui.getLogo());
        this.currentOperationManager = new ConversionManager(this.conversionFile, this.gui, conversionDialog, this.conversionRadioButton1.isSelected());
        currentOperationManager.start();
        conversionDialog.setVisible(true);

        this.gui.showMessageDialog("Emoji Conversion Complete! All Done! :)");
    }

    @Override
    public void stopOperations() {
        if (this.currentOperationManager != null)
            this.currentOperationManager.stop();
        this.cancelled = true;
    }

    private void openFileChooser() {
        this.fileNameField.setText("File Name");

        JFileChooser fileChooser = new JFileChooser(EmojiTools.getRootDirectory());
        FileNameExtensionFilter filter = new FileNameExtensionFilter("Folder or PNG File (*.png)", "png");
        fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        fileChooser.setFileFilter(filter);
        int returnVal = fileChooser.showOpenDialog(this);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            this.fileNameField.setText(fileChooser.getSelectedFile().getName());
            this.conversionFile = fileChooser.getSelectedFile();
        }
        updateStartButton();
    }

    private void updateStartButton() {
        if (!this.fileNameField.getText().equals("File Name"))
            this.startRenamingButton.setEnabled(true);
        else
            this.startRenamingButton.setEnabled(false);
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
        } else if (e.getSource().equals(this.startRenamingButton)) {
            startConversion();
        }
        updateStartButton();
    }
}
