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

    public final static String version = "2.4 beta";
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
    private final ATConnection conn;
    private OutputStream os;

    /**
     * Creates a new instance of OBEXClient. ATConnection is an needed class to control de I/O.
     * @param conn
     */
    public OBEXClient(final ATConnection conn) {
        this.conn = conn;
    }

    /**
     * Sends connect request to obex server
     * @return true if operation was successful.
     * @throws IOException
     */
    public boolean connect() throws IOException {
        if (connected) {
            return connected;
        }
        this.device = conn.getDevice();
        logger.log(Level.FINEST, "Connecting");
        connected = false;
        if (conn.getConnMode() != ATConnection.MODE_DATA) {
            conn.setConnMode(ATConnection.MODE_DATA);
        }
        this.os = conn.getOutputStream();
        this.conn.addConnectionModeListener(eventListener);
        this.conn.addDataEventListener(eventListener);
        currentFolder = OBEXObject.ROOT_FOLDER;
        synchronized (this) {
            getCurrentFolder().setName(device.getRootFolder());
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
    }

    /**
     * Sends disconnect request to obex server.
     * @return true if operation was successful.
     * @throws IOException
     */
    public boolean disconnect() throws IOException {

        if (!connected) {
            return !connected;
        }
        synchronized (this) {
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
     * Erases all deletable files in server disk. WARNING: this operation is irreversible, and does not ask confirmation.
     * @return true if operation was successful.
     */
    public boolean eraseDisk() throws IOException {
        synchronized (this) {
            logger.log(Level.FINEST, "Erasing disk.");
            PutRequest req = new PutRequest();
            req.setFinal();
            Header app = new Header(Header.APP_PARAMETERS);
            app.setValue(new byte[]{(byte) 0x31, (byte) 0x00});
            req.addHeader(app);
            return Utility.threatResponse(put(req));
        }
    }

    /**
     * Reads the total disk space from obex server
     * @return the free space in bytes
     * @throws IOException
     */
    public long getDiskSpace() throws IOException {
        synchronized (this) {
            PutRequest req = new PutRequest();
            req.setFinal();
            Header app = new Header(Header.APP_PARAMETERS);
            app.setValue(new byte[]{0x32, 0x01, 0x01});
            req.addHeader(app);
            PutResponse res = put(req);
            return Utility.bytesToLong(res.getHeaderValue(Header.APP_PARAMETERS));
        }
    }

    /**
     * Reads the free space from obex server
     * @return the free space in bytes
     * @throws IOException
     */
    public long getFreeSpace() throws IOException {
        synchronized (this) {
            PutRequest req = new PutRequest();
            req.setFinal();
            Header app = new Header(Header.APP_PARAMETERS);
            app.setValue(new byte[]{0x32, 0x01, 0x02});
            req.addHeader(app);
            PutResponse res = put(req);
            return Utility.bytesToLong(res.getHeaderValue(Header.APP_PARAMETERS));
        }
    }

    /**
     * Removes a file with the name as setted in OBEXFile, in the current directory.
     * This method is equivalent to removeFile(OBEXFile.getBinaryName());
     * @param file The object containing the name.
     * @return true if successful operation.
     */
    public boolean removeObject(final OBEXObject file) throws IOException {
        return removeObject(file.getBinaryName());
    }

    /**
     * Removes a file with the name, in the current directory
     * @param filename the name of the file to delete
     * @return true if successful operation.
     */
    public boolean removeObject(final byte[] filename) throws IOException {
        synchronized (this) {
            logger.log(Level.FINEST, "Removing object {0}", new String(filename));

            PutRequest req = new PutRequest();
            req.setFinal();

            Header name = new Header(Header.NAME);
            name.setValue(filename);
            req.addHeader(name);

            Response res = put(req);
            return Utility.threatResponse(res);
        }
    }

    public boolean setObjectPerm(OBEXObject object, String userPerm, String groupPerm) throws IOException {
        synchronized (this) {

            PutRequest req = new PutRequest();
            req.setFinal();

            Header name = new Header(Header.NAME);
            name.setValue(object.getBinaryName());
            req.addHeader(name);

            Header app = new Header(Header.APP_PARAMETERS);
            app.setValue(Utility.buildPerm(userPerm, groupPerm));
            req.addHeader(app);

            Response res = put(req);
            return Utility.threatResponse(res);
        }
    }

    public boolean moveObject(OBEXObject object, String newPath) throws IOException {
        return moveObject(object.getPath(), newPath);
    }

    public boolean moveObject(String oldPath, String newPath) throws IOException {
        synchronized (this) {
            PutRequest req = new PutRequest();
            req.setFinal();
            Header app = new Header(Header.APP_PARAMETERS);
            app.setValue(Utility.prepareMoveByteArray(oldPath, newPath));
            req.addHeader(app);
            Response res = put(req);
            return Utility.threatResponse(res);
        }

    }

    /**
     * If OBEXObject is a folder, goes in it, if OBEXObject is a file, change dir to parent folder.
     * @param object the directory to move in, or the file to get in the parent folder dir.
     * @param create if is to create the dirs in path when they are not existent
     * @return true if change dir was sucess full
     * @throws IOException
     * @see OBEXClient#changeDirectory(java.lang.String, boolean) 
     */
    public boolean changeDirectory(final OBEXObject object, final boolean create) throws IOException {
        String path;
        if (object instanceof OBEXFolder) {
            if (object == getCurrentFolder()) {
                return true;
            }
            path = object.getPath();
        } else {
            if (object.getParentFolder() == getCurrentFolder()) {
                return true;
            }
            path = object.getParentFolder().getPath();
        }
        return changeDirectory(path, create);

    }

    /**
     * Method used to change dirs and make dirs.
     * @param path the desired path
     * @param create if is to create unexistents folders in path
     * @return true if operation was successful. When choosen not to create folders, will return false if some of the folders in path does not exists.
     * @throws IOException if an IO error occurs.
     * @see OBEXClient#changeDirectory(com.lhf.obexftplib.fs.OBEXObject)
     */
    public boolean changeDirectory(String path, final boolean create) throws IOException {
        boolean success = true;
        path = Utility.preparePath(path); //prepare path to help users who havent read the docs.
        if (path.startsWith("/")) { //if changeDir path is absolute
            path = Utility.getRelativePath(path, getCurrentFolder().getPath()); // now is relative (:
        }
        String currentpath = Utility.preparePath(getCurrentFolder().getPath());
        if (currentpath.equalsIgnoreCase(path)) {
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
     * Equivalent to readFile(new OBEXFile(getCurrentFolder(), filename));
     * @param filename
     * @return the file contents.
     * @throws IOException
     */
    public OBEXFile readFile(final String filename) throws IOException {
        return readFile(new OBEXFile(getCurrentFolder(), filename));
    }

    /**
     * Moves to file folder, and reads the file.
     * @param file The file to be filledup with data
     * @return the filled file
     * @throws IOException if an IO error occurs
     */
    public OBEXFile readFile(final OBEXFile file) throws IOException {
        synchronized (this) {
            GetRequest request = new GetRequest();
            request.setFinal();
            Header name = new Header(Header.NAME);
            name.setValue(file.getBinaryName());
            request.addHeader(name);
            file.addResponses(getAll(request));
            return file;
        }
    }

    /**
     * Gets the "x-obex/folder-listing" object of the current folder, that represents the folder listing in XML.
     * @return a byte array containing the xml
     * @throws IOException if an IO error occurs
     */
    public OBEXFolder loadFolderListing() throws IOException {
        synchronized (this) {
            GetRequest request = new GetRequest();
            request.setFinal();
            Header type = new Header(Header.TYPE);
            type.setValue(device.getLsName());
            request.addHeader(type);
            getCurrentFolder().addResponses(getAll(request));
            return getCurrentFolder();
        }
    }

    /**
     * Writes to the end of a file, if file not exists, it will automatically create it. 
     * JOBEXFile contains the name and the size, and the folder path.
     * If folder path specified does not exists, it will be automatically created.
     * @param file
     * @return true if operation was successful.
     */
    public boolean writeFile(final OBEXFile file) throws IOException {
        synchronized (this) {
            PutRequest req = new PutRequest(file.getHeaderSet());
            PutResponse res = new PutResponse(Response.BADREQUEST);
            Header body;
            InputStream is = file.getInputStream();
            int toRead;
            do {
                int size = (maxPacketLenght - 40) - req.getPacketLength();
                int ava = is.available();
                if (ava < size + 3) {
                    toRead = ava;
                    body = new Header(Header.END_OF_BODY);
                    req.setFinal();
                } else {
                    toRead = size - 3;
                    body = new Header(Header.BODY);
                }
                byte[] b = new byte[toRead];
                is.read(b);
                body.setValue(b);
                req.addHeader(body);
                res = put(req);
                req = new PutRequest();

            } while ((res.getType() & 0x7F) == Response.CONTINUE);
            is.close();
            file.setInputStream(null);
            return (res.getType() & 0x7F) == Response.SUCCESS;
        }
    }

    /**
     * Sends a put request
     * @param req the request to be sent
     * @return the put response or null if timedout.
     * @throws IOException if an io error occurs.
     */
    private PutResponse put(final PutRequest req) throws IOException {
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
    private boolean setPath(final String folder, final boolean create) throws IOException {
        synchronized (this) {
            boolean success = false;
            SetPathRequest req = new SetPathRequest();
            if (folder != null) {
                if (folder.isEmpty()) {
                    return true;
                }
                logger.log(Level.FINER, "Setting path to {0}", folder);
                Header name = new Header(Header.NAME);
                name.setValue(Utility.nameToBytes(folder));
                req.addHeader(name);
                req.setFlags(create ? 0x00 : (byte) 0x02);
            } else {
                logger.log(Level.FINER, "Setting path to parent folder.");
                req.setFlags((byte) 0x03);
            }
            sendPacketAndWait(req, 500);
            if (hasIncomingPacket) {
                if (incomingData.pullData()[0] == (byte) 0xA0) {
                    logger.log(Level.FINEST, "Path setted.");
                    success = true;
                }
            }
            return success;
        }
    }

    private GetResponse get(final GetRequest request) throws IOException {
        sendPacketAndWait(request, TIMEOUT);
        if (hasIncomingPacket) {
            return new GetResponse(incomingData.pullData());
        }
        return null;
    }

    /**
     * Sends the packet and buffers the output in an bytearrayoutputstream until the last packet is sent, which is identified by the byte sequence {0x49, 0x00, 0x03}, then translates and returns
     * @param request the prepared request
     * @return the translated response
     * @throws IOException if an IO error occurs
     */
    private GetResponse[] getAll(GetRequest request) throws IOException {
        logger.log(Level.FINER, "Get operation to perform.");
        ArrayList<GetResponse> arrayList = new ArrayList<GetResponse>();

        GetResponse response = new GetResponse(Response.BADREQUEST);
        do {
            arrayList.add(response = get(request));
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
    private synchronized void sendPacket(final Packet pkt) throws IOException {
        logger.log(Level.FINEST, "Sending {0}", Utility.dumpBytes(pkt.toBytes()));
        os.write(pkt.toBytes());
    }

    /**
     * Sends a packet, and waits until a incoming packet comes.
     * @param pkt the packet to be sent
     * @param timeout the timeout case the server does not answer.
     * @throws IOException
     */
    private void sendPacketAndWait(final Packet pkt, final int timeout) throws IOException {
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
    private boolean setFolder(String folderName, final boolean create) throws IOException {
        if ("..".equals(folderName)) {
            folderName = null;
        }
        boolean success = setPath(folderName, create);
        if (success) {
            if (folderName == null) {
                currentFolder = getCurrentFolder().getParentFolder();
            } else if (!"".equals(folderName)) {
                OBEXFolder childFolder = getCurrentFolder().getChildFolder(folderName);
                currentFolder = childFolder == null ? new OBEXFolder(getCurrentFolder(), folderName) : childFolder;
            }

        }

        logger.log(Level.FINE, "Now in folder {0}", getCurrentFolder().getPath());

        return success;
    }

    /**
     * @return the currentFolder
     */
    public OBEXFolder getCurrentFolder() {
        if (currentFolder == null) {
            currentFolder = OBEXObject.ROOT_FOLDER;
        }
        return currentFolder;
    }

    private class ObexEventListener implements DataEventListener, ConnectionModeListener {

        /**
         * Method to recieve OBEXEvents from ATConnection.
         * @param event the event.
         */
        public void DataEvent(final byte[] event) {
            synchronized (holder) {
                incomingData.pushData(event);
                hasIncomingPacket = incomingData.isReady();
                if (hasIncomingPacket) {
                    holder.notifyAll();
                }
            }
        }

        public void update(final int mode, final boolean changed) {
            if (mode != ATConnection.MODE_DATA && !changed) {
                try {
                    logger.log(Level.WARNING, "Datamode to close unexpectedly");
//                    abort();
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
    public void setMaxPacketLenght(final int maxPacketLenght) {
        this.maxPacketLenght = maxPacketLenght;
    }

    public boolean isConnected() {
        return connected;
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

    public void pushData(final byte[] newData) {
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
