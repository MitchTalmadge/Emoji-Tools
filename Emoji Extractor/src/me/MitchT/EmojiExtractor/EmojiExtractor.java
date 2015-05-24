package me.MitchT.EmojiExtractor;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;

import javax.swing.SwingUtilities;

import me.MitchT.EmojiExtractor.GUI.MainFrame;

/**
 * Extracts Emojis from '.ttf' files used by Android, iOS, etc. into individual '.png' files.
 * @author Mitch Talmadge (mitcht@aptitekk.com)
 */
public class EmojiExtractor
{
    public static void main(String[] args)
    {
        String fileName = null;
        if(args.length > 0)
            fileName = args[0];
        
        final File filePath = new File(getRootDirectory()+"/"+fileName);
        
        SwingUtilities.invokeLater(new Runnable()
        {
            
            @Override
            public void run()
            {
                new MainFrame(filePath);
            }
        });
    }
    
    
    /**
     * Gets the root directory of the running jar. (The place where it was executed; for example, the Desktop).
     * @return File containing absolute path to the root directory of the running jar.
     */
    public static File getRootDirectory()
    {
        try
        {
            File root = new File(EmojiExtractor.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath()).getParentFile().getAbsoluteFile();
            return root;
        }
        catch(URISyntaxException e)
        {
            return null;
        }
    }
}
