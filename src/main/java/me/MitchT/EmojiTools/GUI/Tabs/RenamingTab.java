/*
 * Emoji Tools helps users and developers of Android, iOS, and OS X extract, modify, and repackage Emoji fonts.
 * Copyright (C) 2015 Mitch Talmadge
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

package me.MitchT.EmojiTools.GUI.Tabs;

import me.MitchT.EmojiTools.EmojiTools;
import me.MitchT.EmojiTools.GUI.EmojiToolsGUI;
import me.MitchT.EmojiTools.GUI.FinishedDialog;
import me.MitchT.EmojiTools.GUI.RenamingDialog;
import me.MitchT.EmojiTools.Renaming.RenamingManager;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;

public class RenamingTab extends OperationTab implements ActionListener {
    private final EmojiToolsGUI gui;
    private final String defaultFileNameFieldText = "File/Folder Name";
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
    private JTextField exampleOutputField;
    private File renameFile;

    public RenamingTab(EmojiToolsGUI gui) {
        this.gui = gui;

        this.setLayout(new BorderLayout());
        this.add(this.contentPane, BorderLayout.CENTER);

        this.browseButton.addActionListener(this);

        this.prefixesRadioButton1.addActionListener(this);
        this.prefixesRadioButton2.addActionListener(this);
        this.prefixesRadioButton3.addActionListener(this);
        this.prefixesRadioButton4.addActionListener(this);

        this.captializationRadioButton1.addActionListener(this);
        this.captializationRadioButton2.addActionListener(this);
        this.captializationRadioButton3.addActionListener(this);
        this.capitalizationCheckBox1.addActionListener(this);

        this.openRootDirectoryButton.addActionListener(this);
        this.startRenamingButton.addActionListener(this);

        updateExample();
    }

    private void startRenaming() {
        this.cancelled = false;

        if (!this.renameFile.exists()) {
            this.renameFile = null;
            this.fileNameField.setText(defaultFileNameFieldText);
            this.updateStartButton();
            return;
        }

        RenamingDialog renamingDialog = new RenamingDialog(this);
        boolean[] prefixButtons = new boolean[]{this.prefixesRadioButton1.isSelected(), this.prefixesRadioButton2.isSelected(), this.prefixesRadioButton3.isSelected(), this.prefixesRadioButton4.isSelected()};
        boolean[] capitalizationButtons = new boolean[]{this.captializationRadioButton1.isSelected(), this.captializationRadioButton2.isSelected(), this.captializationRadioButton3.isSelected(), this.capitalizationCheckBox1.isSelected()};
        this.currentOperationManager = new RenamingManager(renameFile, this.gui, renamingDialog, prefixButtons, capitalizationButtons);
        currentOperationManager.start();
        renamingDialog.setVisible(true);

        new FinishedDialog(this.gui, "Emoji Renaming Complete!", "Your Renamed Emojis can be found in:", renameFile).setVisible(true);
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
        FileNameExtensionFilter filter = new FileNameExtensionFilter("Folder or PNG File (*.png)", "png");
        fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        fileChooser.setFileFilter(filter);
        int returnVal = fileChooser.showOpenDialog(this);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            this.fileNameField.setText(fileChooser.getSelectedFile().getName());
            this.renameFile = fileChooser.getSelectedFile();
        }
        updateStartButton();
    }

    private void updateStartButton() {
        if ((!this.prefixesRadioButton1.isSelected() || !this.captializationRadioButton1.isSelected()) &&
                !this.fileNameField.getText().equals(defaultFileNameFieldText))
            this.startRenamingButton.setEnabled(true);
        else
            this.startRenamingButton.setEnabled(false);
    }

    private void updateExample() {
        String uniPrefix = "";
        String uPrefix = "";
        int usePrefix = 0; //0 = both, 1 = none, 2 = uni, 3 = u

        if (this.prefixesRadioButton1.isSelected() || this.prefixesRadioButton3.isSelected() || this.prefixesRadioButton4.isSelected()) {
            usePrefix = 0;
        } else if (this.prefixesRadioButton2.isSelected()) {
            usePrefix = 1;
        }

        if (this.captializationRadioButton1.isSelected()) {
            if (this.prefixesRadioButton3.isSelected()) {
                uPrefix = "uni1F60D.png";
                uniPrefix = "uni1f60d.png";
            } else if (this.prefixesRadioButton4.isSelected()) {
                uPrefix = "u1F60D.png";
                uniPrefix = "u1f60d.png";
            } else {
                uPrefix = "u1F60D.png";
                uniPrefix = "uni1f60d.png";
            }
        } else if (this.captializationRadioButton2.isSelected()) {
            if (usePrefix == 0 && !this.prefixesRadioButton1.isSelected())
                usePrefix = (this.prefixesRadioButton3.isSelected()) ? 2 : 3;

            if (this.capitalizationCheckBox1.isSelected()) {
                uPrefix = "u1F60D.png";
                uniPrefix = "uni1F60D.png";
            } else {
                uPrefix = "U1F60D.png";
                uniPrefix = "UNI1F60D.png";
            }
        } else if (this.captializationRadioButton3.isSelected()) {
            if (usePrefix == 0 && !this.prefixesRadioButton1.isSelected())
                usePrefix = (this.prefixesRadioButton3.isSelected()) ? 2 : 3;

            uPrefix = "u1f60d.png";
            uniPrefix = "uni1f60d.png";
        }

        switch (usePrefix) {
            case 0:
                this.exampleOutputField.setText(uniPrefix + " or " + uPrefix);
                break;
            case 1:
                if (this.captializationRadioButton2.isSelected() || this.captializationRadioButton3.isSelected())
                    this.exampleOutputField.setText(uniPrefix.substring(3, uniPrefix.length()));
                else
                    this.exampleOutputField.setText(uniPrefix.substring(3, uniPrefix.length()) + " or " + uPrefix.substring(1, uPrefix.length()));
                break;
            case 2:
                this.exampleOutputField.setText(uniPrefix);
                break;
            case 3:
                this.exampleOutputField.setText(uPrefix);
                break;
            default:
                this.exampleOutputField.setText(uniPrefix + " or " + uPrefix);
                break;
        }

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
                EmojiTools.submitError(Thread.currentThread(), e1);
            }
        } else if (e.getSource().equals(this.startRenamingButton)) {
            startRenaming();
        }
        updateStartButton();
        updateExample();
    }
}
