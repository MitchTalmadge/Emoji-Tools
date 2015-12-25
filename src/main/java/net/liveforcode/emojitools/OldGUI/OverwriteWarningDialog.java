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

package net.liveforcode.emojitools.oldgui;

import net.liveforcode.emojitools.oldgui.tabs.ExtractionTab;

import javax.swing.*;
import java.awt.event.*;
import java.io.File;

public class OverwriteWarningDialog extends JDialog implements ActionListener {
    private final ExtractionTab gui;
    private JPanel contentPane;
    private JButton backButton;
    private JButton continuebutton;
    private JTextField extractionDirectoryField;

    public OverwriteWarningDialog(ExtractionTab gui, File extractionDirectory) {

        this.gui = gui;

        setContentPane(contentPane);
        setModal(true);
        setResizable(false);
        getRootPane().setDefaultButton(backButton);

        this.extractionDirectoryField.setText(extractionDirectory.getName());

        backButton.addActionListener(this);
        continuebutton.addActionListener(this);

        // call onBack() when cross is clicked
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onBack();
            }
        });

        // call onBack() on ESCAPE
        contentPane.registerKeyboardAction(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onBack();
            }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

        pack();
        this.setLocationRelativeTo(gui);
    }

    private void onBack() {
        gui.stopOperations();
        this.dispose();
    }

    private void onContinue() {
        this.dispose();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource().equals(this.backButton))
            onBack();
        else if (e.getSource().equals(continuebutton))
            onContinue();
    }
}
