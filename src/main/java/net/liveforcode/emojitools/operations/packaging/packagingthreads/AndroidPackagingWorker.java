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

package net.liveforcode.emojitools.operations.packaging.packagingthreads;

import net.liveforcode.emojitools.EmojiTools;
import net.liveforcode.emojitools.gui.dialogs.OperationProgressDialog;
import net.liveforcode.emojitools.operations.Operation;
import net.liveforcode.emojitools.operations.OperationWorker;
import net.liveforcode.emojitools.operations.extraction.ExtractionOperation;
import net.liveforcode.emojitools.operations.packaging.LigatureSet;
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

public class AndroidPackagingWorker extends OperationWorker {

    private final File packagingDirectory;

    public AndroidPackagingWorker(Operation operation, OperationProgressDialog operationProgressDialog, File packagingDirectory) {
        super(operation, operationProgressDialog, true);
        this.packagingDirectory = packagingDirectory;
    }

    @Override
    protected Boolean doWork() throws Exception {
        File outputDirectory = new File(EmojiTools.getRootDirectory(), "Output");
        if (!outputDirectory.exists())
            outputDirectory.mkdir();
        else
            for (File file : outputDirectory.listFiles())
                file.delete();

        appendMessageToDialog("Building Emoji List...");

        ////////////////////////////// STEP 1: Identify all glyph names by png names //////////////////////////////

        if (isCancelled())
            return false;

        //Open up ttx for reading and writing
        Document infoDocument = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new File(packagingDirectory, ExtractionOperation.TTXType.ANDROID.getFileName()));

        //Check for ttFont element
        Element rootElement = infoDocument.getDocumentElement();
        if (!rootElement.getTagName().equals("ttFont")) {
            //packagingOperationManager.showMessageDialog("Invalid ttx file! Cannot package. Did you modify the file? (Code 1)");
            return false;
        }

        //Check for cmap element
        Element cmapElement = (Element) rootElement.getElementsByTagName("cmap").item(0);
        if (cmapElement == null) {
            //packagingOperationManager.showMessageDialog("Invalid ttx file! Cannot package. Did you modify the file? (Code 2)");
            return false;
        }

        //Check for cmap_format_12 element
        Element cmapFormat12Element = (Element) cmapElement.getElementsByTagName("cmap_format_12").item(0);
        if (cmapFormat12Element == null) {
            //packagingOperationManager.showMessageDialog("Invalid ttx file! Cannot package. Did you modify the file? (Code 3)");
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
        for (File file : packagingDirectory.listFiles()) {
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
                        String[] componentsUnicode = Arrays.copyOfRange(splitFileName, 1, splitFileName.length);

                        ArrayList<String> components = new ArrayList<>();
                        for (String component : componentsUnicode) {
                            components.add(glyphCodeNameMap.get(component));
                        }

                        LigatureSet ligatureSet = ligatureSetMap.get(mainGlyphName);
                        String glyphNameFromComponents = ligatureSet.getGlyphNameFromComponents(components);
                        glyphNameFileMap.put(glyphNameFromComponents, file);
                        appendMessageToDialog("File " + file.getName() + " has been assigned to " + glyphNameFromComponents);
                    }
                } else { //This png file name came directly from the cmap table.
                    if (glyphCodeNameMap.get(splitFileName[0]) != null) {
                        glyphNameFileMap.put(glyphCodeNameMap.get(splitFileName[0]), file);
                        appendMessageToDialog("File " + file.getName() + " has been assigned to " + glyphCodeNameMap.get(splitFileName[0]));
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
            //packagingOperationManager.showMessageDialog("Invalid ttx file! Cannot package. Did you modify the file? (Code 4)");
            return false;
        }

        Element strikeDataElement = (Element) cbdtElement.getElementsByTagName("strikedata").item(0);
        if (strikeDataElement == null) {
            //packagingOperationManager.showMessageDialog("Invalid ttx file! Cannot package. Did you modify the file? (Code 5)");
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
                //packagingOperationManager.showMessageDialog("Invalid ttx file! Cannot package. Did you modify the file? (Code 6)");
                return false;
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
                //packagingOperationManager.showMessageDialog("Invalid ttx file! Cannot package. Did you modify the file? (Code 7)");
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
        Result output = new StreamResult(new File(getJythonHandler().getTempDirectory(), ExtractionOperation.TTXType.ANDROID.getFileName()));
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
        argvList.add(getJythonHandler().getTempDirectory().getAbsolutePath() + "/" + ExtractionOperation.TTXType.ANDROID.getFileName()); //Input ttx path

        getJythonHandler().getPySystemState().argv = new PyList(PyType.fromClass(String.class), argvList);

        if (isCancelled())
            return false;

        //Execute
        getJythonHandler().getPythonInterpreter().execfile(getJythonHandler().getTempDirectory().getAbsolutePath() + "/PythonScripts/package.py");

        updateProgress(100, 100);
        return true;
    }
}
