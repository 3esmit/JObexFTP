/**
 *     This file is part of JObexFTP.
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
package com.lhf.obex;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import com.lhf.obex.dao.OBEXFile;
import com.lhf.obex.dao.OBEXFolder;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Parse obex-folder-listing xml into human readable
 * @author Ricardo Guilherme Schmidt, Ondrej Javonski
 */
public class OBEXFolderListingParser extends DefaultHandler {

    private OBEXFolder folder;
    public OBEXFolderListingParser(byte[] data) throws ParserConfigurationException, SAXException, IOException {
        super();
        folder = new OBEXFolder();
        init(data);
    }
    public OBEXFolderListingParser(byte[] data, OBEXFolder folder) throws ParserConfigurationException, SAXException, IOException {
        super();
        this.folder = folder;
        init(data);
    }
    private void init(byte[] data) throws ParserConfigurationException, SAXException, IOException{
        SAXParserFactory spf = SAXParserFactory.newInstance();
        spf.setValidating(false);
        spf.setFeature("http://xml.org/sax/features/validation", false);
        SAXParser sax = spf.newSAXParser();
        XMLReader xml = sax.getXMLReader();
        xml.setFeature("http://xml.org/sax/features/validation", false);
        xml.setContentHandler(this);
        ByteArrayInputStream bis = new ByteArrayInputStream(data);
        InputSource inps = new InputSource(bis);
        xml.parse(inps);
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
//        System.out.println(qName + " " + attributes.getValue("name") + " " + attributes.getValue("size") + " " + attributes.getValue("modified"));
        if(qName.equalsIgnoreCase("folder")){
            getFolder().addFolder(attributes.getValue("name"));
        }else if(qName.equalsIgnoreCase("file")){
            OBEXFile file = getFolder().addFile(attributes.getValue("name"));
            file.setSize(Long.parseLong(attributes.getValue("size")));
            file.setGroupPerm(attributes.getValue("group-perm"));
            file.setUserPerm(attributes.getValue("user-perm"));
            file.setModified(attributes.getValue("modified"));
        }
    }

    /**
     * @return the root
     */
    public OBEXFolder getFolder() {
        return folder;
    }

   
}
