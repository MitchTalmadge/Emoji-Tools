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

package net.liveforcode.EmojiTools;

import com.AptiTekk.AptiAPI.AptiAPI;
import com.AptiTekk.AptiAPI.ErrorHandler;
import net.liveforcode.EmojiTools.GUI.EmojiToolsGUI;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.net.URISyntaxException;

public class EmojiTools {

    private static final Image logoImage = new ImageIcon(EmojiTools.class.getResource("/Images/EmojiToolsLogo.png")).getImage();
    private static final AptiAPI aptiAPI = new AptiAPI(Versioning.APTIAPI_PROJECT_ID, logoImage);
    private static final ErrorHandler errorHandler = aptiAPI.getErrorHandler();

    public static void main(String[] args) {
        System.setProperty("python.cachedir.skip", "false");
        System.setProperty("python.console.encoding", "UTF-8");

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
                new EmojiToolsGUI(aptiAPI, font);
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
