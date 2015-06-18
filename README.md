# EmojiExtractor
Extracts Emojis from '.ttf' files used by Android, iOS, etc. into individual '.png' files.

Emojis are extracted VERY quickly with an easy-to-use GUI! They are also named by their Unicode names, not by order of extraction!
(They will be named like `uE537.png` rather than `1.png`, `2.png`, etc.)

# How to Use:
1. Clone into IntelliJ and create a Jar out of the sources, or download one of the precompiled Jars from the [downloads page](https://github.com/MitchTalmadge/EmojiExtractor/releases).
2. Put the jar wherever you'd like. Emojis will be extracted into a new folder which is created in the same directory as the jar file, named `ExtractedEmojis`.
3. Either double-click the jar and follow the instructions on the GUI, or start the jar from the command line using `java -jar EmojiExtractor.jar yourFileName.ttf`. If the ttf file does not exist, the ttf selection window will be brought up. If it exists, it will automatically start extraction.

# How to Get Help / Make a Suggestion:
* Make a detailed ticket in the [issue tracker](https://github.com/MitchTalmadge/EmojiExtractor/issues).
* If it's very important, you can email me (Mitch Talmadge).
