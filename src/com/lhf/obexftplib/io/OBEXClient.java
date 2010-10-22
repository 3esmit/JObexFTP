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
package com.lhf.obexftplib.io;

import com.lhf.obexftplib.event.DataEventListener;
import com.lhf.obexftplib.event.ConnectionModeListener;
import com.lhf.obexftplib.etc.*;
import com.lhf.obexftplib.fs.*;
import com.lhf.obexftplib.io.obexop.*;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class manages the OBEX Connection, while device is in data mode. It represents an OBEX client.
 * @author Ricardo Guilherme Schmidt <ricardo@lhf.ind.br>
 */
public class OBEXClient {

    public final static String version = "2.0 beta";
    private final static int TIMEOUT = 30000;
    private final static Logger logger = Utility.getLogger();
    private final ObexEventListener eventListener = new ObexEventListener();
    private final OBEXData incomingData = new OBEXData();
    private final Object holder = new Object();
    private OBEXFolder currentFolder = OBEXObject.ROOT_FOLDER;
    private OBEXDevice device = OBEXDevice.DEFAULT;
    private int maxPacketLenght = 600;
    private boolean hasIncomingPacket;
    private boolean connected;
    private ATConnection conn;
    private OutputStream os;

    /**
     * Creates a new instance of OBEXClient. ATConnection is an needed class to control de I/O.
     * @param conn
     */
    public OBEXClient(ATConnection conn) {
        this.conn = conn;
    }

    /**
     * Sends connect request to obex server
     * @return true if operation was successful.
     * @throws IOException
     */
    public boolean connect() throws IOException {
        this.device = conn.getDevice();
        logger.log(Level.FINEST, "Connecting");
        connected = false;
        if (conn.getConnMode() != ATConnection.MODE_DATA) {
            throw new IllegalArgumentException();
        }
        this.os = conn.getOutputStream();
        this.conn.addConnectionModeListener(eventListener);
        this.conn.addDataEventListener(eventListener);
        currentFolder = OBEXObject.ROOT_FOLDER;
        currentFolder.setName(device.getRootFolder());
        Request req = new ConnectRequest();
        Header target = new Header(Header.TARGET);
        target.setValue(device.getFsUuid());
        req.addHeader(target);
        sendPacketAndWait(req, 500);
        if (hasIncomingPacket) {
            ConnectResponse response = new ConnectResponse(incomingData.pullData());
            setMaxPacketLenght(response.getMaxPacketLength());
            connected = Utility.threatResponse(response);
            if (connected) {
                logger.log(Level.FINE, "Connection established");
            }
        }
        return connected;
    }

    /**
     * Sends disconnect request to obex server.
     * @return true if operation was successful.
     * @throws IOException
     */
    public boolean disconnect() throws IOException {
        logger.log(Level.FINEST, "Disconnecting");
        sendPacketAndWait(new DisconnectRequest(), 500);
        if (hasIncomingPacket) {
            DisconnectResponse response = new DisconnectResponse(incomingData.pullData());
            connected = !Utility.threatResponse(response);
            if (!connected) {
                logger.log(Level.FINE, "Disconnected");
                this.conn.removeDataEventListener(eventListener);
                this.conn.removeConnectionModeListener(eventListener);
            }
        }
        return !connected;
    }

    /**
     * Abort any running operation.
     * @return true if operation was successful.
     */
    public boolean abort() throws IOException {
        logger.log(Level.FINER, "Aborting");
        boolean r = false;
        AbortRequest req = new AbortRequest();
        sendPacketAndWait(req, 1000);
        if (hasIncomingPacket) {
            AbortResponse response = new AbortResponse(incomingData.pullData());
            r = Utility.threatResponse(response);
        }
        return r;
    }

    /**
     * Erases all deletable files in server disk. WARNING: this operations is irreversible, and does not ask confirmation.
     * @return true if operation was successful.
     */
    public boolean eraseDisk() throws IOException {
        logger.log(Level.FINEST, "Erasing disk.");
        PutRequest req = new PutRequest();
        Header app = new Header(Header.APP_PARAMETERS);
        app.setValue(new byte[]{(byte) 0x31, (byte) 0x00});
        return Utility.threatResponse(put(req));
    }

    /**
     * Removes a file with the name as setted in OBEXFile, in the current directory.
     * This method is equivalent to removeFile(OBEXFile.getName());
     * @param file The object containing the name.
     * @return true if successful operation.
     */
    public boolean removeObject(OBEXObject file) throws IOException {
        return removeObject(file.getBinaryName());
    }

    /**
     * Removes a file with the name, in the current directory
     * @param filename the name of the file to delete
     * @return true if successful operation.
     */
    public boolean removeObject(byte[] filename) throws IOException {
        logger.log(Level.FINEST, "Removing object {0}", filename);

        PutRequest req = new PutRequest();
        req.setFinal();

        Header name = new Header(Header.NAME);
        name.setValue(filename);
        req.addHeader(name);

        Header lenght = new Header(Header.LENGTH);
        lenght.setValue(new byte[]{0,0,0,0});
        req.addHeader(lenght);
        Response res = put(req);
        System.out.println(Utility.dumpBytes(res.toBytes()));
        return Utility.threatResponse(res);
    }

    /**
     * Method used to change dirs and make dirs.
     * @param path the desired path
     * @param create if is to create unexistents folders in path
     * @return true if operation was successful. When choosen not to create folders, will return false if some of the folders in path does not exists.
     * @throws IOException if an IO error occurs.
     */
    public boolean changeDirectory(String path, boolean create) throws IOException {
        boolean success = true;
        path = Utility.preparePath(path); //prepare path to help users who havent read the docs.
        if (path.startsWith("a:")) { //if changeDir path is absolute
            path = Utility.getRelativePath(path, currentFolder.getPath()); // now is absolute (:
        }
        if (currentFolder.getPath().equalsIgnoreCase(path)) {
            return true;
        }
        String pathList[] = path.split("/");
        for (int i = 0; i < pathList.length; i++) {
            success = success && setFolder(pathList[i], create);
        }
        return success;
    }

    /**
     * Downloads the requested filename from the current folder.
     * @param filename
     * @return the file contents.
     * @throws IOException
     */
    public OBEXFile readFile(String filename) throws IOException {
        GetRequest request = new GetRequest();
        request.setFinal();
        Header type = new Header(Header.NAME);
        type.setValue(Utility.nameToBytes(filename));
        request.addHeader(type);
        OBEXFile file = new OBEXFile(currentFolder, filename);
        file.addResponses(get(request));
        return file;
    }

    //
    /**
     * Gets the "x-obex/folder-listing" object of the current folder, that represents the folder listing in XML.
     * @return a byte array containing the xml
     * @throws IOException if an IO error occurs
     */
    public OBEXFolder listFolder() throws IOException {
        GetRequest request = new GetRequest();
        request.setFinal();
        Header type = new Header(Header.TYPE);
        type.setValue(device.getLsName());
        request.addHeader(type);
        currentFolder.addResponses(get(request));
        return currentFolder;
    }

    /**
     * Writes to the end of a file, if file not exists, it will automatically create it. JOBEXFile contains the name and the size
     * @param file
     * @return true if operation was successful.
     */
    public boolean writeFile(OBEXFile file) throws IOException {
        boolean r = true;
        PutRequest req = new PutRequest(file.getHeaderSet());
        PutResponse res = null;
        OBEXFolder folder = file.getParentFolder();
        if (folder != currentFolder) {
            changeDirectory(folder.getPath(), false);
        }
        Header body;

        InputStream fileInputStream = file.getInputStream();
        int ava = fileInputStream.available();
        do {
            int size = (maxPacketLenght - 40) - (req.getPacketLength());
            byte[] b;
            if (ava < size + 3) {
                b = new byte[ava];
                req.setFinal();
                body = new Header(Header.END_OF_BODY);
            } else {
                b = new byte[size - 3];
                body = new Header(Header.BODY);
            }
            fileInputStream.read(b);
            body.setValue(b);
            req.addHeader(body);
            res = put(req);
            if (!Utility.threatResponse(res)) {
                r = false;
                logger.log(Level.WARNING, "Error writing file {0} response: {1}", new String[]{file.getName(), Utility.dumpBytes(res.toBytes())});
            }
            req = new PutRequest();
        } while ((ava = fileInputStream.available()) > 0);

        return r;
    }

    /**
     * Sends a put request
     * @param req the request to be sent
     * @return the put response or null if timedout.
     * @throws IOException if an io error occurs.
     */
    private PutResponse put(PutRequest req) throws IOException {
        sendPacketAndWait(req, TIMEOUT);
        if (hasIncomingPacket) {
            return new PutResponse(incomingData.pullData());
        }
        return null;
    }

    /**
     * Creates the SetPathRequest and send it to output. Case folder is null, go to parent directory.
     * @param folder The child folder to move, or null for move to parent folder.
     * @param create if true and the folder does not exists, create a new one.
     * @return a boolean indicating true for successful operation
     * @throws IOException
     */
    private boolean setPath(String folder, boolean create) throws IOException {
        boolean success = false;
        SetPathRequest req = new SetPathRequest();
        if (folder != null) {
            logger.log(Level.FINEST, "Setting path to {0}", folder);
            Header name = new Header(Header.NAME);
            name.setValue(Utility.nameToBytes(folder));
            req.addHeader(name);
            req.setFlags(create ? 0x00 : (byte) 0x02);
        } else {
            logger.log(Level.FINEST, "Setting path to parent folder.");
            req.setFlags((byte) 0x03);
        }
        sendPacketAndWait(req, 500);
        if (hasIncomingPacket) {
            if (incomingData.pullData()[0] == (byte) 0xA0) {
                logger.log(Level.FINER, "Path setted.");
                success = true;
            }
        }
        return success;
    }

    /**
     * Sends the packet and buffers the output in an bytearrayoutputstream until the last packet is sent, which is identified by the byte sequence {0x49, 0x00, 0x03}, then translates and returns
     * @param request the prepared request
     * @return the translated response
     * @throws IOException if an IO error occurs
     */
    private GetResponse[] get(GetRequest request) throws IOException {
        logger.log(Level.FINER, "Get operation to perform.");
        ArrayList<GetResponse> arrayList = new ArrayList<GetResponse>();

        GetResponse response = new GetResponse(Response.BADREQUEST);
        do {
            sendPacketAndWait(request, TIMEOUT);
            if (hasIncomingPacket) {
                arrayList.add(response = Utility.bytesToGetResponse(incomingData.pullData()));
            }
            request = new GetRequest();
        } while ((response.getType() & 0x7F) == Response.CONTINUE);

        GetResponse[] responses = new GetResponse[arrayList.size()];
        responses = arrayList.toArray(responses);
        return responses;
    }

    /**
     * Sends a packet to outputstream.
     * @param pkt The packet to be sent.
     * @throws IOException if an IO error occurs
     */
    private synchronized void sendPacket(Packet pkt) throws IOException {
        logger.log(Level.FINEST, "Sending {0}", Utility.dumpBytes(pkt.toBytes()));
        os.write(pkt.toBytes());
    }

    /**
     * Sends a packet, and waits until a incoming packet comes.
     * @param pkt the packet to be sent
     * @param timeout the timeout case the server does not answer.
     * @throws IOException
     */
    private void sendPacketAndWait(Packet pkt, int timeout) throws IOException {
        synchronized (holder) {
            sendPacket(pkt);
            try {
                hasIncomingPacket = false;
                logger.log(Level.FINEST, "Waiting response for {0} ms.", timeout);
                holder.wait(timeout);
            } catch (InterruptedException iE) {
            }
        }
    }

    /**
     * Sets the path, auto choose forward if path is '..'. Path cannot contain slashes or backslashes.
     * @param folderName
     * @return true if operation was successful
     */
    private boolean setFolder(String folderName, boolean create) throws IOException {
        if (folderName.equals("..")) {
            folderName = null;
        }
        boolean success = setPath(folderName, create);
        if (success) {
            if (folderName == null) {
                currentFolder = currentFolder.getParentFolder();
            } else {
                OBEXFolder childFolder = currentFolder.getChildFolder(folderName);
                currentFolder = childFolder == null ? new OBEXFolder(currentFolder, folderName) : childFolder;
            }
        }
        logger.log(Level.FINE, "Now in folder {0}", currentFolder.getPath());

        return success;
    }

    private class ObexEventListener implements DataEventListener, ConnectionModeListener {

        /**
         * Method to recieve OBEXEvents from ATConnection.
         * @param event the event.
         */
        public void DataEvent(byte[] event) {
            synchronized (holder) {
                incomingData.pushData(event);
                hasIncomingPacket = incomingData.isReady();
                if (hasIncomingPacket) {
                    holder.notifyAll();
                }
            }
        }

        public void update(int mode) {
            if (mode != ATConnection.MODE_DATA) {
                try {
                    logger.log(Level.WARNING, "Datamode to close unexpectedly");
                    abort();
                    disconnect();
                } catch (IOException ex) {
                    Logger.getLogger(OBEXClient.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }

    /**
     * @return the maxPacketSize
     */
    public int getMaxPacketLenght() {
        return maxPacketLenght;
    }

    /**
     * @param maxPacketLenght the maxPacketLenght to set
     */
    public void setMaxPacketLenght(int maxPacketLenght) {
        this.maxPacketLenght = maxPacketLenght;
    }
}

/**
 * Class used for controlling the incoming data when it cames broken.
 * @author Ricardo Guilherme Schmidt
 */
class OBEXData {

    private byte[] data = null;

    OBEXData() {
    }

    public int getPacketLenght() {
        return Utility.bytesToInt(Utility.getBytes(data, 1, 2));
    }

    public int getDataLenght() {
        return data.length;
    }

    public void pushData(byte[] newData) {
        Utility.getLogger().log(Level.FINEST, "Pushed data {0}", Utility.dumpBytes(newData));
        if (data == null) {
            data = newData;
        } else {
            byte[] temp = new byte[data.length + newData.length];
            Utility.setBytes(temp, data, 0, data.length);
            Utility.setBytes(temp, newData, data.length, newData.length);
            data = temp;
        }
    }

    public boolean isReady() {
        return data != null && getDataLenght() == getPacketLenght();
    }

    public byte[] pullData() {
        byte[] temp = data;
        data = null;
        return temp;
    }
}
