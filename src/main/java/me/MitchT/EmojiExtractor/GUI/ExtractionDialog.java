package me.MitchT.EmojiExtractor.GUI;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class ExtractionDialog extends JDialog {
    private final EmojiToolsGUI gui;
    private JPanel contentPane;
    private JButton cancelButton;
    private JProgressBar progressBar;
    private JLabel timeRemainingLabel;
    private JTextArea statusMessageArea;
    private JScrollPane scrollPane;

    public ExtractionDialog(EmojiToolsGUI gui) {
        this.gui = gui;

        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(cancelButton);

        cancelButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        });

        // call onCancel() when cross is clicked
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });

        // call onCancel() on ESCAPE
        contentPane.registerKeyboardAction(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

        this.statusMessageArea.setLineWrap(true);

        this.setMinimumSize(new Dimension(400, 250));

        this.pack();
        this.setLocationRelativeTo(gui);
        this.setVisible(false);
    }

    public void setTimeRemainingVisible(boolean visible) {
        this.timeRemainingLabel.setVisible(visible);
    }

    public void setProgress(final int progress) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                progressBar.setValue(progress);
                progressBar.setString(progress + "%");
                progressBar.updateUI();
            }
        });
    }

    public void setTimeRemaining(final long currentBytePos, final long filePathLength, final long currTime, final long startTime) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                long elapsedTime = currTime - startTime;
                long totalTimeMillis = (long) ((1.0d / ((double) currentBytePos / filePathLength)) * elapsedTime);
                long timeRemainingMillis = totalTimeMillis - elapsedTime;

                long hour = (timeRemainingMillis / (1000 * 60 * 60)) % 24;
                long minute = (timeRemainingMillis / (1000 * 60)) % 60;
                long second = (timeRemainingMillis / 1000) % 60;

                String time = String.format("%02d:%02d:%02d", hour, minute, second);

                timeRemainingLabel.setText("Time Remaining: " + time);
            }
        });
    }

    public void appendToStatus(String message) {
        this.statusMessageArea.append(message + "\n");
        this.scrollPane.getVerticalScrollBar().setValue(this.scrollPane.getVerticalScrollBar().getMaximum());
    }

    private void onCancel() {
        this.gui.getExtractionManager().stopExtraction();
        dispose();
    }
}
