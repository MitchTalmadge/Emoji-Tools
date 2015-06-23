package me.MitchT.EmojiTools.GUI;

import me.MitchT.EmojiTools.GUI.Tabs.ExtractionTab;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class ProgressDialog extends JDialog implements ActionListener {
    private final ExtractionTab gui;
    private JPanel contentPane;
    private JButton cancelButton;
    private JProgressBar progressBar;
    private JTextArea statusMessageArea;
    private JScrollPane scrollPane;
    private JLabel titleLabel;

    ProgressDialog(ExtractionTab gui, String tileText, Image logo) {
        this.gui = gui;


        this.setIconImage(logo);
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(cancelButton);

        this.titleLabel.setText(tileText);

        cancelButton.addActionListener(this);

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

    public void appendToStatus(String message) {
        this.statusMessageArea.append(message + "\n");
        this.scrollPane.getVerticalScrollBar().setValue(this.scrollPane.getVerticalScrollBar().getMaximum());
    }

    private void onCancel() {
        this.gui.stopOperations();
        dispose();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource().equals(cancelButton)) {
            onCancel();
        }
    }
}
