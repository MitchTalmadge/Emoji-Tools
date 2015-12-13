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

package net.liveforcode.EmojiTools.Packaging.PackagingThreads;

import net.liveforcode.EmojiTools.EmojiTools;
import net.liveforcode.EmojiTools.GUI.EmojiToolsGUI;
import net.liveforcode.EmojiTools.GUI.PackagingDialog;
import net.liveforcode.EmojiTools.JythonHandler;
import net.liveforcode.EmojiTools.Packaging.LigatureSet;
import net.liveforcode.EmojiTools.Packaging.PackagingManager;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.util.HashMap;
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

            packagingDialog.setIndeterminate(false);

            packagingDialog.appendToStatus("Rewriting Emojis...");
            HashMap<String, String> glyphCodeNameMap = new HashMap<>();
            HashMap<File, String> glyphFileNameMap = new HashMap<>();

            Document infoFile = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new File(pngDirectory, "info.ttx"));
            Element rootElement = infoFile.getDocumentElement();
            if (!rootElement.getTagName().equals("ttFont")) {
                packagingManager.showMessageDialog("Invalid info.ttx file! Cannot package. Did you modify the file? (Code 1)");
                packagingDialog.dispose();
                return;
            }

            Element cmapElement = (Element) rootElement.getElementsByTagName("cmap").item(0);
            if (cmapElement == null) {
                packagingManager.showMessageDialog("Invalid info.ttx file! Cannot package. Did you modify the file? (Code 2)");
                packagingDialog.dispose();
                return;
            }

            Element cmapFormat12Element = (Element) cmapElement.getElementsByTagName("cmap_format_12").item(0);
            if (cmapFormat12Element == null) {
                packagingManager.showMessageDialog("Invalid info.ttx file! Cannot package. Did you modify the file? (Code 3)");
                packagingDialog.dispose();
                return;
            }

            NodeList mappingList = cmapFormat12Element.getElementsByTagName("map");
            for (int i = 0; i < mappingList.getLength(); i++) {
                Element mappingElement = (Element) mappingList.item(i);
                String code = mappingElement.getAttribute("code");
                String name = mappingElement.getAttribute("name");

                Pattern pattern = Pattern.compile("^0x([A-Fa-f0-9]+)L?$");
                Matcher matcher = pattern.matcher(code);
                if (matcher.find()) {
                    code = matcher.group(1);
                    if (code.length() < 4) {
                        code = new String(new char[4 - code.length()]).replace("\0", "0") + code;
                    }
                    glyphCodeNameMap.put(code, name);
                }
            }

            HashMap<String, LigatureSet> ligatureSetMap = new HashMap<>();

            Element gsubTable = (Element) rootElement.getElementsByTagName("GSUB").item(0);
            boolean gsubEnabled = gsubTable != null;
            if(gsubEnabled)
            {

            }

            for (File file : pngDirectory.listFiles()) {
                String fileName = file.getName();
                Pattern pattern = Pattern.compile("^(uni[A-Fa-f0-9]+(_uni[A-Fa-f0-9]+)*?)\\.png$");
                Matcher matcher = pattern.matcher(fileName);
                if (matcher.matches()) {
                    fileName = matcher.group(1);
                    String[] splitFileName = fileName.split("_");
                    //Remove "uni" prefixes
                    for (int i = 0; i < splitFileName.length; i++) {
                        if (splitFileName[i].startsWith("uni"))
                            splitFileName[i] = splitFileName[i].substring(3);

                        System.out.println(splitFileName[i]);
                    }
                    if (splitFileName.length > 1) {
                        if(gsubEnabled)
                        {

                        }
                    } else {
                        System.out.println("Split: " + splitFileName[0]);
                        if (glyphCodeNameMap.get(splitFileName[0]) != null) {
                            glyphFileNameMap.put(file, glyphCodeNameMap.get(splitFileName[0]));
                            System.out.println("File " + file.getName() + " has been assigned to " + glyphCodeNameMap.get(splitFileName[0]));
                        }
                    }
                }
            }

            //TODO: For each image, check if it has underscores; if it does, place it in the gsub table.

            packagingDialog.appendToStatus("Extracting Scripts...");



        } catch (Exception e) {
            EmojiTools.submitError(Thread.currentThread(), e);
        } finally {
            gui.getConsoleManager().removeConsoleListener(this);

            packagingDialog.dispose();
        }
    }
}
