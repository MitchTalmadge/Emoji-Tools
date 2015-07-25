package me.MitchT.EmojiTools;

import com.AptiTekk.AptiAPI.AptiAPI;
import com.AptiTekk.AptiAPI.AptiAPIListener;
import me.MitchT.EmojiTools.GUI.ErrorReportDialog;

public class ErrorHandler implements Thread.UncaughtExceptionHandler, AptiAPIListener {

    private final AptiAPI aptiAPI;

    public ErrorHandler() {
        this.aptiAPI = new AptiAPI(EmojiTools.PROJECT_ID);
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
    public void displayInfo(String message) {

    }

    @Override
    public void displayError(String message) {

    }

    @Override
    public void shutdown() {
    }
}
