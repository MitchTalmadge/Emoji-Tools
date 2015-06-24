package me.MitchT.EmojiTools.GUI.Tabs;

import me.MitchT.EmojiTools.Conversion.ConversionManager;
import me.MitchT.EmojiTools.Deletion.DeletionManager;
import me.MitchT.EmojiTools.EmojiTools;
import me.MitchT.EmojiTools.Extraction.ExtractionManager;
import me.MitchT.EmojiTools.GUI.*;
import me.MitchT.EmojiTools.OperationManager;
import me.MitchT.EmojiTools.Renaming.RenamingManager;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;

public class ExtractionTab extends OperationTab implements ActionListener {

    private final EmojiToolsGUI gui;
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

    private OperationManager currentOperationManager;
    private boolean cancelled = false;

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

        ((AbstractDocument) this.extractionDirectoryField.getDocument()).setDocumentFilter(new extractionDirectoryFilter(this, this.extractionDirectoryField));

        this.openRootDirectoryButton.addActionListener(this);
        this.startExtractionButton.addActionListener(this);

        if (fontFile != null && fontFile.exists()) {
            this.fontFile = fontFile;
            startExtraction();
        }
    }

    private void startExtraction() {
        this.cancelled = false;

        File extractionDirectory = new File(EmojiTools.getRootDirectory(), this.extractionDirectoryField.getText());
        if (extractionDirectory.exists()) {
            OverwriteWarningDialog overwriteWarningDialog = new OverwriteWarningDialog(this, this.gui.getLogo(), extractionDirectory);
            overwriteWarningDialog.setVisible(true);
            if (cancelled) {
                this.cancelled = false;
                return;
            } else {
                DeletionDialog deletionDialog = new DeletionDialog(this, this.gui.getLogo());
                this.currentOperationManager = new DeletionManager(extractionDirectory, this.gui, deletionDialog);
                currentOperationManager.start();
                deletionDialog.setVisible(true);
            }
        }

        if (!cancelled) {
            ExtractionDialog extractionDialog = new ExtractionDialog(this, this.gui.getLogo());
            this.currentOperationManager = new ExtractionManager(this.fontFile, extractionDirectory, this.gui, extractionDialog);
            currentOperationManager.start();
            extractionDialog.setVisible(true);
        } else {
            this.cancelled = false;
            return;
        }

        if (this.renameRadioButton2.isSelected() && !cancelled) {
            RenamingDialog renamingDialog = new RenamingDialog(this, this.gui.getLogo());
            this.currentOperationManager = new RenamingManager(extractionDirectory, this.gui, renamingDialog, new boolean[]{false, true, false, false}, new boolean[]{true, false, false, false});
            currentOperationManager.start();
            renamingDialog.setVisible(true);
        }

        if (this.convertRadioButton2.isSelected() && !cancelled) {
            ConversionDialog conversionDialog = new ConversionDialog(this, this.gui.getLogo());
            this.currentOperationManager = new ConversionManager(extractionDirectory, this.gui, conversionDialog);
            currentOperationManager.start();
            conversionDialog.setVisible(true);
        }

        FinishedDialog finishedDialog = new FinishedDialog(this.gui, this.gui.getLogo(), extractionDirectory);
        finishedDialog.setVisible(true);
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
        if (this.extractionDirectoryField.getText().length() > 0 && this.fileNameField.getText().equals("File Name"))
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
                e1.printStackTrace();
            }
        }
    }

    class extractionDirectoryFilter extends DocumentFilter {

        private final ExtractionTab gui;
        private final JTextField textField;

        public extractionDirectoryFilter(ExtractionTab gui, JTextField field) {
            this.gui = gui;
            this.textField = field;
        }

        @Override
        public void replace(FilterBypass fb, int i, int i1, String string, AttributeSet as) throws BadLocationException {
            for (int n = string.length(); n > 0; n--) {
                char c = string.charAt(n - 1);
                if ((Character.isAlphabetic(c) || Character.isDigit(c) || c == '_' || c == '-') && this.textField.getText().length() < 50)
                    super.replace(fb, i, i1, String.valueOf(c), as);
                if (textField.getText().length() == 1)
                    gui.updateStartButton();
            }
        }

        @Override
        public void remove(FilterBypass fb, int i, int i1) throws BadLocationException {
            super.remove(fb, i, i1);

            if (textField.getText().length() == 0)
                gui.updateStartButton();
        }
    }
}
