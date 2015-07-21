package me.MitchT.EmojiTools;

import java.io.PrintWriter;
import java.io.StringWriter;

public class ErrorReport {

    private final Throwable exception;
    private String description;
    private String name;
    private String email;

    public ErrorReport(Throwable exception) {
        this.exception = exception;
    }

    public void sendReport() {
        //TODO: send report
    }

    public String getStackTrace() {
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        exception.printStackTrace(printWriter);
        return stringWriter.toString();
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

}
