#!/usr/bin/python

import glob
import sys
from fontTools import ttx
from png import PNG

if len(sys.argv) < 4:
    print >> sys.stderr, """
Usage:

add_glyphs.py font.ttx out-font.ttx strike-prefix...

This will search for files that have strike-prefix followed
by a hex number, and end in ".png".  For example, if strike-prefix
is "icons/uni", then files with names like "icons/uni1f4A9.png" will
be loaded.  The script then adds cmap and htmx entries for the Unicode
characters found.  The advance width will be chosen based on image
aspect ratio.  If Unicode values outside the BMP are desired, the
existing cmap table should be of the appropriate (format 12) type.
Only the first cmap table is modified.
"""
    sys.exit(1)

in_file = sys.argv[1]
out_file = sys.argv[2]
img_prefix = sys.argv[3]
del sys.argv

font = ttx.TTFont()
font.importXML(in_file)

img_files = {}
glb = "%s*.png" % img_prefix
print "Looking for images matching '%s'." % glb
for img_file in glob.glob(glb):
    if "_" in img_file[len(img_prefix):-4]:
        continue
    u = int(img_file[len(img_prefix):-4], 16)
    img_files[u] = img_file
if not img_files:
    raise Exception("No image files found in '%s'." % glb)

ascent = font['hhea'].ascent
descent = -font['hhea'].descent

g = font['GlyphOrder'].glyphOrder
c = font['cmap'].tables[0].cmap
h = font['hmtx'].metrics
for (u, filename) in img_files.items():
    print "Adding glyph for U+%04X" % u
    n = "uni%04x" % u
    g.append(n)
    c[u] = n
    (img_width, img_height) = PNG(filename).get_size()
    advance = int(round((float(ascent + descent) * img_width / img_height)))
    h[n] = [advance, 0]

font.saveXML(out_file)
