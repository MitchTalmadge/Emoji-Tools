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

import me.MitchT.EmojiTools.Conversion.ConversionManager;
import me.MitchT.EmojiTools.Deletion.DeletionManager;
import me.MitchT.EmojiTools.EmojiTools;
import me.MitchT.EmojiTools.Extraction.ExtractionManager;
import me.MitchT.EmojiTools.GUI.*;
import me.MitchT.EmojiTools.Renaming.RenamingManager;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;

public class ExtractionTab extends OperationTab implements ActionListener, TextFilter.TextFilterListener {

    private final EmojiToolsGUI gui;
    private final String defaultFileNameFieldText = "File Name";
    private JPanel contentPane;
    private JRadioButton renameRadioButton1;
    private JRadioButton renameRadioButton2;
    private JTextField fileNameField;
    private JButton browseButton;
    private JRadioButton convertRadioButton1;
    private JRadioButton convertRadioButton2;
    private JTextField extractionDirectoryField;
    private JButton startExtractionButton;
    private JButton openRootDirectoryButton;
    private File fontFile;

    public ExtractionTab(EmojiToolsGUI gui, File fontFile) {

        this.gui = gui;
        this.fontFile = null;

        setLayout(new BorderLayout());
        this.add(contentPane, BorderLayout.CENTER);

        this.browseButton.addActionListener(this);
        this.renameRadioButton1.addActionListener(this);
        this.renameRadioButton2.addActionListener(this);
        this.convertRadioButton1.addActionListener(this);
        this.convertRadioButton2.addActionListener(this);

        TextFilter.assignFilter(this.extractionDirectoryField, 50, TextFilter.FILENAME, this);

        this.openRootDirectoryButton.addActionListener(this);
        this.startExtractionButton.addActionListener(this);

        if (fontFile != null && fontFile.exists()) {
            this.fontFile = fontFile;
            startExtraction();
        }
    }

    private void startExtraction() {
        this.cancelled = false;

        if (!this.fontFile.exists()) {
            this.fontFile = null;
            this.fileNameField.setText(defaultFileNameFieldText);
            this.updateStartButton();
            return;
        }

        File extractionDirectory = new File(EmojiTools.getRootDirectory(), this.extractionDirectoryField.getText());
        if (extractionDirectory.exists()) {
            OverwriteWarningDialog overwriteWarningDialog = new OverwriteWarningDialog(this, extractionDirectory);
            overwriteWarningDialog.setVisible(true);
            if (cancelled) {
                this.cancelled = false;
                return;
            } else {
                DeletionDialog deletionDialog = new DeletionDialog(this);
                this.currentOperationManager = new DeletionManager(extractionDirectory, this.gui, deletionDialog);
                currentOperationManager.start();
                deletionDialog.setVisible(true);
            }
        }

        if (!cancelled) {
            ExtractionDialog extractionDialog = new ExtractionDialog(this);
            this.currentOperationManager = new ExtractionManager(this.fontFile, extractionDirectory, this.gui, extractionDialog);
            currentOperationManager.start();
            extractionDialog.setVisible(true);
        } else {
            this.cancelled = false;
            return;
        }

        if (this.renameRadioButton2.isSelected() && !cancelled) {
            RenamingDialog renamingDialog = new RenamingDialog(this);
            this.currentOperationManager = new RenamingManager(extractionDirectory, this.gui, renamingDialog, new boolean[]{false, true, false, false}, new boolean[]{true, false, false, false});
            currentOperationManager.start();
            renamingDialog.setVisible(true);
        }

        if (this.convertRadioButton2.isSelected() && !cancelled) {
            ConversionDialog conversionDialog = new ConversionDialog(this);
            this.currentOperationManager = new ConversionManager(extractionDirectory, this.gui, conversionDialog, true);
            currentOperationManager.start();
            conversionDialog.setVisible(true);
        }

        new FinishedDialog(this.gui, "Emoji Extraction Complete!", "Your Extracted Emojis can be found in:", extractionDirectory).setVisible(true);
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
        FileNameExtensionFilter filter = new FileNameExtensionFilter("Emoji Font File (*.ttf)", "ttf");
        fileChooser.setFileFilter(filter);
        int returnVal = fileChooser.showOpenDialog(this);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            this.fileNameField.setText(fileChooser.getSelectedFile().getName());
            this.fontFile = fileChooser.getSelectedFile();
        }
        updateStartButton();
    }

    private void updateStartButton() {
        if (this.extractionDirectoryField.getText().length() > 0 && this.fileNameField.getText().equals(defaultFileNameFieldText))
            this.startExtractionButton.setEnabled(true);
        else
            this.startExtractionButton.setEnabled(true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource().equals(this.browseButton)) {
            openFileChooser();
        } else if (e.getSource().equals(this.startExtractionButton)) {
            startExtraction();
        } else if (e.getSource().equals(this.openRootDirectoryButton)) {
            Desktop desktop = Desktop.getDesktop();
            try {
                desktop.open(EmojiTools.getRootDirectory());
            } catch (IOException e1) {
                EmojiTools.submitError(Thread.currentThread(), e1);
            }
        }
    }

    @Override
    public void lengthChanged(int newLength, JTextComponent sourceComponent) {
        updateStartButton();
    }
}
