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

package com.AptiTekk2.AptiAPI2.GUI;

import com.AptiTekk2.AptiAPI2.ErrorReport;

import javax.swing.*;
import java.awt.event.*;

public class ErrorDetailsDialog extends JDialog implements ActionListener {
    private JPanel contentPane;
    private JTextPane reportArea;
    private JButton OKButton;
    private JScrollPane scrollPane;

    public ErrorDetailsDialog(ErrorReportDialog dialog, ErrorReport report) {

        setIconImage(dialog.getIconImages().get(0));
        setContentPane(contentPane);
        setModal(true);
        setResizable(false);
        getRootPane().setDefaultButton(OKButton);

        this.reportArea.setText("<html>" + report.generateReport().replaceAll("\n", "<br>").replaceAll("\t", "&nbsp;&nbsp;&nbsp;&nbsp;") + "</html>");

        this.OKButton.addActionListener(this);

        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onOK();
            }
        });

        // call onDontSendReport() on ESCAPE
        contentPane.registerKeyboardAction(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onOK();
            }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

        pack();

        this.reportArea.setCaretPosition(0);

        setLocationRelativeTo(dialog);
    }

    private void onOK() {
        this.dispose();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource().equals(this.OKButton))
            onOK();
    }
}
