package net.liveforcode.emojitools.packaging2;

import java.util.HashMap;
import java.util.List;

public class LigatureSet {

    private final String mainGlyphName;
    private final HashMap<List<String>, String> ligatureComponentsGlyphMap = new HashMap<>();

    public LigatureSet(String mainGlyphName)
    {
        this.mainGlyphName = mainGlyphName;
    }

    public String getMainGlyphName()
    {
        return mainGlyphName;
    }

    public void assignComponentsToGlyph(List<String> components, String glyph)
    {
        ligatureComponentsGlyphMap.put(components, glyph);
    }

    public String getGlyphNameFromComponents(List<String> components)
    {
        return ligatureComponentsGlyphMap.get(components);
    }

}
