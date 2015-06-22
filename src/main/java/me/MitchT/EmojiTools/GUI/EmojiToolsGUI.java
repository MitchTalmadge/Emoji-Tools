package me.MitchT.EmojiTools.GUI;

import me.MitchT.EmojiTools.Conversion.ConversionManager;
import me.MitchT.EmojiTools.EmojiTools;
import me.MitchT.EmojiTools.Extraction.ExtractionManager;
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
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;

public class EmojiToolsGUI extends JFrame implements ActionListener {

    private final String version = "V1.5";
    private final Image logo;

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
    private JButton openRootDirectoryButton;

    private OperationManager currentOperationManager;
    private File font;
    private boolean cancelled = false;

    public EmojiToolsGUI(File font) {
        this.font = font;

        setTitle("Emoji Tools");
        setContentPane(contentPane);
        setResizable(false);

        this.logo = new ImageIcon(getClass().getResource("/Images/EmojiToolsLogo.png")).getImage();

        this.setIconImage(logo);

        this.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (currentOperationManager != null)
                    currentOperationManager.stop();
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
        this.openRootDirectoryButton.addActionListener(this);

        pack();
        this.setLocationRelativeTo(null);
        this.setVisible(true);

        if (font != null && font.exists()) {
            startExtraction();
        }
    }

    private void startExtraction() {
        ExtractionDialog extractionDialog = new ExtractionDialog(this, this.logo);
        this.currentOperationManager = new ExtractionManager(this.font, this.exportDirectoryField.getText(), this, extractionDialog);
        currentOperationManager.start();
        extractionDialog.setVisible(true);

        if (this.renameRadioButton2.isSelected() && !cancelled) {
            RenamingDialog renamingDialog = new RenamingDialog(this, this.logo);
            this.currentOperationManager = new RenamingManager(new File(EmojiTools.getRootDirectory(), this.exportDirectoryField.getText()), this, renamingDialog);
            currentOperationManager.start();
            renamingDialog.setVisible(true);
        }

        if (this.convertRadioButton2.isSelected() && !cancelled) {
            ConversionDialog conversionDialog = new ConversionDialog(this, this.logo);
            this.currentOperationManager = new ConversionManager(new File(EmojiTools.getRootDirectory(), this.exportDirectoryField.getText()), this, conversionDialog);
            currentOperationManager.start();
            conversionDialog.setVisible(true);
        }

        FinishedDialog finishedDialog = new FinishedDialog(this, logo, new File(EmojiTools.getRootDirectory(), this.exportDirectoryField.getText()));
        finishedDialog.setVisible(true);
        this.cancelled = false;
    }

    private void openFileChooser() {
        this.fileNameField.setText("File Name");

        JFileChooser fileChooser = new JFileChooser(EmojiTools.getRootDirectory());
        FileNameExtensionFilter filter = new FileNameExtensionFilter("Emoji Font File (*.ttf)", "ttf");
        fileChooser.setFileFilter(filter);
        int returnVal = fileChooser.showOpenDialog(this);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            this.fileNameField.setText(fileChooser.getSelectedFile().getName());
            this.font = fileChooser.getSelectedFile();
        }
        updateStartButton();
    }

    private void updateStartButton() {
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

    public void cancelOperations() {
        this.currentOperationManager.stop();
        this.cancelled = true;
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
    }
}
