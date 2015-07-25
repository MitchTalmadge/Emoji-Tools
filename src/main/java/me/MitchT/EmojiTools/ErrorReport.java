package me.MitchT.EmojiTools;

import java.io.PrintWriter;
import java.io.StringWriter;

public class ErrorReport {

    private final String threadName;
    private final Throwable exception;
    private String description;
    private String name;
    private String email;

    public ErrorReport(Thread thread, Throwable exception) {
        this.threadName = thread.getName();
        this.exception = exception;
    }

    public String getStackTrace() {
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        exception.printStackTrace(printWriter);
        return stringWriter.toString();
    }

    public String generateReport() {
        String report = "<b>BEGIN ERROR REPORT</b><br>" +
                "<br>" +
                "<i>Version: </i>" + EmojiTools.VERSION_STRING + "<br>" +
                "<i>User Name: </i>" + getName() + "<br>" +
                "<i>User Email: </i>" + getEmail() + "<br>" +
                "<i>User Comment: </i>" + getDescription() + "<br>" +
                "<br>" +
                "<i>Thread Name: </i>" + threadName + "<br>" +
                "<br>" +
                "<b>BEGIN STACK TRACE</b><br>" +
                "<br>" +
                getStackTrace() + "<br>" +
                "<b>END STACK TRACE</b><br>" +
                "<b>END ERROR REPORT</b>";

        return report;
    }

    public String getDescription() {
        return (description.isEmpty()) ? "No Comment." : description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getName() {
        return (name.isEmpty()) ? "No Name." : name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return (email.isEmpty()) ? "No Email." : email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

}
