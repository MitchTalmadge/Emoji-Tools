# Emoji Tools
Multiple useful tools to help Android and iOS/OSX developers with creating and modifying Emoji Font files.

Features:
* Extracts Emojis from '.ttf' files used by Android, iOS, and OSX into individual '.png' files.
  * Extracted Emojis are named by Unicode names (Android: `unie537.png` iOS/OSX: `uE537.png`)
  * Extracted Emojis can automatically be stripped of their prefixes (`unie537.png` -> `E537.png`)
  * Extracted iOS CgBI (pincrushed) Emojis (which show up black on Windows) can automatically be converted to RGBA.
* Renamer tool which makes renaming Emojis very easy! You can change prefixes, capitalise/lowercase, etc.
* Converts from CgBI (pincrushed) Emojis to RGBA and vice versa.
* Ability to Package Emojis into Android and Apple Emoji Fonts
* Easy-to-use GUI with a professional look and feel.
* More!

# How to Use:
Check out [this tutorial](https://MitchTalmadge.com/2015/07/28/how-to-modify-android-emojis/) on how to modify Android Emojis using these tools!

1. Either download one of the precompiled .exe files or .jar files from the [downloads page](https://github.com/MitchTalmadge/Emoji-Tools/releases), or clone this repo into IntelliJ and create a .jar out of the sources.
2. Put the .exe or .jar wherever you'd like. Emojis will be extracted into a new folder which is created in the same directory as the jar file. You can choose the name of this folder using the GUI. By default, it is named `ExtractedEmojis`.
3. Double-click the .exe or .jar and Emoji Tools will open.

# How to Get Help / Make a Suggestion:
* Make a detailed ticket in the [issue tracker](https://github.com/MitchTalmadge/Emoji-Tools/issues).
* If it's very important, you can [email me](https://MitchTalmadge.com/contact/).
