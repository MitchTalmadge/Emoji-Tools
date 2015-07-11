package me.MitchT.EmojiTools.GUI;

import me.MitchT.EmojiTools.ErrorReport;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class ErrorDetailsDialog extends JDialog implements ActionListener {
    private final ErrorReportDialog dialog;
    private final ErrorReport report;
    private JPanel contentPane;
    private JTextArea exceptionArea;
    private JButton OKButton;

    public ErrorDetailsDialog(ErrorReportDialog dialog, Image logo, ErrorReport report) {

        this.dialog = dialog;
        this.report = report;

        setIconImage(logo);
        setContentPane(contentPane);
        setModal(true);
        setResizable(false);
        getRootPane().setDefaultButton(OKButton);

        this.exceptionArea.setText(report.getStackTrace());

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
