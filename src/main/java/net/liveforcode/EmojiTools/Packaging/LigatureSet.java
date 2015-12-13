package net.liveforcode.EmojiTools.Packaging;

import java.util.HashMap;

public class LigatureSet {

    private final String mainGlyphName;
    private final HashMap<String[], String> ligatureComponentsGlyphMap = new HashMap<>();

    public LigatureSet(String mainGlyphName)
    {
        this.mainGlyphName = mainGlyphName;
    }

    public String getMainGlyphName()
    {
        return mainGlyphName;
    }

    public void assignComponentsToGlyph(String[] components, String glyph)
    {
        ligatureComponentsGlyphMap.put(components, glyph);
    }

    public String getGlyphNameFromComponents(String[] components)
    {
        return ligatureComponentsGlyphMap.get(components);
    }

}
