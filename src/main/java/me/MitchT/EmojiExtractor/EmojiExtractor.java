package me.MitchT.EmojiExtractor;

import me.MitchT.EmojiExtractor.GUI.EmojiToolsGUI;

import javax.swing.*;
import java.io.File;
import java.net.URISyntaxException;

/**
 * Extracts Emojis from '.ttf' files used by Android, iOS, etc. into individual '.png' files.
 *
 * @author Mitch Talmadge (mitcht@aptitekk.com)
 */
public class EmojiExtractor {
    public static void main(String[] args) {
        String fontName = null;
        if (args.length > 0)
            fontName = args[0];

        final File font = new File(getRootDirectory() + "/" + fontName);

        try {
            UIManager.setLookAndFeel(
                    UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e) {
            e.printStackTrace();
        }

        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                new EmojiToolsGUI(font);
            }
        });
    }


    /**
     * Gets the root directory of the running jar. (The place where it was executed; for example, the Desktop).
     *
     * @return File containing absolute path to the root directory of the running jar.
     */
    public static File getRootDirectory() {
        try {
            return new File(EmojiExtractor.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath()).getParentFile().getAbsoluteFile();
        } catch (URISyntaxException e) {
            return null;
        }
    }
}
