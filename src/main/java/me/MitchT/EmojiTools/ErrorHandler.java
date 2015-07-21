package me.MitchT.EmojiTools;

import me.MitchT.EmojiTools.GUI.ErrorReportDialog;

public class ErrorHandler implements Thread.UncaughtExceptionHandler {

    @Override
    public void uncaughtException(Thread t, Throwable e) {
        ErrorReport errorReport = new ErrorReport(e);

        System.out.println("ERROR OCCURRED!");
        System.out.println("Thread Name: " + t.getName());
        System.out.println("Exception:\n" + errorReport.getStackTrace());

        new ErrorReportDialog(null, errorReport).setVisible(true);
    }
}
