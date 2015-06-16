package me.MitchT.EmojiExtractor.GUI;

import me.MitchT.EmojiExtractor.ExtractionManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;

public class MainFrame extends JFrame {
    private CardLayout cardLayout;
    private SelectionPanel selectionPanel;
    private ExtractionManager extractionManager;

    public MainFrame(File filePath) {
        setTitle("Emoji Extractor");
        this.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (extractionManager != null)
                    extractionManager.stopExtraction();
                System.exit(0);
            }
        });

        this.selectionPanel = new SelectionPanel(this);

        this.setContentPane(selectionPanel);
        this.pack();
        this.setLocationRelativeTo(null);
        this.setVisible(true);

        if (filePath != null && filePath.exists()) {
            startExtraction(filePath);
        }
    }

    public void showSelectionPanel() {
        if (extractionManager != null)
            extractionManager.stopExtraction();
        this.setContentPane(selectionPanel);
        this.pack();
    }

    public void showMessagePanel(final String message) {
        if (extractionManager != null)
            extractionManager.stopExtraction();
        final MainFrame mainFrame = this;
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                setContentPane(new MessagePanel(mainFrame, message));
                pack();
            }
        });

    }

    public void startExtraction(File filePath) {
        ProgressPanel progressPanel = new ProgressPanel(this);
        this.setContentPane(progressPanel);
        this.pack();
        this.extractionManager = new ExtractionManager(filePath, this, progressPanel);
        this.extractionManager.startExtraction();
    }
}
