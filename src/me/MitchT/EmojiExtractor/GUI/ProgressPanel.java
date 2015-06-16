package me.MitchT.EmojiExtractor.GUI;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ProgressPanel extends JPanel implements ActionListener {
    private JButton stopButton;
    private MainFrame mainFrame;
    private JProgressBar progressBar;
    private JLabel timeRemainingLabel;
    private JLabel statusLabel;
    private boolean stopped = false;

    public ProgressPanel(MainFrame frame) {
        this.mainFrame = frame;

        mainFrame.setMinimumSize(new Dimension(400, 150));

        this.progressBar = new JProgressBar();
        progressBar.setValue(0);

        this.timeRemainingLabel = new JLabel("Time Remaining: Unknown");

        this.statusLabel = new JLabel("Status: Unknown");

        this.stopButton = new JButton("Stop Extraction");
        stopButton.addActionListener(this);

        GridBagLayout layout = new GridBagLayout();
        GridBagConstraints gbc = new GridBagConstraints();
        layout.setConstraints(this, gbc);
        this.setLayout(layout);

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.insets = new Insets(5, 5, 5, 5);
        this.add(progressBar, gbc);
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 2;
        this.add(timeRemainingLabel, gbc);
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        this.add(statusLabel, gbc);
        gbc.gridx = 0;
        gbc.gridy = 3;
        this.add(stopButton, gbc);
    }

    public void setShowTimeRemaining(boolean show) {
        this.timeRemainingLabel.setVisible(show);
    }

    public void setShowStatusMessage(boolean show) {
        this.statusLabel.setVisible(show);
    }

    public void setProgress(final int progress) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                progressBar.setValue(progress);
                progressBar.updateUI();
            }
        });
    }

    public boolean getStopped() {
        return this.stopped;
    }

    @Override
    public void actionPerformed(ActionEvent arg0) {
        if (arg0.getSource() instanceof JButton) {
            JButton button = (JButton) arg0.getSource();
            if (button.equals(stopButton)) {
                if (getStopped())
                    stopButton.setText("Stop Extraction");
                else
                    stopButton.setText("Start Extraction");
                this.stopped = !this.stopped;
            }
        }
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

    public void setStatusMessage(String message) {
        this.statusLabel.setText(message);
    }
}
