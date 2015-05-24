# EmojiExtractor
Extracts Emojis from '.ttf' files used by Android, iOS, etc. into individual '.png' files.

# How to Use:
1. Clone into Eclipse and create a Runnable Jar, or download one of the precompiled jars from the [downloads page](https://github.com/MitchTalmadge/EmojiExtractor/releases).
2. Put the jar into the same directory as your Emoji ttf file.
3. Either name your Emoji ttf file `NotoColorEmoji.ttf` and double click the jar, or start the jar from the command line using `java -jar EmojiExtractor.jar yourFileName.ttf`

Note: You can also start the jar from the command line using `java -jar EmojiExtractor.jar`, without supplying a ttf file name, and it will default to using `NotoColorEmoji.ttf`.

Starting the jar from the command line will show progress and any errors.

Emojis will be extracted into a new folder which is created in the same directory as the ttf file, named `ExtractedEmojis`.
