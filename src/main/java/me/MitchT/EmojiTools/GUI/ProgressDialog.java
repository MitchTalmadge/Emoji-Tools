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

package me.MitchT.EmojiTools.GUI;

import me.MitchT.EmojiTools.EmojiTools;
import me.MitchT.EmojiTools.GUI.Tabs.OperationTab;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class ProgressDialog extends JDialog implements ActionListener {
    private final OperationTab gui;
    private JPanel contentPane;
    private JButton cancelButton;
    private JProgressBar progressBar;
    private JTextArea statusMessageArea;
    private JScrollPane scrollPane;
    private JLabel titleLabel;

    ProgressDialog(OperationTab gui, String tileText) {
        this.gui = gui;

        this.setIconImage(EmojiTools.getLogoImage());
        setContentPane(contentPane);
        setModal(true);
        setResizable(false);
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

    public void setIndeterminate(final boolean indeterminate) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                progressBar.setIndeterminate(indeterminate);
            }
        });
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

    public void writeToStatus(final String message) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                statusMessageArea.append(message);
                scrollPane.getVerticalScrollBar().setValue(scrollPane.getVerticalScrollBar().getMaximum());
            }
        });
    }

    public void appendToStatus(final String message) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                statusMessageArea.append(message + "\n");
                scrollPane.getVerticalScrollBar().setValue(scrollPane.getVerticalScrollBar().getMaximum());
            }
        });
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
