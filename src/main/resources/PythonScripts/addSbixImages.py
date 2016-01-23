#!/usr/bin/env python

"""
Demo script: Add an sbix table to an existing, specially crafted font (see end of file for hints)
Jens Kutilek, 2013-05-24
"""

import sys
from fontTools import ttLib
from fontTools.ttLib.tables.sbixBitmapSet import BitmapSet
from fontTools.ttLib.tables.sbixBitmap import Bitmap
from os import walk
from os.path import join


def main():
    # open the source font
    f = ttLib.TTFont(sys.argv[1]) #Arg 1 = Original Font Location

    # mapping of image size to directory name
    sets = {
        20: sys.argv[2]+"/set_20", 32: sys.argv[2]+"/set_32", 40: sys.argv[2]+"/set_40",
    48: sys.argv[2]+"/set_48", 64: sys.argv[2]+"/set_64", 96: sys.argv[2]+"/set_96",
    160: sys.argv[2]+"/set_160"

    }

    sbix = ttLib.newTable("sbix")
    go = f.getGlyphOrder()

    for s, d in sets.iteritems():
        # make an empty bitmap set for current image size
        mySet = BitmapSet(size=s)
        for root, dirs, files in walk(d, topdown=False):
            for myFile in files:
                if myFile[-4:] == ".png":
                    # use file name without suffix as glyph name
                    # FIXME: filename clashes with case-sensitive glyph names
                    glyphname = myFile[:-4]
                    if glyphname in go:  # only use files that have a matching glyph in the source font
                        print glyphname
                        img = open(join(root, myFile), "rb")
                        imgData = img.read()
                        img.close()
                        # make a bitmap record for the current image
                        myBitmap = Bitmap(glyphName=glyphname, imageFormatTag="png ", imageData=imgData)
                        # add bitmap to current bitmap set
                        mySet.bitmaps[glyphname] = myBitmap
        sbix.bitmapSets[s] = mySet
    # add sbix table to the source font
    f["sbix"] = sbix
    # save font under new name
    f.save(sys.argv[2]+"/AppleColorEmoji@2x.ttf") #Arg 2 = Output Directory Location


if __name__ == "__main__":
    main()

"""
WARNING

Glyph records need a special format that determines the scaling of the bitmap.
If you don't do this, the bitmaps will not be displayed.

<TTGlyph name="u1F433" xMin="0" yMin="-80" xMax="180" yMax="220">
  <contour>
    <pt x="0" y="-80" on="1"/>
  </contour>
  <contour>
    <pt x="180" y="220" on="1"/>
  </contour>
  <instructions><assembly>
    </assembly></instructions>
</TTGlyph>

If there is a "real" outline in the glyph, it will be displayed on top of the bitmap.
In the demo font, the outlines are only mapped to Unicode values in the Windows cmap, so Mac users should only see the embedded images.

"""
