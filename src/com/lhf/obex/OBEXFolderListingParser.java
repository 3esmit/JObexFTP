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
 *
 * @author ricardo
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
