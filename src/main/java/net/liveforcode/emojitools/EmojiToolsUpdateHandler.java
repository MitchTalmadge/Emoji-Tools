package net.liveforcode.emojitools;

import com.aptitekk.aptiapi.AptiAPIUpdateHandler;
import net.liveforcode.emojitools.gui.aptiapi.UpdateNoticeDialog;

public class EmojiToolsUpdateHandler extends AptiAPIUpdateHandler {

    @Override
    public void onUpdateAvailable(String newVersion, String changeLog, String downloadUrl) {
        new UpdateNoticeDialog(newVersion, changeLog, downloadUrl).display();
    }
}
