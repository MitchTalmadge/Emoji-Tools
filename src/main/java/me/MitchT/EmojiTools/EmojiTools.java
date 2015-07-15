package me.MitchT.EmojiTools;

import me.MitchT.EmojiTools.GUI.EmojiToolsGUI;

import javax.swing.*;
import java.io.File;
import java.net.URISyntaxException;

/**
 * Extracts Emojis from '.ttf' files used by Android, iOS, etc. into individual '.png' files.
 *
 * @author Mitch Talmadge (mitcht@aptitekk.com)
 */
public class EmojiTools {
    public static void main(String[] args) {
        System.setProperty("python.cachedir.skip", "false");

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

    public static File getRootDirectory() {
        try {
            return new File(EmojiTools.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath()).getParentFile().getAbsoluteFile();
        } catch (URISyntaxException e) {
            return null;
        }
    }

}
