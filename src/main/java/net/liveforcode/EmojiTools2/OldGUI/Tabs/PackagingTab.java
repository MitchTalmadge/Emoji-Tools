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

package net.liveforcode.EmojiTools2.OldGUI.Tabs;

import net.liveforcode.EmojiTools2.Conversion.ConversionManager;
import net.liveforcode.EmojiTools2.EmojiTools;
import net.liveforcode.EmojiTools2.Extraction.ExtractionManager;
import net.liveforcode.EmojiTools2.OldGUI.*;
import net.liveforcode.EmojiTools2.Packaging.PackagingManager;
import net.liveforcode.EmojiTools2.Renaming.RenamingManager;

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
    private JButton startPackagingButton;
    private JButton openRootDirectoryButton;
    private File packagingFile;

    public PackagingTab(EmojiToolsGUI gui) {
        this.gui = gui;

        this.setLayout(new BorderLayout());
        this.add(this.contentPane, BorderLayout.CENTER);

        this.browseButton.addActionListener(this);

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

        File ttxFile = null;
        for (File file : packagingFile.listFiles()) {
            if (file.getName().endsWith(".ttx"))
                ttxFile = file;
        }

        if (ttxFile == null) {
            gui.showMessageDialog("The selected directory cannot be packaged!");
            return;
        }

        ExtractionManager.TTXType ttxType = null;
        for (ExtractionManager.TTXType type : ExtractionManager.TTXType.values()) {
            if (type.getFileName().equals(ttxFile.getName())) {
                ttxType = type;
                break;
            }
        }

        boolean containsPngs = false;

        for (String fileName : this.packagingFile.list()) {
            if (fileName.contains(".png"))
                containsPngs = true;
        }

        if (!containsPngs) {
            gui.showMessageDialog("The selected directory does not contain any Emojis!");
            return;
        }

        RenamingDialog renamingDialog = new RenamingDialog(this);
        this.currentOperationManager = new RenamingManager(this.packagingFile, this.gui, renamingDialog, new boolean[]{false, false, true, false}, new boolean[]{false, false, true, true});
        currentOperationManager.start();
        renamingDialog.setVisible(true);

        if (!cancelled) {
            ConversionDialog conversionDialog = new ConversionDialog(this);
            this.currentOperationManager = new ConversionManager(this.packagingFile, this.gui, conversionDialog, true);
            currentOperationManager.start();
            conversionDialog.setVisible(true);
        }

        if (!cancelled) {
            PackagingDialog packagingDialog = new PackagingDialog(this);
            this.currentOperationManager = new PackagingManager(this.gui, this.packagingFile, packagingDialog, ttxType);
            currentOperationManager.start();
            packagingDialog.setVisible(true);
        }

        new FinishedDialog(this.gui, "Emoji Packaging Complete!", "Your Packaged Emoji Font can be found in:", new File(EmojiTools.getRootDirectory(), "Output")).setVisible(true);
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
                EmojiTools.submitError(Thread.currentThread(), e1);
            }
        } else if (e.getSource().equals(this.startPackagingButton)) {
            startPackaging();
        }
        updateStartButton();
    }
}
