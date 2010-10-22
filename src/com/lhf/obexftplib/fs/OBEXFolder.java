/**
 * Last updated in 21/Out/2010
 *
 *    This file is part of JObexFTP.
 *
 *    JObexFTP is free software: you can redistribute it and/or modify
 *    it under the terms of the GNU Lesser General Public License as published by
 *    the Free Software Foundation, either version 3 of the License, or
 *    (at your option) any later version.
 *
 *    JObexFTP is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU Lesser General Public License for more details.
 *
 *    You should have received a copy of the GNU Lesser General Public License
 *    along with JObexFTP.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package com.lhf.obexftplib.fs;

import com.lhf.obexftplib.etc.Utility;
import com.lhf.obexftplib.io.obexop.Header;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

/**
 *
 * @author Ricardo Guilherme Schmidt <ricardo@lhf.ind.br>
 */
public final class OBEXFolder extends OBEXObject {

    private String path = null;
    private Map<String, OBEXObject> subobjects = new TreeMap<String, OBEXObject>();
    private static final OBEXFolderListingParser PARSER = new OBEXFolderListingParser();

    public OBEXFolder(OBEXFolder parentFolder, String filename) {
        super(parentFolder);
        setName(filename);
    }

    public OBEXFolder(String filename) {
        this(ROOT_FOLDER, filename);
    }

    @Override
    public String getPath() {
        if (path != null) {
            return path + "/" + name;
        } else if (getParentFolder() == null) {
            return name;
        } else {
            return getParentFolder().getPath() + "/" + name;
        }
    }

    public void add(OBEXObject object) {
        subobjects.put(object.name, object);
    }

    public Map<String, OBEXObject> getSubobjects() {
        return subobjects;
    }

    public OBEXFolder getChildFolder(String name) {
        OBEXObject obj = subobjects.get(name);
        if (obj != null && obj instanceof OBEXFolder) {
            return (OBEXFolder) obj;
        }
        return null;
    }

    public OBEXFile getChildFile(String name) {
        OBEXObject obj = subobjects.get(name);
        if (obj != null && obj instanceof OBEXFolder) {
            return (OBEXFile) obj;
        }
        return null;
    }

    public OBEXFolder addFolder(String filename) {
        OBEXFolder folder = new OBEXFolder(this, filename);
        add(folder);
        return folder;
    }

    public OBEXFile addFile(String filename) {
        OBEXFile file = new OBEXFile(filename);
        add(file);
        return file;
    }

    public String getListing() {
        StringBuilder builder = new StringBuilder();
        int folders = 0, files = 0;
        long fileSpace = 0;
        builder.append("\n Directory of ").append(getPath()).append("/\n\n").length();

        if (this != ROOT_FOLDER) {
            Utility.listingFormat(builder, ".", getSizeString(), getTime());
            Utility.listingFormat(builder, "..", getParentFolder().getSizeString(), getParentFolder().getTime());
        }
        for (Iterator<OBEXObject> it = subobjects.values().iterator(); it.hasNext();) {
            OBEXObject ob = it.next();
            builder.append(ob.toString());
            if (ob instanceof OBEXFile) {
                files++;
                OBEXFile file = (OBEXFile) ob;
                fileSpace += Utility.bytesToInt(file.getSize());
            } else {
                folders++;

            }
        }
        builder.append("                    ").append(files).append(" file(s) ").append(Utility.humanReadableByteCount(fileSpace, true)).append("\n");
        builder.append("                    ").append(folders).append(" dir(s)\n\n");

        return builder.toString();
    }

    @Override
    protected void threatHeader(Header header) {
        switch (header.getId()) {
            case Header.NAME:
                setName(header.getValue());
            case Header.BODY:
                try {
                    setContents(header.getValue());
                } catch (IOException ex) {
                }
                break;
            case Header.END_OF_BODY:
                try {
                    setContents(header.getValue());
                } catch (IOException ex) {
                }
                setReady();
                break;
        }
    }

    @Override
    protected void onReady() {
        try {
            PARSER.parse(getContents(), this);
        } catch (IOException ex) {
            Logger.getLogger(OBEXFolder.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SAXException ex) {
            Logger.getLogger(OBEXFolder.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    protected void onReset() {
        subobjects.clear();
    }

    @Override
    public String getSizeString() {
        return "<DIR>";
    }
}

/**
 * Parse obex-folder-listing xml into object oriented
 * @author Ricardo Guilherme Schmidt, Ondrej Javonski
 */
class OBEXFolderListingParser extends DefaultHandler {

    private OBEXFolder folder;
    private XMLReader xml;
    private File file;

    OBEXFolderListingParser() {
        try {
            xml = SAXParserFactory.newInstance().newSAXParser().getXMLReader();
            init();
        } catch (SAXException ex) {
            Logger.getLogger(OBEXFolderListingParser.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(OBEXFolderListingParser.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ParserConfigurationException ex) {
            Logger.getLogger(OBEXFolderListingParser.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public OBEXFolder parse(final byte[] folderListing, final OBEXFolder folder) throws IOException, SAXException {
        this.folder = folder;
        ByteArrayInputStream bis = new ByteArrayInputStream(folderListing);
        InputSource inps = new InputSource(bis);
        inps.setSystemId(file.toURI().toURL().toExternalForm());
        xml.parse(inps);
        return folder;
    }

    private void init() throws ParserConfigurationException, SAXException, IOException {
        SAXParserFactory spf = SAXParserFactory.newInstance();
        spf.setValidating(false);
        spf.setFeature("http://xml.org/sax/features/validation", false);
        SAXParser sax = spf.newSAXParser();
        xml = sax.getXMLReader();
        xml.setFeature("http://xml.org/sax/features/validation", false);
        xml.setContentHandler(this);

        file = new File("obex-folder-listing.dtd");
        if (!file.exists()) {
            file.createNewFile();
        }
    }

    @Override
    public void startDocument() throws SAXException {
        super.startDocument();
        if (this.folder == null) {
            this.folder = OBEXFolder.ROOT_FOLDER;
        }
    }

    @Override
    public void startElement(String uri, String localName, String qName, org.xml.sax.Attributes attributes) throws SAXException {
        OBEXObject childObject = null;
        if (qName.equalsIgnoreCase("folder")) {
            childObject = new OBEXFolder(folder, attributes.getValue("name"));
            childObject.setTime(attributes.getValue("modified"));
            childObject.setUserPerm(attributes.getValue("user-perm"));
            childObject.setGroupPerm(attributes.getValue("group-perm"));
        } else if (qName.equalsIgnoreCase("file")) {
            OBEXFile childFile = new OBEXFile(folder, attributes.getValue("name"));
            childFile.setTime(attributes.getValue("modified"));
            childFile.setUserPerm(attributes.getValue("user-perm"));
            childFile.setGroupPerm(attributes.getValue("group-perm"));
            childFile.setSize(Integer.parseInt(attributes.getValue("size")));
            childObject = childFile;
        }
        if (childObject != null) {
            folder.add(childObject);
        }
    }
}
