package me.MitchT.EmojiExtractor.GUI;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;

public class MessagePanel extends JPanel implements ActionListener
{
    private JButton okayButton;
    private MainFrame mainFrame;
    
    public MessagePanel(MainFrame frame, String message)
    {
        this.mainFrame = frame;
        
        mainFrame.setMinimumSize(new Dimension(400,150));
        
        JLabel messageLabel = new JLabel(message);
        
        this.okayButton = new JButton("Okay");
        okayButton.addActionListener(this);
        
        GridBagLayout layout = new GridBagLayout();
        GridBagConstraints gbc = new GridBagConstraints();
        layout.setConstraints(this, gbc);
        this.setLayout(layout);
        
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.insets = new Insets(5, 5, 5, 5);
        this.add(messageLabel, gbc);
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 2;
        this.add(okayButton, gbc);
    }
    
    @Override
    public void actionPerformed(ActionEvent arg0)
    {
        if(arg0.getSource() instanceof JButton)
        {
            JButton button = (JButton) arg0.getSource();
            if(button.equals(okayButton))
            {
                mainFrame.showSelectionPanel();
            }
        }
    }
}
