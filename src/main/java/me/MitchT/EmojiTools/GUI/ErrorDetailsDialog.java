package me.MitchT.EmojiTools.GUI;

import me.MitchT.EmojiTools.EmojiTools;
import me.MitchT.EmojiTools.ErrorReport;

import javax.swing.*;
import java.awt.event.*;

public class ErrorDetailsDialog extends JDialog implements ActionListener {
    private JPanel contentPane;
    private JTextPane reportArea;
    private JButton OKButton;
    private JScrollPane scrollPane;

    public ErrorDetailsDialog(ErrorReportDialog dialog, ErrorReport report) {

        setIconImage(EmojiTools.getLogoImage());
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
