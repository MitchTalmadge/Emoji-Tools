package me.MitchT.EmojiExtractor;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import me.MitchT.EmojiExtractor.GUI.MainFrame;
import me.MitchT.EmojiExtractor.GUI.ProgressPanel;

public class ExtractionThread extends Thread
{
    private static boolean[] searchBooleans = new boolean[8];
    private static final int[] prefix = new int[] { 0x89, 0x50, 0x4E, 0x47 };
    private static final int[] suffix = new int[] { 0x49, 0x45, 0x4E, 0x44, 0xAE, 0x42, 0x60, 0x82 };
    private static final File emojisDir = new File(EmojiExtractor.getRootDirectory() + "/ExtractedEmojis");
    
    private long currentBytePos = 0;
    private boolean running = true;
    private File filePath;
    private MainFrame mainFrame;
    private ProgressPanel progressPanel;
    
    public ExtractionThread(File filePath, MainFrame mainFrame, ProgressPanel progressPanel)
    {
        this.filePath = filePath;
        this.mainFrame = mainFrame;
        this.progressPanel = progressPanel;
    }
    
    @Override
    public void run()
    {
        try
        {
            InputStream inputStream = new FileInputStream(filePath);
            
            if(!emojisDir.exists())
            {
                emojisDir.mkdir();
            }
            
            int imageID = 0;
            while(inputStream.available() >= 1)
            {
                if(!running)
                {
                    inputStream.close();
                    return;
                }
                
                if(progressPanel.getStopped())
                    continue;
                
                updateProgress();
                if(checkForPrefix(inputStream.read()))
                {
                    imageID++;
                    extractEmoji(inputStream, imageID);
                }
            }
            
            System.out.println("No more Emojis to extract! All done! :)");
            mainFrame.showMessagePanel("No more Emojis to extract! All done! :)");
            
            inputStream.close();
        }
        catch(FileNotFoundException e)
        {
            System.out.println(filePath.getName()+" not found!");
            mainFrame.showMessagePanel(filePath.getName()+" not found!");
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
    private void extractEmoji(InputStream inputStream, int emojiID)
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
                currentBytePos++;
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
    private boolean checkForPrefix(int val)
    {
        currentBytePos++;
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
    private boolean checkForSuffix(int val)
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
    private void resetSearchBooleans()
    {
        searchBooleans = new boolean[8];
    }
    
    private void updateProgress()
    {
        int progress = (int) (((double)currentBytePos / filePath.length())*100);
        progressPanel.setProgress(progress);
    }
    
    public void endExtraction()
    {
        running = false;
    }
}
