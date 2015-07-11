package me.MitchT.EmojiTools.GUI;

import me.MitchT.EmojiTools.ErrorReport;
import me.MitchT.EmojiTools.GUI.Tabs.OperationTab;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class ErrorReportDialog extends JDialog implements ActionListener {
    private final OperationTab gui;
    private final ErrorReport report;
    private JPanel contentPane;
    private JButton sendReportButton;
    private JButton dontSendButton;
    private JTextField nameField;
    private JTextField emailAddressField;
    private JButton viewDetailsButton;
    private JTextArea textArea1;
    private JTextArea descriptionArea;

    public ErrorReportDialog(OperationTab gui, Image logo, ErrorReport report) {

        this.gui = gui;
        this.report = report;

        setIconImage(logo);
        setContentPane(contentPane);
        setModal(true);
        setResizable(false);
        getRootPane().setDefaultButton(sendReportButton);

        this.sendReportButton.addActionListener(this);
        this.dontSendButton.addActionListener(this);
        this.viewDetailsButton.addActionListener(this);

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
        setLocationRelativeTo(gui);
    }

    private void onSendReport() {
        this.report.setDescription(this.descriptionArea.getText());
        this.report.setName(this.nameField.getText());
        this.report.setEmail(this.emailAddressField.getText());
        this.report.sendReport();

        this.dispose();
        System.exit(0);
    }

    private void onDontSendReport() {
        gui.stopOperations();
        this.dispose();
        System.exit(0);
    }

    private void onViewDetails() {
        new ErrorDetailsDialog(this, this.getIconImages().get(0), report).setVisible(true);
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
