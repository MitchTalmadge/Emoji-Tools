# EmojiExtractor
Extracts Emojis from '.ttf' files used by Android, iOS, etc. into individual '.png' files.

# How to Use:
1. Clone into Eclipse and create a Runnable Jar, or download one of the precompiled jars from the [downloads page](https://github.com/MitchTalmadge/EmojiExtractor/releases).
2. Put the jar into the same directory as your Emoji ttf file.
3. 
   * FOR V1.0: Either name your Emoji ttf file `NotoColorEmoji.ttf` and double-click the jar, or start the jar from the command line using `java -jar EmojiExtractor.jar yourFileName.ttf`
   * FOR V1.1: Either double-click the jar and follow the instructions on the GUI, or start the jar from the command line using `java -jar EmojiExtractor.jar yourFileName.ttf`. If the ttf file does not exist, the ttf selection window will be brought up. If it exists, it will automatically start extraction.

Emojis will be extracted into a new folder which is created in the same directory as the ttf file, named `ExtractedEmojis`.
