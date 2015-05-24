package me.MitchT.EmojiExtractor.GUI;

import java.awt.CardLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import me.MitchT.EmojiExtractor.ExtractionThread;

public class MainFrame extends JFrame
{
    private CardLayout cardLayout;
    private ExtractionThread extractionThread;
    private SelectionPanel selectionPanel;

    public MainFrame(File filePath)
    {
        setTitle("Emoji Extractor");
        this.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        this.addWindowListener(new WindowAdapter()
        {
            @Override
            public void windowClosing(WindowEvent e) {
                if(extractionThread != null && extractionThread.isAlive())
                {
                    extractionThread.endExtraction();
                    try
                    {
                        extractionThread.join();
                    }
                    catch(InterruptedException e1)
                    {
                    }
                    extractionThread = null;
                }
                System.exit(0);
            }
        });
        
        this.selectionPanel = new SelectionPanel(this);
        
        this.setContentPane(selectionPanel);
        this.pack();
        this.setLocationRelativeTo(null);
        this.setVisible(true);

        if(filePath != null && filePath.exists())
        {
            startExtraction(filePath);
        }
    }
    
    public void showSelectionPanel()
    {
        if(extractionThread != null && extractionThread.isAlive())
        {
            extractionThread.endExtraction();
        }
        this.setContentPane(selectionPanel);
        this.pack();
    }
    
    public void showMessagePanel(final String message)
    {
        if(extractionThread != null && extractionThread.isAlive())
        {
            extractionThread.endExtraction();
        }
        final MainFrame mainFrame = this;
        SwingUtilities.invokeLater(new Runnable()
        {
            
            @Override
            public void run()
            {
                setContentPane(new MessagePanel(mainFrame, message));
                pack();
            }
        });
        
    }
    
    public void startExtraction(File filePath)
    {
        ProgressPanel progressPanel = new ProgressPanel(this);
        this.setContentPane(progressPanel);
        this.pack();
        this.extractionThread = new ExtractionThread(filePath, this, progressPanel);
        extractionThread.start();
    }
}
