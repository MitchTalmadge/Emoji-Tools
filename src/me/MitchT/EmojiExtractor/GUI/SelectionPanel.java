package me.MitchT.EmojiExtractor.GUI;

import me.MitchT.EmojiExtractor.EmojiExtractor;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class SelectionPanel extends JPanel implements ActionListener {
    private JFileChooser fileChooser;
    private JTextField fileField;
    private JButton browseButton;
    private JButton startButton;
    private MainFrame mainFrame;

    public SelectionPanel(MainFrame frame) {
        this.mainFrame = frame;

        mainFrame.setMinimumSize(new Dimension(400, 150));

        this.fileField = new JTextField();
        fileField.setColumns(20);
        fileField.setEditable(false);

        this.browseButton = new JButton("Browse...");
        browseButton.addActionListener(this);
        this.startButton = new JButton("Start Extraction");
        startButton.addActionListener(this);

        JLabel copyrightLabel = new JLabel("Copyright 2015 Mitch Talmadge");

        JPanel browsePanel = new JPanel();
        browsePanel.add(fileField);
        browsePanel.add(browseButton);

        GridBagLayout layout = new GridBagLayout();
        GridBagConstraints gbc = new GridBagConstraints();
        layout.setConstraints(this, gbc);
        this.setLayout(layout);

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.insets = new Insets(5, 5, 5, 5);
        this.add(browsePanel, gbc);
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 2;
        this.add(startButton, gbc);
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        this.add(copyrightLabel, gbc);
    }

    private void openFileChooser() {
        this.fileField.setText("");

        this.fileChooser = new JFileChooser(EmojiExtractor.getRootDirectory());
        FileNameExtensionFilter filter = new FileNameExtensionFilter("Emoji Font File (*.ttf)", "ttf");
        fileChooser.setFileFilter(filter);
        int returnVal = fileChooser.showOpenDialog(this);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            this.fileField.setText(fileChooser.getSelectedFile().getName());
        }
    }

    @Override
    public void actionPerformed(ActionEvent arg0) {
        if (arg0.getSource() instanceof JButton) {
            JButton button = (JButton) arg0.getSource();
            if (button.equals(browseButton)) {
                openFileChooser();
            } else if (button.equals(startButton)) {
                if (this.fileChooser != null && this.fileChooser.getSelectedFile() != null)
                    mainFrame.startExtraction(this.fileChooser.getSelectedFile());
            }
        }
    }
}
