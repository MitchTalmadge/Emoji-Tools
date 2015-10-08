package me.MitchT.EmojiTools;

import com.sun.istack.internal.NotNull;

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
                    listener.write(String.valueOf((char) b));
                }
                originalOutStream.write(b);
            }

            @Override
            @NotNull
            public void write(byte[] b, int off, int len) throws IOException {
                for (ConsoleListener listener : consoleListeners) {
                    listener.write(new String(b, off, len));
                }
                originalOutStream.write(b, off, len);
            }

            @Override
            @NotNull
            public void write(byte[] b) throws IOException {
                write(b, 0, b.length);
            }
        };

        this.errStream = new OutputStream() {
            @Override
            public void write(final int b) throws IOException {
                for (ConsoleListener listener : consoleListeners) {
                    listener.write(String.valueOf((char) b));
                }
                originalErrStream.write(b);
            }

            @Override
            @NotNull
            public void write(byte[] b, int off, int len) throws IOException {
                for (ConsoleListener listener : consoleListeners) {
                    listener.write(new String(b, off, len));
                }
                originalErrStream.write(b, off, len);
            }

            @Override
            @NotNull
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
        void write(String message);
    }

}
