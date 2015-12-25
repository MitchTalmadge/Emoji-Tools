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

package com.aptitekk.aptiapi;

import java.io.PrintWriter;
import java.io.StringWriter;

public class ErrorReport {

    private final String threadName;
    private final Throwable exception;
    private String version;
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
                "<i>Version: </i>" + getVersion() + "<br>" +
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
        return (description == null || description.isEmpty()) ? "No Comment." : description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getName() {
        return (name == null || name.isEmpty()) ? "No Name." : name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return (email == null || email.isEmpty()) ? "No Email." : email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }
}
