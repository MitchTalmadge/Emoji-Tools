/*
 * Emoji Tools helps users and developers of Android, iOS, and OS X extract, modify, and repackage Emoji fonts.
 * Copyright (C) 2015 Mitch Talmadge
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

package net.liveforcode.emojitools.packaging.PackagingThreads;

import net.liveforcode.emojitools.EmojiTools;
import net.liveforcode.emojitools.extraction.ExtractionManager;
import net.liveforcode.emojitools.oldgui.EmojiToolsGUI;
import net.liveforcode.emojitools.oldgui.PackagingDialog;
import net.liveforcode.emojitools.JythonHandler;
import net.liveforcode.emojitools.packaging.LigatureSet;
import net.liveforcode.emojitools.packaging.PackagingManager;
import org.python.core.PyList;
import org.python.core.PyType;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AndroidPackagingThread extends PackagingThread {

    public AndroidPackagingThread(EmojiToolsGUI gui, File pngDirectory, PackagingManager packagingManager, PackagingDialog packagingDialog, JythonHandler jythonHandler) {
        super("AndroidPackagingThread", gui, pngDirectory, packagingManager, packagingDialog, jythonHandler);
    }

    @Override
    public void run() {
        try {
            File outputDirectory = new File(EmojiTools.getRootDirectory(), "Output");
            if (!outputDirectory.exists())
                outputDirectory.mkdir();
            else
                for (File file : outputDirectory.listFiles())
                    file.delete();

            packagingDialog.setIndeterminate(false);

            packagingDialog.appendToStatus("Rewriting Emojis...");

            ////////////////////////////// STEP 1: Identify all glyph names by png names //////////////////////////////

            if (!running)
                return;

            //Open up ttx for reading and writing
            Document infoDocument = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new File(pngDirectory, ExtractionManager.TTXType.ANDROID.getFileName()));

            //Check for ttFont element
            Element rootElement = infoDocument.getDocumentElement();
            if (!rootElement.getTagName().equals("ttFont")) {
                packagingManager.showMessageDialog("Invalid ttx file! Cannot package. Did you modify the file? (Code 1)");
                packagingDialog.dispose();
                return;
            }

            //Check for cmap element
            Element cmapElement = (Element) rootElement.getElementsByTagName("cmap").item(0);
            if (cmapElement == null) {
                packagingManager.showMessageDialog("Invalid ttx file! Cannot package. Did you modify the file? (Code 2)");
                packagingDialog.dispose();
                return;
            }

            //Check for cmap_format_12 element
            Element cmapFormat12Element = (Element) cmapElement.getElementsByTagName("cmap_format_12").item(0);
            if (cmapFormat12Element == null) {
                packagingManager.showMessageDialog("Invalid ttx file! Cannot package. Did you modify the file? (Code 3)");
                packagingDialog.dispose();
                return;
            }

            //Begin mapping unicode names to glyphs
            HashMap<String, String> glyphCodeNameMap = new HashMap<>();
            NodeList mappingList = cmapFormat12Element.getElementsByTagName("map");
            for (int i = 0; i < mappingList.getLength(); i++) {
                if (!running)
                    return;

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
                                if (!running)
                                    return;

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
            for (File file : pngDirectory.listFiles()) {
                if (!running)
                    return;

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
                            String[] componentsUnicode = Arrays.copyOfRange(splitFileName, 1, splitFileName.length);

                            ArrayList<String> components = new ArrayList<>();
                            for (String component : componentsUnicode) {
                                components.add(glyphCodeNameMap.get(component));
                            }

                            LigatureSet ligatureSet = ligatureSetMap.get(mainGlyphName);
                            String glyphNameFromComponents = ligatureSet.getGlyphNameFromComponents(components);
                            glyphNameFileMap.put(glyphNameFromComponents, file);
                            System.out.println("File " + file.getName() + " has been assigned to " + glyphNameFromComponents);
                        }
                    } else { //This png file name came directly from the cmap table.
                        if (glyphCodeNameMap.get(splitFileName[0]) != null) {
                            glyphNameFileMap.put(glyphCodeNameMap.get(splitFileName[0]), file);
                            System.out.println("File " + file.getName() + " has been assigned to " + glyphCodeNameMap.get(splitFileName[0]));
                        }
                    }
                }
            }

            packagingDialog.setProgress(25);

            ////////////////////////////// STEP 2: Re-write png files to ttx file //////////////////////////////

            if (!running)
                return;

            Element cbdtElement = (Element) rootElement.getElementsByTagName("CBDT").item(0);
            if (cbdtElement == null) {
                packagingManager.showMessageDialog("Invalid ttx file! Cannot package. Did you modify the file? (Code 4)");
                packagingDialog.dispose();
                return;
            }

            Element strikeDataElement = (Element) cbdtElement.getElementsByTagName("strikedata").item(0);
            if (strikeDataElement == null) {
                packagingManager.showMessageDialog("Invalid ttx file! Cannot package. Did you modify the file? (Code 5)");
                packagingDialog.dispose();
                return;
            }

            NodeList cbdtBitmapFormat17ElementList = cbdtElement.getElementsByTagName("cbdt_bitmap_format_17");
            for (int i = 0; i < cbdtBitmapFormat17ElementList.getLength(); i++) {
                if (!running)
                    return;

                Element cbdtBitmapFormat17Element = (Element) cbdtBitmapFormat17ElementList.item(i);
                String glyphName = cbdtBitmapFormat17Element.getAttribute("name");

                //Get png file from glyph name
                File pngFile = glyphNameFileMap.get(glyphName);

                if (pngFile == null) {
                    packagingManager.showMessageDialog("Invalid ttx file! Cannot package. Did you modify the file? (Code 6)");
                    packagingDialog.dispose();
                    return;
                }

                //Create a hex string out of the png file
                FileInputStream fileInputStream = new FileInputStream(pngFile);
                int value = 0;
                StringBuilder hexStringBuilder = new StringBuilder();

                while ((value = fileInputStream.read()) != -1) {
                    hexStringBuilder.append(String.format("%02X ", value));
                }

                fileInputStream.close();
                String hexString = hexStringBuilder.toString();

                //Rewrite content of rawimagedata element with the hex string generated from the png file
                Element rawImageDataElement = (Element) cbdtBitmapFormat17Element.getElementsByTagName("rawimagedata").item(0);
                if (rawImageDataElement == null) {
                    packagingManager.showMessageDialog("Invalid ttx file! Cannot package. Did you modify the file? (Code 7)");
                    packagingDialog.dispose();
                    return;
                }

                rawImageDataElement.setTextContent(hexString);

                System.out.println("Packaged " + pngFile.getName());
                packagingDialog.setProgress(25 + (int) (((float) i / cbdtBitmapFormat17ElementList.getLength()) * 50));
            }

            ////////////////////////////// STEP 3: Store modified ttx file in tmp dir //////////////////////////////

            if (!running)
                return;

            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            Result output = new StreamResult(new File(jythonHandler.getTempDirectory(), ExtractionManager.TTXType.ANDROID.getFileName()));
            Source input = new DOMSource(infoDocument);

            transformer.transform(input, output);

            ////////////////////////////// STEP 4: Convert ttx to NotoColorEmoji.ttf //////////////////////////////

            if (!running)
                return;

            System.out.println("Building font... (This can take a while - Please wait)");

            //---- ttx.py ----//

            //Set sys.argv
            ArrayList<String> argvList = new ArrayList<>();
            argvList.add("package.py");                                                     //Python Script Name
            argvList.add("-o");                                                             //Output flag
            argvList.add(outputDirectory.getAbsolutePath() + "/NotoColorEmoji.ttf");        //Output ttf path
            argvList.add(jythonHandler.getTempDirectory().getAbsolutePath() + "/"+ ExtractionManager.TTXType.ANDROID.getFileName()); //Input ttx path

            jythonHandler.getPySystemState().argv = new PyList(PyType.fromClass(String.class), argvList);

            if (!running)
                return;

            //Execute
            jythonHandler.getPythonInterpreter().execfile(jythonHandler.getTempDirectory().getAbsolutePath() + "/PythonScripts/package.py");

            packagingDialog.setProgress(100);

        } catch (Exception e) {
            EmojiTools.submitError(Thread.currentThread(), e);
        } finally {
            gui.getConsoleManager().removeConsoleListener(this);

            packagingDialog.dispose();
        }
    }
}
