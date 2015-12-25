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

package net.liveforcode.emojitools.oldgui.tabs;

import net.liveforcode.emojitools.conversion.ConversionManager;
import net.liveforcode.emojitools.EmojiTools;
import net.liveforcode.emojitools.oldgui.ConversionDialog;
import net.liveforcode.emojitools.oldgui.EmojiToolsGUI;
import net.liveforcode.emojitools.oldgui.FinishedDialog;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;

public class ConversionTab extends OperationTab implements ActionListener {
    private final static String defaultFileNameFieldText = "File/Folder Name";
    private final EmojiToolsGUI gui;
    private JPanel contentPane;
    private JTextField fileNameField;
    private JButton browseButton;
    private JRadioButton conversionRadioButton1;
    private JRadioButton conversionRadioButton2;
    private JButton startConvertingButton;
    private JButton openRootDirectoryButton;
    private File conversionFile;

    public ConversionTab(EmojiToolsGUI gui) {
        this.gui = gui;

        this.setLayout(new BorderLayout());
        this.add(this.contentPane, BorderLayout.CENTER);

        this.browseButton.addActionListener(this);

        this.conversionRadioButton1.addActionListener(this);
        this.conversionRadioButton1.addActionListener(this);

        this.openRootDirectoryButton.addActionListener(this);
        this.startConvertingButton.addActionListener(this);
    }

    private void startConversion() {
        this.cancelled = false;

        if (!this.conversionFile.exists()) {
            this.conversionFile = null;
            this.fileNameField.setText(defaultFileNameFieldText);
            this.updateStartButton();
            return;
        }

        ConversionDialog conversionDialog = new ConversionDialog(this);
        this.currentOperationManager = new ConversionManager(this.conversionFile, this.gui, conversionDialog, this.conversionRadioButton1.isSelected());
        currentOperationManager.start();
        conversionDialog.setVisible(true);

        new FinishedDialog(this.gui, "Emoji Conversion Complete!", "Your Converted Emojis can be found in:", conversionFile).setVisible(true);
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
            this.conversionFile = fileChooser.getSelectedFile();
        }
        updateStartButton();
    }

    private void updateStartButton() {
        if (!this.fileNameField.getText().equals(defaultFileNameFieldText))
            this.startConvertingButton.setEnabled(true);
        else
            this.startConvertingButton.setEnabled(false);
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
                EmojiTools.submitError(Thread.currentThread(), e1);
            }
        } else if (e.getSource().equals(this.startConvertingButton)) {
            startConversion();
        }
        updateStartButton();
    }
}
