package me.MitchT.EmojiTools;

import com.AptiTekk.AptiAPI.AptiAPI;
import com.AptiTekk.AptiAPI.AptiAPIListener;
import me.MitchT.EmojiTools.GUI.ErrorReportDialog;

import javax.swing.*;

public class ErrorHandler implements Thread.UncaughtExceptionHandler, AptiAPIListener {

    private final AptiAPI aptiAPI;

    public ErrorHandler() {
        this.aptiAPI = new AptiAPI(EmojiTools.PROJECT_ID);
        this.aptiAPI.addAPIListener(this);
    }

    @Override
    public void uncaughtException(Thread t, Throwable e) {
        ErrorReport errorReport = new ErrorReport(t, e);

        System.out.println("ERROR OCCURRED!");
        System.out.println("Thread Name: " + t.getName());
        System.out.println("Exception:\n" + errorReport.getStackTrace());

        new ErrorReportDialog(this, null, errorReport).setVisible(true);
    }

    public void sendErrorReport(ErrorReport errorReport) {
        this.aptiAPI.sendErrorReport(errorReport.generateReport());
    }

    @Override
    public void displayInfo(final String message) {
        JOptionPane.showMessageDialog(null, message, "Info", JOptionPane.INFORMATION_MESSAGE);
    }

    @Override
    public void displayError(final String message) {
        JOptionPane.showMessageDialog(null, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    @Override
    public void shutdown() {
    }
}
