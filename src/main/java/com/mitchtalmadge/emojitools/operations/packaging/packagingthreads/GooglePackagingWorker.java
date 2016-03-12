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

package com.mitchtalmadge.emojitools.operations.packaging.packagingthreads;

import com.mitchtalmadge.emojitools.EmojiTools;
import com.mitchtalmadge.emojitools.gui.dialogs.OperationProgressDialog;
import com.mitchtalmadge.emojitools.operations.Operation;
import com.mitchtalmadge.emojitools.operations.OperationWorker;
import com.mitchtalmadge.emojitools.operations.packaging.LigatureSet;
import org.python.core.PyList;
import org.python.core.PyType;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.imageio.ImageIO;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GooglePackagingWorker extends OperationWorker {

    private final File packagingDirectory;

    public GooglePackagingWorker(Operation operation, OperationProgressDialog operationProgressDialog, File packagingDirectory) {
        super(operation, operationProgressDialog, true);
        this.packagingDirectory = packagingDirectory;
    }

    @Override
    protected Boolean doWork() throws Exception {
        File outputDirectory = new File(EmojiTools.getRootDirectory(), "Output");
        if (!outputDirectory.exists() && !outputDirectory.mkdir()) {
            showErrorDialog("Unable to Create Directory", "Emoji Tools was unable to create a required directory. Does it have permission?");
            return false;
        }

        appendMessageToDialog("Building Emoji List...");

        ////////////////////////////// STEP 1: Identify all glyph names by png names //////////////////////////////

        if (isCancelled())
            return false;

        //Open up ttx for reading and writing
        Document infoDocument = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new File(packagingDirectory, "font.ttx"));

        //Check for ttFont element
        Element rootElement = infoDocument.getDocumentElement();
        if (!rootElement.getTagName().equals("ttFont")) {
            showErrorDialog("Invalid '.ttx' File (Error Code 1)", "The '.ttx' file in the emojis directory appears to have been incorrectly modified. Packaging cannot continue.");
            return false;
        }

        //Check for cmap element
        Element cmapElement = (Element) rootElement.getElementsByTagName("cmap").item(0);
        if (cmapElement == null) {
            showErrorDialog("Invalid '.ttx' File (Error Code 2)", "The '.ttx' file in the emojis directory appears to have been incorrectly modified. Packaging cannot continue.");
            return false;
        }

        //Check for cmap_format_12 element
        Element cmapFormat12Element = (Element) cmapElement.getElementsByTagName("cmap_format_12").item(0);
        if (cmapFormat12Element == null) {
            showErrorDialog("Invalid '.ttx' File (Error Code 3)", "The '.ttx' file in the emojis directory appears to have been incorrectly modified. Packaging cannot continue.");
            return false;
        }

        //Begin mapping unicode names to glyphs
        HashMap<String, String> glyphCodeNameMap = new HashMap<>();
        NodeList mappingList = cmapFormat12Element.getElementsByTagName("map");
        for (int i = 0; i < mappingList.getLength(); i++) {
            if (isCancelled())
                return false;

            Element mappingElement = (Element) mappingList.item(i);
            String code = mappingElement.getAttribute("code");
            String name = mappingElement.getAttribute("name");

            Pattern pattern = Pattern.compile("^0x([A-Fa-f0-9]+)L?$");
            Matcher matcher = pattern.matcher(code);
            if (matcher.find()) {
                code = matcher.group(1);
                if (code.length() < 4) {
                    //Unicode names are at least 4 long, padded with zeros in the beginning.
                    code = new String(new char[4 - code.length()]).replace("\0", "0") + code;
                }
                glyphCodeNameMap.put(code, name);
            }
        }

        //Create a glyph substitution mapping if the gsub element exists.
        HashMap<String, LigatureSet> ligatureSetMap = null;
        Element gsubTableElement = (Element) rootElement.getElementsByTagName("GSUB").item(0);
        if (gsubTableElement != null) {
            Element lookupListElement = (Element) gsubTableElement.getElementsByTagName("LookupList").item(0);
            if (lookupListElement != null) {
                Element lookupElement = (Element) lookupListElement.getElementsByTagName("Lookup").item(0);
                if (lookupElement != null) {
                    Element ligatureSubstElement = (Element) lookupElement.getElementsByTagName("LigatureSubst").item(0);
                    if (ligatureSubstElement != null) {
                        ligatureSetMap = new HashMap<>();
                        NodeList ligatureSetElementList = ligatureSubstElement.getElementsByTagName("LigatureSet");
                        for (int i = 0; i < ligatureSetElementList.getLength(); i++) {
                            if (isCancelled())
                                return false;

                            Element ligatureSetElement = (Element) ligatureSetElementList.item(i);
                            String mainGlyphName = ligatureSetElement.getAttribute("glyph");
                            LigatureSet ligatureSet = new LigatureSet(mainGlyphName);

                            NodeList ligatureList = ligatureSetElement.getElementsByTagName("Ligature");
                            for (int j = 0; j < ligatureList.getLength(); j++) {
                                Element ligatureElement = (Element) ligatureList.item(j);
                                List<String> components = Arrays.asList(ligatureElement.getAttribute("components").split(","));
                                String glyph = ligatureElement.getAttribute("glyph");

                                ligatureSet.assignComponentsToGlyph(components, glyph);
                            }
                            ligatureSetMap.put(mainGlyphName, ligatureSet);
                        }
                    }
                }
            }
        }

        //Begin mapping png file names to known glyph names
        HashMap<String, File> glyphNameFileMap = new HashMap<>();
        File[] files = packagingDirectory.listFiles();
        if (files == null) {
            showErrorDialog("Packaging Failed (Error Code 2)", "An internal error occurred. Please contact the developer for help.");
            return false;
        }
        for (File file : files) {
            if (isCancelled())
                return false;

            String fileName = file.getName();
            Pattern pattern = Pattern.compile("^((_?uni[A-Fa-f0-9]+)+)\\.png$");
            Matcher matcher = pattern.matcher(fileName);
            if (matcher.matches()) {
                fileName = matcher.group(1);
                String[] splitFileName = fileName.split("_");
                //Remove "uni" prefixes
                for (int i = 0; i < splitFileName.length; i++) {
                    if (splitFileName[i].startsWith("uni"))
                        splitFileName[i] = splitFileName[i].substring(3);
                }
                if (splitFileName.length > 1) { //This png file name was the result of a glyph substitution (flags, etc).
                    if (ligatureSetMap != null) {
                        String mainGlyphName = glyphCodeNameMap.get(splitFileName[0]);
                        if (mainGlyphName != null) {
                            String[] componentsUnicode = Arrays.copyOfRange(splitFileName, 1, splitFileName.length);

                            ArrayList<String> components = new ArrayList<>();
                            for (String component : componentsUnicode) {
                                components.add(glyphCodeNameMap.get(component));
                            }

                            LigatureSet ligatureSet = ligatureSetMap.get(mainGlyphName);
                            String glyphNameFromComponents = ligatureSet.getGlyphNameFromComponents(components);
                            glyphNameFileMap.put(glyphNameFromComponents, file);
                            appendMessageToDialog("File " + file.getName() + " has been assigned to " + glyphNameFromComponents);
                        } else { //This must be a new emoji. Let's try to add it to font.ttx
                            //TODO: Add to glyph substitution
                        }
                    }
                } else { //This png file name came directly from the cmap table.
                    if (glyphCodeNameMap.get(splitFileName[0]) != null) {
                        glyphNameFileMap.put(glyphCodeNameMap.get(splitFileName[0]), file);
                        appendMessageToDialog("File " + file.getName() + " has been assigned to " + glyphCodeNameMap.get(splitFileName[0]));
                    } else { //Not in cmap table! Must be a new emoji. Let's try to add it to font.ttx
                        Element glyphOrderElement = (Element) rootElement.getElementsByTagName("GlyphOrder").item(0);
                        if (glyphOrderElement == null) {
                            showErrorDialog("Invalid '.ttx' File (Error Code 8)", "The '.ttx' file in the emojis directory appears to have been incorrectly modified. Packaging cannot continue.");
                            return false;
                        }

                        Element lastGlyphIDElement = (Element) glyphOrderElement.getElementsByTagName("GlyphID").item(glyphOrderElement.getElementsByTagName("GlyphID").getLength() - 1);
                        if (lastGlyphIDElement == null) {
                            showErrorDialog("Invalid '.ttx' File (Error Code 9)", "The '.ttx' file in the emojis directory appears to have been incorrectly modified. Packaging cannot continue.");
                            return false;
                        }

                        String lastID = lastGlyphIDElement.getAttribute("id");
                        int newID = Integer.parseInt(lastID) + 1;

                        Element newGlyphIDElement = (Element) lastGlyphIDElement.cloneNode(false);
                        newGlyphIDElement.setAttribute("id", newID + "");
                        newGlyphIDElement.setAttribute("name", "uni"+splitFileName[0]);
                        glyphOrderElement.appendChild(newGlyphIDElement);

                        Element hmtxElement = (Element) rootElement.getElementsByTagName("hmtx").item(0);
                        if (hmtxElement == null) {
                            showErrorDialog("Invalid '.ttx' File (Error Code 10)", "The '.ttx' file in the emojis directory appears to have been incorrectly modified. Packaging cannot continue.");
                            return false;
                        }

                        Element lastMtxElement = (Element) hmtxElement.getElementsByTagName("mtx").item(hmtxElement.getElementsByTagName("mtx").getLength() - 1);
                        if (lastMtxElement == null) {
                            showErrorDialog("Invalid '.ttx' File (Error Code 11)", "The '.ttx' file in the emojis directory appears to have been incorrectly modified. Packaging cannot continue.");
                            return false;
                        }

                        Element newMtxElement = (Element) lastMtxElement.cloneNode(false);
                        newMtxElement.setAttribute("name", "uni"+splitFileName[0]);
                        newMtxElement.setAttribute("width", "2550");
                        newMtxElement.setAttribute("lsb", "0");
                        hmtxElement.appendChild(newMtxElement);

                        Element lastMapElement = (Element) cmapFormat12Element.getElementsByTagName("map").item(cmapFormat12Element.getElementsByTagName("map").getLength() - 1);
                        if (lastMapElement == null) {
                            showErrorDialog("Invalid '.ttx' File (Error Code 12)", "The '.ttx' file in the emojis directory appears to have been incorrectly modified. Packaging cannot continue.");
                            return false;
                        }

                        Element newMapElement = (Element) lastMapElement.cloneNode(false);
                        newMapElement.setAttribute("code", "0x" + splitFileName[0].toLowerCase());
                        newMapElement.setAttribute("name", "uni"+splitFileName[0]);
                        cmapFormat12Element.appendChild(newMapElement);

                        Element cbdtElement = (Element) rootElement.getElementsByTagName("CBDT").item(0);
                        if (cbdtElement == null) {
                            showErrorDialog("Invalid '.ttx' File (Error Code 13)", "The '.ttx' file in the emojis directory appears to have been incorrectly modified. Packaging cannot continue.");
                            return false;
                        }

                        Element strikeDataElement = (Element) cbdtElement.getElementsByTagName("strikedata").item(0);
                        if (strikeDataElement == null) {
                            showErrorDialog("Invalid '.ttx' File (Error Code 14)", "The '.ttx' file in the emojis directory appears to have been incorrectly modified. Packaging cannot continue.");
                            return false;
                        }

                        Element lastStrikeElement = (Element) strikeDataElement.getElementsByTagName("cbdt_bitmap_format_17").item(strikeDataElement.getElementsByTagName("cbdt_bitmap_format_17").getLength() - 1);
                        if (lastStrikeElement == null) {
                            showErrorDialog("Invalid '.ttx' File (Error Code 15)", "The '.ttx' file in the emojis directory appears to have been incorrectly modified. Packaging cannot continue.");
                            return false;
                        }

                        Element newStrikeElement = (Element) lastStrikeElement.cloneNode(true);
                        newStrikeElement.setAttribute("name", "uni"+splitFileName[0]);

                        BufferedImage image = ImageIO.read(file);
                        int width = image.getWidth();
                        int height = image.getHeight();

                        Element smallGlyphMetrics = (Element) newStrikeElement.getElementsByTagName("SmallGlyphMetrics").item(0);
                        Element widthElement = (Element) smallGlyphMetrics.getElementsByTagName("width").item(0);
                        widthElement.setAttribute("value", width + "");
                        Element heightElement = (Element) smallGlyphMetrics.getElementsByTagName("height").item(0);
                        heightElement.setAttribute("value", height + "");
                        Element advanceElement = (Element) smallGlyphMetrics.getElementsByTagName("Advance").item(0);
                        advanceElement.setAttribute("value", width + "");

                        Element rawImageDataElement = (Element) newStrikeElement.getElementsByTagName("rawimagedata").item(0);
                        rawImageDataElement.setTextContent("");

                        strikeDataElement.appendChild(newStrikeElement);

                        Element cblcElement = (Element) rootElement.getElementsByTagName("CBLC").item(0);
                        if (cblcElement == null) {
                            showErrorDialog("Invalid '.ttx' File (Error Code 16)", "The '.ttx' file in the emojis directory appears to have been incorrectly modified. Packaging cannot continue.");
                            return false;
                        }

                        Element cblcStrikeElement = (Element) cblcElement.getElementsByTagName("strike").item(0);
                        if (cblcStrikeElement == null) {
                            showErrorDialog("Invalid '.ttx' File (Error Code 17)", "The '.ttx' file in the emojis directory appears to have been incorrectly modified. Packaging cannot continue.");
                            return false;
                        }

                        Element glyphLocListElement = (Element) cblcStrikeElement.getElementsByTagName("eblc_index_sub_table_1").item(0);
                        if (glyphLocListElement == null) {
                            showErrorDialog("Invalid '.ttx' File (Error Code 18)", "The '.ttx' file in the emojis directory appears to have been incorrectly modified. Packaging cannot continue.");
                            return false;
                        }

                        Element lastGlyphLocElement = (Element) glyphLocListElement.getElementsByTagName("glyphLoc").item(glyphLocListElement.getElementsByTagName("glyphLoc").getLength() - 1);
                        if (lastGlyphLocElement == null) {
                            showErrorDialog("Invalid '.ttx' File (Error Code 19)", "The '.ttx' file in the emojis directory appears to have been incorrectly modified. Packaging cannot continue.");
                            return false;
                        }

                        Element newGlyphLocElement = (Element) lastGlyphIDElement.cloneNode(false);
                        newGlyphLocElement.setAttribute("id", newID + "");
                        newGlyphLocElement.setAttribute("name", "uni"+splitFileName[0]);
                        glyphLocListElement.appendChild(newGlyphLocElement);

                        glyphNameFileMap.put("uni"+splitFileName[0], file);
                        appendMessageToDialog("File " + file.getName() + " has been assigned to " + "uni"+splitFileName[0]);
                    }
                }
            }
        }

        updateProgress(25, 100);

        ////////////////////////////// STEP 2: Re-write png files to ttx file //////////////////////////////

        if (isCancelled())
            return false;

        Element cbdtElement = (Element) rootElement.getElementsByTagName("CBDT").item(0);
        if (cbdtElement == null) {
            showErrorDialog("Invalid '.ttx' File (Error Code 4)", "The '.ttx' file in the emojis directory appears to have been incorrectly modified. Packaging cannot continue.");
            return false;
        }

        Element strikeDataElement = (Element) cbdtElement.getElementsByTagName("strikedata").item(0);
        if (strikeDataElement == null) {
            showErrorDialog("Invalid '.ttx' File (Error Code 5)", "The '.ttx' file in the emojis directory appears to have been incorrectly modified. Packaging cannot continue.");
            return false;
        }

        NodeList cbdtBitmapFormat17ElementList = cbdtElement.getElementsByTagName("cbdt_bitmap_format_17");
        for (int i = 0; i < cbdtBitmapFormat17ElementList.getLength(); i++) {
            if (isCancelled())
                return false;

            Element cbdtBitmapFormat17Element = (Element) cbdtBitmapFormat17ElementList.item(i);
            String glyphName = cbdtBitmapFormat17Element.getAttribute("name");

            //Get png file from glyph name
            File pngFile = glyphNameFileMap.get(glyphName);

            if (pngFile == null) {
                showErrorDialog("Invalid '.ttx' File (Error Code 6)", "The '.ttx' file in the emojis directory appears to have been incorrectly modified. Packaging cannot continue.");
                return false;
            }

            //Create a hex string out of the png file
            FileInputStream fileInputStream = new FileInputStream(pngFile);

            StringBuilder hexStringBuilder = new StringBuilder();

            int value;
            while ((value = fileInputStream.read()) != -1) {
                hexStringBuilder.append(String.format("%02X ", value));
            }

            fileInputStream.close();
            String hexString = hexStringBuilder.toString();

            //Rewrite content of rawimagedata element with the hex string generated from the png file
            Element rawImageDataElement = (Element) cbdtBitmapFormat17Element.getElementsByTagName("rawimagedata").item(0);
            if (rawImageDataElement == null) {
                showErrorDialog("Invalid '.ttx' File (Error Code 7)", "The '.ttx' file in the emojis directory appears to have been incorrectly modified. Packaging cannot continue.");
                return false;
            }

            rawImageDataElement.setTextContent(hexString);

            appendMessageToDialog("Packaged " + pngFile.getName());
            updateProgress(25 + (int) (((float) i / cbdtBitmapFormat17ElementList.getLength()) * 50), 100);
        }

        ////////////////////////////// STEP 3: Store modified ttx file in tmp dir //////////////////////////////

        if (isCancelled())
            return false;

        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        Result output = new StreamResult(new File(getJythonHandler().getTempDirectory(), "font.ttx"));
        Source input = new DOMSource(infoDocument);

        transformer.transform(input, output);

        ////////////////////////////// STEP 4: Convert ttx to NotoColorEmoji.ttf //////////////////////////////

        if (isCancelled())
            return false;

        appendMessageToDialog("Building font... (This can take a while - Please wait)");

        //---- ttx.py ----//

        //Set sys.argv
        ArrayList<String> argvList = new ArrayList<>();
        argvList.add("package.py");                                                     //Python Script Name
        argvList.add("-o");                                                             //Output flag
        argvList.add(outputDirectory.getAbsolutePath() + "/NotoColorEmoji.ttf");        //Output ttf path
        argvList.add(getJythonHandler().getTempDirectory().getAbsolutePath() + "/" + "font.ttx"); //Input ttx path

        getJythonHandler().getPySystemState().argv = new PyList(PyType.fromClass(String.class), argvList);

        if (isCancelled())
            return false;

        //Execute
        getJythonHandler().getPythonInterpreter().execfile(getJythonHandler().getTempDirectory().getAbsolutePath() + "/PythonScripts/package.py");

        updateProgress(100, 100);
        return true;
    }
}
