package me.MitchT.EmojiExtractor;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;

/**
 * Extracts Emojis from '.ttf' files used by Android, iOS, etc. into individual '.png' files.
 * @author Mitch Talmadge (mitcht@aptitekk.com)
 */
public class EmojiExtractor
{
    private static boolean[] searchBooleans = new boolean[8];
    private static final int[] prefix = new int[] { 0x89, 0x50, 0x4E, 0x47 };
    private static final int[] suffix = new int[] { 0x49, 0x45, 0x4E, 0x44, 0xAE, 0x42, 0x60, 0x82 };
    
    private static final File emojisDir = new File(getRootDirectory() + "/ExtractedEmojis");
    
    public static void main(String[] args)
    {
        String fileName = null;
        if(args.length > 0)
            fileName = args[0];
        if(fileName == null)
        {
            fileName = "NotoColorEmoji.ttf";
        }
        
        try
        {
            InputStream inputStream = new FileInputStream(new File(getRootDirectory() + "/" + fileName));
            
            if(!emojisDir.exists())
            {
                emojisDir.mkdir();
            }
            
            int imageID = 0;
            while(inputStream.available() >= 1)
            {
                if(checkForPrefix(inputStream.read()))
                {
                    imageID++;
                    extractEmoji(inputStream, imageID);
                }
            }
            
            System.out.println("No more Emojis to extract! All done! :)");
            
            inputStream.close();
        }
        catch(FileNotFoundException e)
        {
            e.printStackTrace();
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }
    }
    
    /**
     * Extracts the next emoji from the inputStream into a '.png' file inside of <i>emojisDir</i>.
     * @param inputStream The inputStream to read from.
     * @param emojiID The ID of the emoji (used for naming conventions).
     */
    private static void extractEmoji(InputStream inputStream, int emojiID)
    {
        resetSearchBooleans();
        
        System.out.println("Extracting Emoji #" + emojiID + " to '" + emojiID + ".png'");
        try
        {
            FileOutputStream outputStream = new FileOutputStream(new File(emojisDir, emojiID + ".png"));
            for(int i = 0; i < prefix.length; i++)
                outputStream.write(prefix[i]);
            
            while(inputStream.available() >= 1)
            {
                int b = inputStream.read();
                outputStream.write(b);
                if(checkForSuffix(b))
                    break;
            }
            
            outputStream.close();
            
            resetSearchBooleans();
        }
        catch(FileNotFoundException e)
        {
            e.printStackTrace();
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }
        
    }
    
    /**
     * Checks bytes being read to see if they match the defined prefix. Ensures order and accuracy.
     * @param val Byte to check.
     * @return True if all bytes up to this one match all bytes in the defined prefix.<br>
     * False if the entire prefix has not been matched yet, or one of the bytes did not match.
     */
    private static boolean checkForPrefix(int val)
    {
        for(int i = 0; i < prefix.length; i++)
        {
            if(!searchBooleans[i])
                if(prefix[i] == val)
                {
                    searchBooleans[i] = true;
                    break;
                }
                else
                {
                    resetSearchBooleans();
                    break;
                }
        }
        if(searchBooleans[prefix.length - 1])
            return true;
        return false;
    }
    
    /**
     * Checks bytes being read to see if they match the defined suffix. Ensures order and accuracy.
     * @param val Byte to check.
     * @return True if all bytes up to this one match all bytes in the defined suffix.<br>
     * False if the entire suffix has not been matched yet, or one of the bytes did not match.
     */
    private static boolean checkForSuffix(int val)
    {
        for(int i = 0; i < suffix.length; i++)
        {
            if(!searchBooleans[i])
                if(suffix[i] == val)
                {
                    searchBooleans[i] = true;
                    break;
                }
                else
                {
                    resetSearchBooleans();
                    break;
                }
        }
        if(searchBooleans[suffix.length - 1])
            return true;
        return false;
    }
    
    /**
     * Must be called at the beginning of checking bytes against the prefix or suffix. Clears all previously checked bytes.
     */
    private static void resetSearchBooleans()
    {
        searchBooleans = new boolean[8];
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
