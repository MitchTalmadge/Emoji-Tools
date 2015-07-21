package me.MitchT.EmojiTools;

import me.MitchT.EmojiTools.GUI.EmojiToolsGUI;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.net.URISyntaxException;

/**
 * Extracts Emojis from '.ttf' files used by Android, iOS, etc. into individual '.png' files.
 *
 * @author Mitch Talmadge (mitcht@aptitekk.com)
 */
public class EmojiTools {

    public static final String version = "V1.6";
    private static final Image logoImage = new ImageIcon(EmojiTools.class.getResource("/Images/EmojiToolsLogo.png")).getImage();
    private static final ErrorHandler errorHandler = new ErrorHandler();

    public static void main(String[] args) {
        System.setProperty("python.cachedir.skip", "false");

        Thread.setDefaultUncaughtExceptionHandler(errorHandler);

        String fontName = null;
        if (args.length > 0)
            fontName = args[0];

        final File font = new File(getRootDirectory() + "/" + fontName);

        try {
            UIManager.setLookAndFeel(
                    UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e) {
            submitError(Thread.currentThread(), e);
        }

        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                new EmojiToolsGUI(font);
            }
        });
    }

    public static Image getLogoImage() {
        return logoImage;
    }

    public static void submitError(Thread thread, Throwable throwable) {
        errorHandler.uncaughtException(thread, throwable);
    }

    public static File getRootDirectory() {
        try {
            return new File(EmojiTools.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath()).getParentFile().getAbsoluteFile();
        } catch (URISyntaxException e) {
            submitError(Thread.currentThread(), e);
            return null;
        }
    }

}
