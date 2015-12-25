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

package com.aptitekk.aptiapi.gui;

import com.aptitekk.aptiapi.AptiAPI;
import com.aptitekk.aptiapi.ErrorReport;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class ErrorReportDialog extends JDialog implements ActionListener {
    private final ErrorReport report;
    private Image imageIcon;
    private AptiAPI aptiAPI;
    private JPanel contentPane;
    private JButton sendReportButton;
    private JButton dontSendButton;
    private JTextField nameField;
    private JTextField emailAddressField;
    private JButton viewDetailsButton;
    private JTextArea descriptionArea;

    public ErrorReportDialog(AptiAPI aptiAPI, ErrorReport report) {
        this.aptiAPI = aptiAPI;
        this.report = report;

        setContentPane(contentPane);
        setModal(true);
        setResizable(false);
        getRootPane().setDefaultButton(sendReportButton);

        this.sendReportButton.addActionListener(this);
        this.dontSendButton.addActionListener(this);
        this.viewDetailsButton.addActionListener(this);

        TextFilter.assignFilter(this.descriptionArea, 255, TextFilter.NO_HTML, null);
        TextFilter.assignFilter(this.nameField, 70, TextFilter.ALPHA_SPACE, null);
        TextFilter.assignFilter(this.emailAddressField, 100, TextFilter.EMAIL, null);

        // call onDontSendReport() when cross is clicked
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onDontSendReport();
            }
        });

        // call onDontSendReport() on ESCAPE
        contentPane.registerKeyboardAction(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onDontSendReport();
            }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

        pack();
        setLocationRelativeTo(null);
    }

    private void onSendReport() {
        aptiAPI.shutdownListeners();
        this.report.setDescription(this.descriptionArea.getText());
        this.report.setName(this.nameField.getText());
        this.report.setEmail(this.emailAddressField.getText());
        this.aptiAPI.sendErrorReport(report);

        JOptionPane.showMessageDialog(this, "Error Report has been sent successfully! Thank you for your support!", "Error Report Sent!", JOptionPane.INFORMATION_MESSAGE);

        this.dispose();
        System.exit(0);
    }

    private void onDontSendReport() {
        aptiAPI.shutdownListeners();
        this.dispose();
        System.exit(0);
    }

    private void onViewDetails() {
        this.report.setDescription(this.descriptionArea.getText());
        this.report.setName(this.nameField.getText());
        this.report.setEmail(this.emailAddressField.getText());
        new ErrorDetailsDialog(this, report).setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource().equals(this.sendReportButton)) {
            onSendReport();
        } else if (e.getSource().equals(this.dontSendButton)) {
            onDontSendReport();
        } else if (e.getSource().equals(this.viewDetailsButton)) {
            onViewDetails();
        }
    }
}
