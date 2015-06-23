package me.MitchT.EmojiTools.GUI.Tabs;

import me.MitchT.EmojiTools.EmojiTools;
import me.MitchT.EmojiTools.GUI.EmojiToolsGUI;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;

public class RenamingTab extends OperationTab implements ActionListener {
    private final EmojiToolsGUI gui;

    private JPanel contentPane;
    private JTextField fileNameField;
    private JButton browseButton;
    private JRadioButton captializationRadioButton1;
    private JRadioButton captializationRadioButton2;
    private JRadioButton captializationRadioButton3;
    private JRadioButton prefixesRadioButton1;
    private JRadioButton prefixesRadioButton2;
    private JButton startRenamingButton;
    private JButton openRootDirectoryButton;
    private JRadioButton prefixesRadioButton3;
    private JRadioButton prefixesRadioButton4;
    private JCheckBox capitalizationCheckBox1;

    private File fontFile;

    public RenamingTab(EmojiToolsGUI gui) {
        this.gui = gui;

        this.setLayout(new BorderLayout());
        this.add(contentPane, BorderLayout.CENTER);

        this.browseButton.addActionListener(this);

        this.prefixesRadioButton1.addActionListener(this);
        this.prefixesRadioButton2.addActionListener(this);
        this.prefixesRadioButton3.addActionListener(this);
        this.prefixesRadioButton4.addActionListener(this);

        this.captializationRadioButton1.addActionListener(this);
        this.captializationRadioButton2.addActionListener(this);
        this.captializationRadioButton3.addActionListener(this);

        this.openRootDirectoryButton.addActionListener(this);
        this.startRenamingButton.addActionListener(this);
    }

    private void startRenaming() {

    }

    @Override
    public void stopOperations() {

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
            this.fontFile = fileChooser.getSelectedFile();
        }
        updateStartButton();
    }

    private void updateStartButton() {
        if ((!this.prefixesRadioButton1.isSelected() || !this.captializationRadioButton1.isSelected()) &&
                !this.fileNameField.getText().equals("File Name"))
            this.startRenamingButton.setEnabled(true);
        else
            this.startRenamingButton.setEnabled(false);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource().equals(this.browseButton))
            openFileChooser();
        else if (e.getSource().equals(this.captializationRadioButton1) || e.getSource().equals(this.captializationRadioButton3) || e.getSource().equals(this.prefixesRadioButton2))
            this.capitalizationCheckBox1.setEnabled(false);
        else if ((e.getSource().equals(this.captializationRadioButton2) && !this.prefixesRadioButton2.isSelected()) || ((e.getSource().equals(this.prefixesRadioButton1) || e.getSource().equals(this.prefixesRadioButton3) || e.getSource().equals(this.prefixesRadioButton4)) && this.captializationRadioButton2.isSelected()))
            this.capitalizationCheckBox1.setEnabled(true);
        else if (e.getSource().equals(this.openRootDirectoryButton)) {
            Desktop desktop = Desktop.getDesktop();
            try {
                desktop.open(EmojiTools.getRootDirectory());
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
        updateStartButton();
    }
}
