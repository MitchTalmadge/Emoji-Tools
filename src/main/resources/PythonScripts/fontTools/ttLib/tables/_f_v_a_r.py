from __future__ import print_function, division, absolute_import
from fontTools.misc.py23 import *
from fontTools.misc import sstruct
from fontTools.misc.fixedTools import fixedToFloat, floatToFixed
from fontTools.misc.textTools import safeEval, num2binary, binary2num
from fontTools.ttLib import TTLibError
from . import DefaultTable
import struct


# Apple's documentation of 'fvar':
# https://developer.apple.com/fonts/TrueType-Reference-Manual/RM06/Chap6fvar.html

FVAR_HEADER_FORMAT = """
    > # big endian
    version:        L
    offsetToData:   H
    countSizePairs: H
    axisCount:      H
    axisSize:       H
    instanceCount:  H
    instanceSize:   H
"""

FVAR_AXIS_FORMAT = """
    > # big endian
    axisTag:        4s
    minValue:       16.16F
    defaultValue:   16.16F
    maxValue:       16.16F
    flags:          H
    nameID:         H
"""

FVAR_INSTANCE_FORMAT = """
    > # big endian
    nameID:     H
    flags:      H
"""

class table__f_v_a_r(DefaultTable.DefaultTable):
    dependencies = ["name"]

    def __init__(self, tag=None):
        DefaultTable.DefaultTable.__init__(self, tag)
        self.axes = []
        self.instances = []

    def compile(self, ttFont):
        header = {
            "version": 0x00010000,
            "offsetToData": sstruct.calcsize(FVAR_HEADER_FORMAT),
            "countSizePairs": 2,
            "axisCount": len(self.axes),
            "axisSize": sstruct.calcsize(FVAR_AXIS_FORMAT),
            "instanceCount": len(self.instances),
            "instanceSize": sstruct.calcsize(FVAR_INSTANCE_FORMAT) + len(self.axes) * 4
        }
        result = [sstruct.pack(FVAR_HEADER_FORMAT, header)]
        result.extend([axis.compile() for axis in self.axes])
        axisTags = [axis.axisTag for axis in self.axes]
        result.extend([instance.compile(axisTags) for instance in self.instances])
        return bytesjoin(result)

    def decompile(self, data, ttFont):
        header = {}
        headerSize = sstruct.calcsize(FVAR_HEADER_FORMAT)
        header = sstruct.unpack(FVAR_HEADER_FORMAT, data[0:headerSize])
        if header["version"] != 0x00010000:
            raise TTLibError("unsupported 'fvar' version %04x" % header["version"])
        pos = header["offsetToData"]
        axisSize = header["axisSize"]
        for _ in range(header["axisCount"]):
            axis = Axis()
            axis.decompile(data[pos:pos+axisSize])
            self.axes.append(axis)
            pos += axisSize
        instanceSize = header["instanceSize"]
        axisTags = [axis.axisTag for axis in self.axes]
        for _ in range(header["instanceCount"]):
            instance = NamedInstance()
            instance.decompile(data[pos:pos+instanceSize], axisTags)
            self.instances.append(instance)
            pos += instanceSize

    def toXML(self, writer, ttFont, progress=None):
        for axis in self.axes:
            axis.toXML(writer, ttFont)
        for instance in self.instances:
            instance.toXML(writer, ttFont)

    def fromXML(self, name, attrs, content, ttFont):
        if name == "Axis":
            axis = Axis()
            axis.fromXML(name, attrs, content, ttFont)
            self.axes.append(axis)
        elif name == "NamedInstance":
            instance = NamedInstance()
            instance.fromXML(name, attrs, content, ttFont)
            self.instances.append(instance)

class Axis(object):
    def __init__(self):
        self.axisTag = None
        self.nameID = 0
        self.flags = 0  # not exposed in XML because spec defines no values
        self.minValue = -1.0
        self.defaultValue = 0.0
        self.maxValue = 1.0

    def compile(self):
        return sstruct.pack(FVAR_AXIS_FORMAT, self)

    def decompile(self, data):
        sstruct.unpack2(FVAR_AXIS_FORMAT, data, self)

    def toXML(self, writer, ttFont):
        name = ttFont["name"].getDebugName(self.nameID)
        if name is not None:
            writer.newline()
            writer.comment(name)
            writer.newline()
        writer.begintag("Axis")
        writer.newline()
        for tag, value in [("AxisTag", self.axisTag),
                           ("MinValue", str(self.minValue)),
                           ("DefaultValue", str(self.defaultValue)),
                           ("MaxValue", str(self.maxValue)),
                           ("NameID", str(self.nameID))]:
            writer.begintag(tag)
            writer.write(value)
            writer.endtag(tag)
            writer.newline()
        writer.endtag("Axis")
        writer.newline()

    def fromXML(self, name, _attrs, content, ttFont):
        assert(name == "Axis")
        for tag, _, value in filter(lambda t: type(t) is tuple, content):
            value = ''.join(value)
            if tag == "AxisTag":
                self.axisTag = value
            elif tag in ["MinValue", "DefaultValue", "MaxValue", "NameID"]:
                setattr(self, tag[0].lower() + tag[1:], safeEval(value))

class NamedInstance(object):
    def __init__(self):
        self.nameID = 0
        self.flags = 0  # not exposed in XML because spec defines no values
        self.coordinates = {}

    def compile(self, axisTags):
        result = [sstruct.pack(FVAR_INSTANCE_FORMAT, self)]
        for axis in axisTags:
            fixedCoord = floatToFixed(self.coordinates[axis], 16)
            result.append(struct.pack(">l", fixedCoord))
        return bytesjoin(result)

    def decompile(self, data, axisTags):
        sstruct.unpack2(FVAR_INSTANCE_FORMAT, data, self)
        pos = sstruct.calcsize(FVAR_INSTANCE_FORMAT)
        for axis in axisTags:
            value = struct.unpack(">l", data[pos : pos + 4])[0]
            self.coordinates[axis] = fixedToFloat(value, 16)
            pos += 4

    def toXML(self, writer, ttFont):
        name = ttFont["name"].getDebugName(self.nameID)
        if name is not None:
            writer.newline()
            writer.comment(name)
            writer.newline()
        writer.begintag("NamedInstance", nameID=self.nameID)
        writer.newline()
        for axis in ttFont["fvar"].axes:
            writer.simpletag("coord", axis=axis.axisTag,
                             value=self.coordinates[axis.axisTag])
            writer.newline()
        writer.endtag("NamedInstance")
        writer.newline()

    def fromXML(self, name, attrs, content, ttFont):
        assert(name == "NamedInstance")
        self.nameID = safeEval(attrs["nameID"])
        for tag, elementAttrs, _ in filter(lambda t: type(t) is tuple, content):
            if tag == "coord":
                self.coordinates[elementAttrs["axis"]] = safeEval(elementAttrs["value"])
