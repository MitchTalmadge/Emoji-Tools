package me.MitchT.EmojiExtractor.GUI;

import me.MitchT.EmojiExtractor.EmojiExtractor;
import me.MitchT.EmojiExtractor.ExtractionManager;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;

public class EmojiToolsGUI extends JFrame implements ActionListener {

    private final String version = "V1.4";

    private JTabbedPane tabbedPane;
    private JPanel contentPane;
    private JRadioButton renameRadioButton1;
    private JRadioButton renameRadioButton2;
    private JTextField fileNameField;
    private JButton browseButton;
    private JRadioButton convertRadioButton1;
    private JRadioButton convertRadioButton2;
    private JLabel headerLabel;
    private JTextField exportDirectoryField;
    private JButton startExtractionButton;

    private ExtractionManager extractionManager;
    private File font;

    public EmojiToolsGUI(File font) {
        this.font = font;

        setTitle("Emoji Tools");
        setContentPane(contentPane);
        setResizable(false);

        this.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (extractionManager != null)
                    extractionManager.stopExtraction();
                System.exit(0);
            }
        });

        this.headerLabel.setText("Emoji Tools " + version);
        this.tabbedPane.setSelectedIndex(0);

        this.browseButton.addActionListener(this);
        this.renameRadioButton1.addActionListener(this);
        this.renameRadioButton2.addActionListener(this);
        this.convertRadioButton1.addActionListener(this);
        this.convertRadioButton2.addActionListener(this);

        ((AbstractDocument) this.exportDirectoryField.getDocument()).setDocumentFilter(new exportDirectoryFilter(this, this.exportDirectoryField));
        this.exportDirectoryField.addActionListener(this);

        this.startExtractionButton.addActionListener(this);

        pack();
        this.setLocationRelativeTo(null);
        this.setVisible(true);

        if (font != null && font.exists()) {
            startExtraction();
        }
    }

    public void startExtraction() {
        ExtractionDialog extractionDialog = new ExtractionDialog(this);
        this.extractionManager = new ExtractionManager(this.font, this, extractionDialog);
        extractionManager.startExtraction();
        extractionDialog.setVisible(true);
    }

    private void openFileChooser() {
        this.fileNameField.setText("File Name");

        JFileChooser fileChooser = new JFileChooser(EmojiExtractor.getRootDirectory());
        FileNameExtensionFilter filter = new FileNameExtensionFilter("Emoji Font File (*.ttf)", "ttf");
        fileChooser.setFileFilter(filter);
        int returnVal = fileChooser.showOpenDialog(this);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            this.fileNameField.setText(fileChooser.getSelectedFile().getName());
            this.font = fileChooser.getSelectedFile();
        }
        updateStartButton();
    }

    public void updateStartButton() {
        if (this.exportDirectoryField.getText().length() == 0 || this.fileNameField.getText().equals("File Name")) {
            this.startExtractionButton.setEnabled(false);
        } else {
            this.startExtractionButton.setEnabled(true);
        }
    }

    public void showMessageDialog(String message) {
        JOptionPane.showMessageDialog(this, message);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource().equals(this.browseButton)) {
            openFileChooser();
        /*} else if (e.getSource().equals(this.renameRadioButton1)) {
            this.renameRadioButton1.setSelected(true);
            this.renameRadioButton2.setSelected(false);
        } else if (e.getSource().equals(this.renameRadioButton2)) {
            this.renameRadioButton1.setSelected(false);
            this.renameRadioButton2.setSelected(true);
        } else if (e.getSource().equals(this.convertRadioButton1)) {
            this.convertRadioButton1.setSelected(true);
            this.convertRadioButton2.setSelected(false);
        } else if (e.getSource().equals(this.convertRadioButton2)) {
            this.convertRadioButton1.setSelected(false);
            this.convertRadioButton2.setSelected(true);*/
        } else if (e.getSource().equals(this.startExtractionButton)) {
            startExtraction();
        }
    }

    public ExtractionManager getExtractionManager() {
        return this.extractionManager;
    }

    class exportDirectoryFilter extends DocumentFilter {

        private final EmojiToolsGUI gui;
        private final JTextField textField;

        public exportDirectoryFilter(EmojiToolsGUI gui, JTextField field) {
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

        @Override
        public void insertString(DocumentFilter.FilterBypass fb, int i, String string, AttributeSet as) throws BadLocationException {
            super.insertString(fb, i, string, as);
        }
    }
}
