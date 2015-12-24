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

package net.liveforcode.EmojiTools.OldGUI;

import net.liveforcode.EmojiTools.EmojiTools;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;

public class FinishedDialog extends JDialog implements ActionListener {
    private final File outputDirectory;
    private JPanel contentPane;
    private JLabel headerField;
    private JLabel descriptionField;
    private JTextField locationField;
    private JButton OKButton;
    private JButton openOutputDirectoryButton;

    public FinishedDialog(EmojiToolsGUI gui, String headerText, String descriptionText, File outputDirectory) {
        this.outputDirectory = outputDirectory;

        setContentPane(contentPane);
        setModal(true);
        setResizable(false);
        getRootPane().setDefaultButton(OKButton);

        this.headerField.setText(headerText);
        this.descriptionField.setText(descriptionText);

        this.locationField.setText(this.outputDirectory.getAbsolutePath());

        OKButton.addActionListener(this);
        openOutputDirectoryButton.addActionListener(this);

        // call onOK() when cross is clicked
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onOK();
            }
        });

        // call onOK() on ESCAPE
        contentPane.registerKeyboardAction(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onOK();
            }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

        pack();
        this.setLocationRelativeTo(gui);
    }

    private void onOK() {
        this.dispose();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource().equals(OKButton)) {
            onOK();
        } else if (e.getSource().equals(openOutputDirectoryButton)) {
            try {
                Desktop.getDesktop().open(outputDirectory);
            } catch (IOException e1) {
                EmojiTools.submitError(Thread.currentThread(), e1);
            }
            this.dispose();
        }
    }
}
