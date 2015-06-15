# EmojiExtractor
Extracts Emojis from '.ttf' files used by Android, iOS, etc. into individual '.png' files.

# How to Use:
1. Clone into Eclipse and create a Runnable Jar, or download one of the precompiled jars from the [downloads page](https://github.com/MitchTalmadge/EmojiExtractor/releases).
2. Put the jar wherever you'd like. Emojis will be extracted into a new folder which is created in the same directory as the jar file, named `ExtractedEmojis`.
3. Either double-click the jar and follow the instructions on the GUI, or start the jar from the command line using `java -jar EmojiExtractor.jar yourFileName.ttf`. If the ttf file does not exist, the ttf selection window will be brought up. If it exists, it will automatically start extraction.

## Important Note:
At the time of writing, Apple (OS X / iOS) Emojis will be extracted very quickly and the resulting PNG files will be named by the Unicode name of the Emoji (for example, `uE537.png`). Android Emojis take longer to extract and will be named sequentially. (for example, `1.png`, `2.png`, etc.). 

This is due to a lack of support for Android Emojis. Android Emojis will gain speed improvements and Unicode names in a future release.

#How to Get Help / Make a Suggestion:
* Make a detailed ticket in the [issue tracker](https://github.com/MitchTalmadge/EmojiExtractor/issues).
* If it's very important, you can email me (Mitch Talmadge).
