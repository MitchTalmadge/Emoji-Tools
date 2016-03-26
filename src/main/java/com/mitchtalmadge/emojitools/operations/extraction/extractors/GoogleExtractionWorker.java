/*
 * Emoji Tools helps users and developers of Android, iOS, and OS X extract, modify, and repackage Emoji fonts.
 * Copyright (C) 2015 - 2016 Mitch Talmadge
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Contact Mitch Talmadge at mitcht@liveforcode.net
 */

package com.mitchtalmadge.emojitools.operations.extraction.extractors;

import com.mitchtalmadge.emojitools.EmojiTools;
import com.mitchtalmadge.emojitools.gui.dialogs.OperationProgressDialog;
import com.mitchtalmadge.emojitools.operations.FontType;
import com.mitchtalmadge.emojitools.operations.Operation;
import com.mitchtalmadge.emojitools.operations.extraction.Ligature;
import org.python.core.PyList;
import org.python.core.PyType;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.bind.DatatypeConverter;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.io.FileOutputStream;
import java.util.*;

public class GoogleExtractionWorker extends ExtractionWorker {

    public GoogleExtractionWorker(Operation operation, OperationProgressDialog operationProgressDialog, File fontFile, File extractionDirectory, List<String> tableNames, List<Integer> tableOffsets, List<Integer> tableLengths) {
        super(operation, operationProgressDialog, fontFile, extractionDirectory, tableNames, tableOffsets, tableLengths, true);
    }

    @Override
    protected Boolean doWork() throws Exception {
        //---- ttx.py ----//
        setProgressIndeterminate();
        appendMessageToDialog("Decompiling Emoji Font... Please wait...");

        //Set sys.argv
        ArrayList<String> argvList = new ArrayList<>();
        argvList.add("package.py");                                         //Python Script Name
        argvList.add("-o");                                                 //Output flag
        argvList.add(extractionDirectory.getAbsolutePath()
                + "/" + "font.ttx");   //Output ttx path
        argvList.add(fontFile.getAbsolutePath());                               //Input ttf path

        getJythonHandler().getPySystemState().argv = new PyList(PyType.fromClass(String.class), argvList);

        if (isCancelled())
            return false;

        //Execute
        getJythonHandler().getPythonInterpreter().execfile(getJythonHandler().getTempDirectory().getAbsolutePath()
                + "/PythonScripts/package.py");

        getJythonHandler().getPythonInterpreter().cleanup();

        File ttxFile = new File(extractionDirectory, "font.ttx");

        if (!ttxFile.exists()) {
            EmojiTools.showErrorDialog("font.ttx File Missing!", "The font.ttx file appears to be missing. Did it get deleted?");
            return false;
        }

        Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(ttxFile);

        //Check for ttFont element
        Element rootElement = document.getDocumentElement();
        if (!rootElement.getTagName().equals("ttFont")) {
            showErrorDialog("Invalid '.ttx' File (Incorrect Root Element)", "The '.ttx' file in the emojis directory appears to have been incorrectly modified. Packaging cannot continue.");
            return false;
        }

        //Check for cmap element
        Element cmapElement = (Element) rootElement.getElementsByTagName("cmap").item(0);
        if (cmapElement == null) {
            showErrorDialog("Invalid '.ttx' File (Missing cmap Table)", "The '.ttx' file in the emojis directory appears to have been incorrectly modified. Packaging cannot continue.");
            return false;
        }

        //Check for cmap_format_12 element
        Element cmapFormat12Element = (Element) cmapElement.getElementsByTagName("cmap_format_12").item(0);
        if (cmapFormat12Element == null) {
            showErrorDialog("Invalid '.ttx' File (Missing cmap_format_12 Table)", "The '.ttx' file in the emojis directory appears to have been incorrectly modified. Packaging cannot continue.");
            return false;
        }

        Map<String, String> cmap = new HashMap<>();
        NodeList cmapMapElementList = cmapElement.getElementsByTagName("map");
        for (int i = 0; i < cmapMapElementList.getLength(); i++) {
            Element map = (Element) cmapMapElementList.item(i);
            String code = map.getAttribute("code");
            if (code.startsWith("0x")) //Remove beginning "0x"
                code = code.substring(2);
            if (code.endsWith("L")) //Remove ending "L"
                code = code.substring(0, code.length() - 1);
            if (code.length() < 4) //Pad with 0s to make at least 4 long
                for (int j = code.length(); j != 4; j++)
                    code = "0" + code;

            cmap.put(map.getAttribute("name"), code);
        }

        //Create a glyph substitution list if the gsub element exists.
        List<Ligature> glyphSubList = null;
        Element gsubTableElement = (Element) rootElement.getElementsByTagName("GSUB").item(0);
        if (gsubTableElement != null) {
            Element lookupListElement = (Element) gsubTableElement.getElementsByTagName("LookupList").item(0);
            if (lookupListElement != null) {
                Element lookupElement = (Element) lookupListElement.getElementsByTagName("Lookup").item(0);
                if (lookupElement != null) {
                    Element ligatureSubstElement = (Element) lookupElement.getElementsByTagName("LigatureSubst").item(0);
                    if (ligatureSubstElement != null) {
                        glyphSubList = new ArrayList<>();
                        NodeList ligatureSetElementList = ligatureSubstElement.getElementsByTagName("LigatureSet");
                        for (int i = 0; i < ligatureSetElementList.getLength(); i++) {
                            if (isCancelled())
                                return false;

                            Element ligatureSetElement = (Element) ligatureSetElementList.item(i);
                            String mainGlyphName = ligatureSetElement.getAttribute("glyph");

                            NodeList ligatureList = ligatureSetElement.getElementsByTagName("Ligature");
                            for (int j = 0; j < ligatureList.getLength(); j++) {
                                Element ligatureElement = (Element) ligatureList.item(j);
                                List<String> components = Arrays.asList(ligatureElement.getAttribute("components").split(","));
                                String glyph = ligatureElement.getAttribute("glyph");

                                glyphSubList.add(new Ligature(glyph, mainGlyphName, components));
                            }
                        }
                    }
                }
            }
        }

        Element cbdtElement = (Element) rootElement.getElementsByTagName("CBDT").item(0);
        if (cbdtElement == null) {
            showErrorDialog("Invalid '.ttx' File (Missing CBDT Table)", "The '.ttx' file in the emojis directory appears to have been incorrectly modified. Packaging cannot continue.");
            return false;
        }

        Element strikeDataElement = (Element) cbdtElement.getElementsByTagName("strikedata").item(0);
        if (strikeDataElement == null) {
            showErrorDialog("Invalid '.ttx' File (Missing strikedata Table)", "The '.ttx' file in the emojis directory appears to have been incorrectly modified. Packaging cannot continue.");
            return false;
        }

        NodeList cbdtBitmapFormat17ElementList = cbdtElement.getElementsByTagName("cbdt_bitmap_format_17");
        for (int i = 0; i < cbdtBitmapFormat17ElementList.getLength(); i++) {
            Element imageElement = (Element) cbdtBitmapFormat17ElementList.item(i);
            String name = imageElement.getAttribute("name");
            Element rawImageData = (Element) imageElement.getElementsByTagName("rawimagedata").item(0);

            if (cmap.containsKey(name)) {
                File outputFile = new File(extractionDirectory, "uni" + cmap.get(name) + ".png");

                appendMessageToDialog("Extracting Emoji: " + outputFile.getName());
                updateProgress(i, cbdtBitmapFormat17ElementList.getLength());

                FileOutputStream outputStream = new FileOutputStream(outputFile);
                String imageDataHex = rawImageData.getTextContent();
                imageDataHex = imageDataHex.replaceAll("\\s|\\n|\\r|\\t", "");
                byte[] imageDataBytes = DatatypeConverter.parseHexBinary(imageDataHex);
                outputStream.write(imageDataBytes);
                outputStream.close();
            } else {
                if (glyphSubList != null) {
                    for (Ligature ligature : glyphSubList) {
                        if (ligature.getLigatureGlyphName().equals(name)) {
                            String fileName = "uni" + cmap.get(ligature.getSetGlyphName());
                            for (String component : ligature.getComponents()) {
                                fileName += "_" + "uni" + cmap.get(component);
                            }

                            File outputFile = new File(extractionDirectory, fileName + ".png");

                            appendMessageToDialog("Extracting Emoji: " + outputFile.getName());
                            updateProgress(i, cbdtBitmapFormat17ElementList.getLength());

                            FileOutputStream outputStream = new FileOutputStream(outputFile);
                            String imageDataHex = rawImageData.getTextContent();
                            imageDataHex = imageDataHex.replaceAll("\\s|\\n|\\r|\\t", "");
                            byte[] imageDataBytes = DatatypeConverter.parseHexBinary(imageDataHex);
                            outputStream.write(imageDataBytes);
                            outputStream.close();
                            break;
                        }
                    }
                }
            }
        }

        writeFontTypeFile(FontType.GOOGLE, null);

        //Cleanup memory...
        cmap.clear();
        cmap = null;
        if (glyphSubList != null)
            glyphSubList.clear();
        glyphSubList = null;
        System.gc();

        return true;
    }
}
