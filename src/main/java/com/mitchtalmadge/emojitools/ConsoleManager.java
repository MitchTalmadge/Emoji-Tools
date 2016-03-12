/*
 * Emoji Tools helps users and developers of Android, iOS, and OS X extract, modify, and repackage Emoji fonts.
 * Copyright (C) 2015 - 2016 Mitch Talmadge
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

package com.mitchtalmadge.emojitools;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

public class ConsoleManager {
    private final ArrayList<ConsoleListener> consoleListeners = new ArrayList<>();
    private final OutputStream outStream;
    private final OutputStream errStream;
    private final PrintStream originalOutStream;
    private final PrintStream originalErrStream;

    public ConsoleManager() {
        this.originalOutStream = new PrintStream(System.out);
        this.originalErrStream = new PrintStream(System.err);

        this.outStream = new OutputStream() {

            @Override
            public void write(final int b) throws IOException {
                for (ConsoleListener listener : consoleListeners) {
                    listener.sysOut(String.valueOf((char) b));
                }
                originalOutStream.write(b);
            }

            @Override
            public void write(byte[] b, int off, int len) throws IOException {
                for (ConsoleListener listener : consoleListeners) {
                    listener.sysOut(new String(b, off, len));
                }
                originalOutStream.write(b, off, len);
            }

            @Override
            public void write(byte[] b) throws IOException {
                write(b, 0, b.length);
            }
        };

        this.errStream = new OutputStream() {
            @Override
            public void write(final int b) throws IOException {
                for (ConsoleListener listener : consoleListeners) {
                    listener.sysErr(String.valueOf((char) b));
                }
                originalErrStream.write(b);
            }

            @Override
            public void write(byte[] b, int off, int len) throws IOException {
                for (ConsoleListener listener : consoleListeners) {
                    listener.sysErr(new String(b, off, len));
                }
                originalErrStream.write(b, off, len);
            }

            @Override
            public void write(byte[] b) throws IOException {
                write(b, 0, b.length);
            }
        };

        try {
            System.setOut(new PrintStream(this.outStream, true, "UTF-8"));
            System.setErr(new PrintStream(this.errStream, true, "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    public void addConsoleListener(ConsoleListener listener) {
        if (!this.consoleListeners.contains(listener))
            this.consoleListeners.add(listener);
    }

    public void removeConsoleListener(ConsoleListener listener) {
        if (this.consoleListeners.contains(listener))
            this.consoleListeners.remove(listener);
    }

    public interface ConsoleListener {
        void sysOut(String message);

        void sysErr(String message);
    }

}
