from __future__ import print_function, division, absolute_import

from fontTools import ttLib
from fontTools.misc.py23 import *

superclass = ttLib.getTableClass("TSI0")

class table_T_S_I__2(superclass):

	dependencies = ["TSI3"]
