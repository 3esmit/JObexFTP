package com.lhf.obex;

import gnu.io.CommPort;
import gnu.io.CommPortIdentifier;
import gnu.io.NoSuchPortException;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;
import gnu.io.UnsupportedCommOperationException;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.InvalidParameterException;
import java.util.Calendar;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import com.lhf.obex.dao.OBEXFile;
import java.net.Socket;

/**
 *
 * @author Ricardo Guilherme Schmidt, Florian Chiș, Radu Petrișor
 */
public class OBEXProtocol implements OBEXInterface {

    /**
     * @param aVerbose the verbose to set
     */
    public void setVerbose(boolean aVerbose) {
        verbose = aVerbose;
    }

    /**
     * @param aVeratcmd the veratcmd to set
     */
    public void setVeratcmd(boolean aVeratcmd) {
        veratcmd = aVeratcmd;
    }

    /**
     * @param aAutoon the autoon to set
     */
    public void setAutoon(int aAutoon) {
        autoon = aAutoon;
    }

    /**
     * @return the flowControl
     */
    public int getFlowControl() {
        return flowControl;
    }

    /**
     * @param aFlowControl the flowControl to set
     */
    public void setFlowControl(int aFlowControl) {
        if (aFlowControl >= 0 && aFlowControl <= 2) {
            flowControl = aFlowControl;
        } else {
            throw new InvalidParameterException("Parameter must be between 0 and 2");
        }
    }

    /**
     * @return the baudRate
     */
    public int getBaudRate() {
        return baudRate;
    }

    /**
     * @param aBaudRate the baudRate to set
     */
    public void setBaudRate(int aBaudRate) {
        baudRate = aBaudRate;
    }

    /**
     * @return the out
     */
    public OutputStream getOut() {
        if (out == null) {
            out = System.out;
        }
        return out;
    }

    /**
     * @param aOut the out to set
     */
    public void setOut(OutputStream aOut) {
        out = aOut;
    }
    private InputStream is;
    private OutputStream os;
    private SerialPort serialPort;
    private Socket socketConnection;
    private int maxpacket = 600;
    private boolean wasTurnedOff = false;
    private boolean obexConnected = false;
    private boolean verbose = false;
    private boolean veratcmd = false;
    private int autoon = 2;
    private int flowControl = FLOW_RTSCTS;
    private int baudRate = 115200;
    private OutputStream out;

    /**
     * @param serial to be connected
     * @throws OBEXException if could not open connection
     */
    public OBEXProtocol(String serial, boolean check) throws OBEXException {
        if (serial.indexOf("ttyS") < 0 && serial.indexOf("COM") < 0) {
            System.setProperty("gnu.io.rxtx.SerialPorts", serial);
        }
        verbose("# Connecting...");
        try {
            open(serial);
            start(check);
        } catch (OBEXException ex) {
            close();
            throw ex;
        } catch (IOException ex) {
            close();
            ex.printStackTrace();
            throw new OBEXException(ex.getMessage(), ex);
        }
    }

    /**
     * Opens a Socket Connection to an ethernet to RS232 converter
     * @param address the network address of the ethernet to RS232 converter
     * @param port the port that the RS232 converter uses to relay the data
     * @throws OBEXException if the connection cannot be opened
     */
     public OBEXProtocol(String address, int port,boolean check) throws OBEXException{
         try{
             System.out.println("# Opening socket connection to "+address+":"+port);
             socketConnection = new Socket(address,port);
             is = socketConnection.getInputStream();
             os = socketConnection.getOutputStream();
             start(check);
         }catch(IOException ex){
             System.out.println("# Error while opening the connection");
             close();
             ex.printStackTrace();
             throw new OBEXException(ex.getMessage(), ex);
         }
     }

    /**
     * Helper function to send out more information about the process.
     * @param text to be printed out
     */
    private void verbose(String text) {
        if (verbose) {
            try {
                text += "\r\n";
                getOut().write(text.getBytes());
                getOut().flush();
            } catch (IOException ex) {
                Logger.getLogger(OBEXProtocol.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    /**
     * Open object streams with the tty/serial device
     * @param portName the address of the comuncation port
     * @return true if sucessful (can be void?)
     * @throws OBEXException if not sucessful
     */
    public void open(String portName) throws OBEXException {
        boolean b = false;
        try {
            open(CommPortIdentifier.getPortIdentifier(portName));
        } catch (NoSuchPortException ex) {
            throw new OBEXException(portName + " is not available. " + ex.getMessage(), ex);
        }
    }

    /**
     * Open object streams with the tty/serial device
     * @param portIdentifier the port identifier
     * @return true if sucessful (can be void?)
     * @throws OBEXException if not sucessful
     */
    public void open(CommPortIdentifier portIdentifier) throws OBEXException {
        try {
            CommPort commPort = portIdentifier.open(this.getClass().getName(), 2000);
            if (commPort instanceof SerialPort) {
                serialPort = (SerialPort) commPort;
                serialPort.setSerialPortParams(getBaudRate(), SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
                switch (getFlowControl()) {
                    case FLOW_NONE:
                        serialPort.setFlowControlMode(SerialPort.FLOWCONTROL_NONE);
                        break;
                    case FLOW_XONXOFF:
                        serialPort.setFlowControlMode(SerialPort.FLOWCONTROL_XONXOFF_IN | SerialPort.FLOWCONTROL_XONXOFF_OUT);
                        break;
                    case FLOW_RTSCTS:
                        serialPort.setFlowControlMode(SerialPort.FLOWCONTROL_RTSCTS_OUT | SerialPort.FLOWCONTROL_RTSCTS_IN);
                        break;
                }
                is = serialPort.getInputStream();
                os = serialPort.getOutputStream();
            }
        } catch (IOException ex) {
            verbose("### IOError: Can't open stream. " + ex.getMessage());
            throw new OBEXException("Can't open stream " + ex.getMessage(), ex);
        } catch (UnsupportedCommOperationException ex) {
            verbose("### Error: Unsupported Communication Operation. " + ex.getMessage());
            throw new OBEXException("Unsupported Communication Operation" + ex.getMessage(), ex);
        } catch (PortInUseException ex) {
            verbose("### Error: " + portIdentifier.getName() + " in use. " + ex.getMessage());
            throw new OBEXException(portIdentifier.getName() + " in use " + ex.getMessage(), ex);
        }
    }

    /**
     * Helper function to sleep the current Thread
     * @param millis to sleep
     */
    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException ex) {
        }
    }

    /**
     * Sends (AT) commands to the stream.
     * @param s the string to be send
     * @return the response
     * @throws IOException
     */
    public String send(String s) throws IOException {
        return send(s.getBytes());
    }

    /**
     * Sends (AT) commands to the stream.
     * @param s the string to be send
     * @return the response
     * @throws IOException
     */
    private String send(String s, long millis) throws IOException {
        return send(s.getBytes(), millis);
    }

    /**
     * Sends (AT) commands to the stream.
     * @param s the string to be send
     * @return the response
     * @throws IOException
     */
    private String send(byte[] b) throws IOException {
        return send(b, 500);
    }

    /**
     * Sends (AT) commands to the stream.
     * @param s the string to be send
     * @return the response
     * @throws IOException
     */
    private String send(byte[] b, long millis) throws IOException {
        StringBuffer buff = new StringBuffer();
        try {
            os.write(b);
            os.flush();
            sleep(millis);
            char c;
            while (is.available() > 0) {
                c = (char) is.read();
                buff.append(c);
            }
            if (veratcmd) {
                getOut().write(buff.toString().getBytes());
            }
        } catch (IOException ex) {
            verbose("### IOError: Cannot send " + new String(b));
            throw ex;
        }
        return buff.toString();
    }

    /**
     * Function to turn on module, it blocks the autostart of the app (if is not started yet),
     * put module in airplane mode to transfer files without gsm signal
     */
    private void turnOn() throws IOException {
        try {
            if (send("ATI\r\n").indexOf("ERROR") > -1) {
                wasTurnedOff = true;
                verbose("# Powering the RGGGSM...");
                send("AT^SCFG=\"Userware/Autostart\",\"\",\"0\"\r\n");
                send("AT^SCFG=\"MEopMode/Airplane\",\"on\"\r\n", 1000);

            } else {
                wasTurnedOff = false;
            }
        } catch (IOException ex) {
            verbose("# Error on powering the module");
            throw ex;
        }
    }

    /**
     * Start function to openstream, turn on, switch to command mode and open obex stream
     * @param serial that will be connected to
     * @return true if connection was sucessful
     * @throws IOException something goes wrong
     */
    private void start(boolean check) throws IOException {
        try {            
            if (check) {
                if (autoon > 0) {
                    turnOn();
                }
                if (!isCommandMode()) {
                    verbose("# Entering command mode...");
                    try {
                        leaveOBEXMode();
                    } catch (OBEXException e) {
                        throw new IOException("Can't activate command mode probably because port is in use or an application is running inside the module");
                    }
                    sleep(500);
                    if (!isCommandMode()) {
                        throw new IOException("Can't comunicate with module. Restart and try again");
                    }
                }
            }
        } catch (IOException ex) {
            System.out.println("### IOError: " + ex.getMessage());
            throw ex;
        }
    }

    /**
     * Function to turn off the module using at AT^SMSO
     * @throws IOException if stream closed/error
     */
    private void turnOff() throws IOException {
        try {
            verbose("# Shutting RGGGSM...");
            send("AT^SMSO\r\n", 1600);
        } catch (IOException ex) {
            verbose("### IOError: Can't turn off RGGGSM " + ex.getMessage());
            ex.printStackTrace();
            throw ex;
        }
    }

    /**
     * Function to read all data from inputstream
     * @return the data read
     * @throws IOException if could not read.
     */
    private byte[] readAll() throws IOException {
        byte[] res = new byte[is.available()];
        try {
            is.read(res);
        } catch (IOException ex) {
            verbose("### Error: Can't read available data " + ex.getMessage());
            throw ex;
        }
        return res;
    }

    /**
     * function that waits for an answer
     * @param timeout timeout in miliseconds
     * @returns byte[] answer
     * @throws IOException
     */
    private byte[] readAnswer(long timeout) throws IOException{
        long ct = System.currentTimeMillis();
        do{
            try{Thread.sleep(20);}catch(Exception _){/*do nothing*/}
        }while(ct+timeout > System.currentTimeMillis() && is.available() == 0);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        long T0 = System.currentTimeMillis();
        while ((System.currentTimeMillis() - T0) < 100) { //fixed: avoid orphan packages
            while (is.available() > 0) {
                baos.write(is.read());
            }
        }
        return baos.toByteArray();
    }

    /**
     * Function that closes everyting and leaveOBEXMode module.
     * @throws OBEXException if something goes wrong
     */
    public void close() throws OBEXException {
        verbose("# Disconnecting...");
        sleep(1000);
        try {
            if (obexConnected) {
                leaveOBEXMode();
            }
            if (wasTurnedOff && autoon > 1) {
                turnOff();
            }
            if(is!=null) {
                is.close();
                is=null;
            }
            if(os!=null) {
                os.close();
                os=null;
            }
        } catch (IOException ex) {
            ex.printStackTrace();
            verbose("### IOError: Could not disconnect" + ex.getMessage());
            throw new OBEXException(ex.getMessage(), ex);
        }
        if (serialPort != null) {
            serialPort.close();
        }
        if (socketConnection != null){
            try{socketConnection.close();}catch(IOException ex){};
        }

    }

    /**
     * Function to get a folderlisting
     * @param path if not null goes to that folder, if null lists the actual folder
     * @return xml string containg the folder listing
     * @throws OBEXException if get operation fails
     */
    public String getFolderListing(String path) throws OBEXException {
        verbose("# Listing folder...");
        if (path != null) {
            goTo(path);
        }

        String s = get("x-obex/folder-listing").trim();
        //For some misterious reason, in windows, sometimes the header came broken, this is a bad workaround in case it happens
        if (s.startsWith("listing version=\"1.0\">")) {
            verbose("# Broken XML header. Fixing...");
            s = "<?xml version=\"1.0\"?>\n<!DOCTYPE folder-listing SYSTEM \"obex-folder-listing.dtd\"><folder-" + s;
        }
        return s;
    }

    /**
     * function to get files from module
     * @param filename file with remote path to be get
     * @return string with the file contents
     * @throws OBEXException if get operation fails
     */
    public String getFile(String filename) throws OBEXException {
        goTo(filename);
        StringTokenizer tok = new StringTokenizer(filename, "/");
        String fileNameWithoutPath = null;
        while (tok.hasMoreTokens()) {
            fileNameWithoutPath = tok.nextToken();
        }
        verbose("# Getting " + fileNameWithoutPath + "...");
        return get(fileNameWithoutPath);
    }

    /**
     * function to change the actual folder
     * @param path the destiny path
     * @throws OBEXException
     */
    public void goTo(String path) throws OBEXException {
        String filename = path.replace('\\', '/');
        StringTokenizer tok = new StringTokenizer(filename, "/");
        while (tok.hasMoreTokens()) {
            String tmp = tok.nextToken();

            if (tmp.indexOf("a:") < 0 && tmp.length() != 0) {
                verbose("# Setting directory to " + tmp);
                setPath(tmp, false, false);
                try {
                    while (is.available() > 0) {
                        out.write(("0x" + Integer.toHexString(is.read()).toUpperCase() + "\n").getBytes());
                    }
                } catch (IOException ex) {
                    Logger.getLogger(OBEXProtocol.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }

    public boolean deleteFile(OBEXFile object) throws OBEXException {
        verbose("# Deleting: " + object.getName());
        put(PUT_DELETE, object);
        boolean end = false, success = false;
        try {
            while (!end) {
                while (is.available() > 0) {
                    if (is.read() == 0xa0) {
                        success = true;
                    }
                    end = true;
                }
            }
        } catch (IOException ex) {
            throw new OBEXException("Error while deleting the file", ex);
        }
        return success;
    }

    /**
     * function to send files
     * @param object the object with filename and contents (or file stream)
     * @throws OBEXException
     */
    public boolean sendFile(OBEXFile object) throws OBEXException {
        try {
            byte[] ans;
            boolean ok = false;
            //goTo(filename);
            deleteFile(object);
            verbose("# Sending: " + object.getName());
            put(PUT_SEND, object);
            ans =readAnswer(1000);
            if (ans.length > 0 && ((ans[0]&0xff)==0x90 || (ans[0]&0xff)==0xA0))
                ok=true;
            verbose("Ok uploading... ");
            if (!ok){
                System.out.println("\r\n# Error sending the file");
                return false;
            }
            if (object.available() > 0) {
                while (put(PUT_SEND_MORE, object)) {
                    //end = false;
                    ok = false;
                    ans =readAnswer(1000);
                    if (ans.length > 0 && ((ans[0]&0xff)==0x90 || (ans[0]&0xff)==0xA0))
                        ok=true;
                    if (!ok) {
                        System.out.println("\r\n# Error sending the file");
                        return false;
                    }
                }
            }
            else
                return true;
            if (object.getName().toLowerCase().indexOf(".jar") > -1 || object.getName().toLowerCase().indexOf(".jad") > -1) {
                object.setUserPerm("\"RWD\"");
                put(PUT_CHANGE, object);
                sleep(500);
                readAll();
            }
            return true;
        } catch (IOException ex) {
            System.out.println("### IOError sending file");
            throw new OBEXException(ex.getMessage(), ex);
        }

    }

    /**
     * function to send files
     * @param filename the name of the file to be written
     * @param f the stream containing the file
     * @throws OBEXException if put operation fails
     */
    public boolean sendFile(String filename, FileInputStream f) throws OBEXException {
        OBEXFile object = new OBEXFile(f, filename);
        return sendFile(object);
    }

    /**
     * helper function to preapare names in bytes for obex
     * @param name to be prepared
     * @return the result bytes
     */
    private byte[] toBytes(String name) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        for (int i = 0; i < name.length(); i++) {
            bos.write(0);
            bos.write((int) name.charAt(i));
        }
        byte[] b = bos.toByteArray();
        bos.reset();
        bos = null;
        return b;

    }

    /**
     * Not working well..
     * @return true if is connected
     */
    public boolean isOBEXMode() {
        return obexConnected;
    }

    /**
     * function that goes up or down a folder in the obex connection,
     * and can create folders
     * @param path to be changed
     * @param backwards if its backwards (not tested)
     * @param create if the folder need to  be created
     * @return the obex response
     * @throws OBEXException
     */
    public byte[] setPath(String path, boolean backwards, boolean create) throws OBEXException {
        byte[] b;
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            bos.write((backwards ? SETPATH2_OPCODE : SETPATH_OPCODE)); //setpath
            bos.write(0);
            bos.write(0); //length of package, that will be deducted later
            bos.write((create ? 0 : 2)); //flag   2 - Dont create dir, 0 - Create dir and move to it
            bos.write(0); //constant
            bos.write(1); //length of header of data 2B - length of data, including the state of header, that is, +3
            b = toBytes(path); //name is finished with 00
            int pos = b.length;
            pos += 3;
            bos.write(pos >> 8);
            bos.write(pos & 255);
            bos.write(b);
            b = bos.toByteArray();
            pos = b.length;
            b[1] = (byte) (pos >> 8);
            b[2] = (byte) (pos & 255);
            os.write(b);
            sleep(200);
            return readAll();
        } catch (IOException ex) {
            throw new OBEXException("Can't select directory (" + ex.getMessage() + ")", ex);
        }
    }

    /**
     * function for obex get operations, auto perpares the string to bytes
     * @param what you want to get
     * @return what you got
     * @throws OBEXException if operation was not successful
     */
    private String get(String what) throws OBEXException {
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            byte[] b;
            bos.write(GET_OPCODE);

            bos.write(0);
            bos.write(0);
            if (what.startsWith("x-obex/")) {
                bos.write(0x42);
                b = what.getBytes();
            } else {
                bos.write(1);
                b = toBytes(what);
            }
            int pos = b.length + 3;
            bos.write(pos >> 8);
            bos.write(pos & 255);
            bos.write(b);

            b = bos.toByteArray();
            pos = b.length;
            b[1] = (byte) (pos >> 8);
            b[2] = (byte) (pos & 255);

            return new String(get(b));
        } catch (IOException ex) {
            ex.printStackTrace();
            throw new OBEXException("Can't get (" + ex.getMessage() + ")", ex);
        }
    }

    /**
     * function for obex get operations
     * @param what you want to get
     * @return what you got
     * @throws OBEXException if operation was not successful
     */
    public byte[] get(byte[] what) throws OBEXException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        byte[] b = null;
        try {
            os.write(what);
            os.flush();
            boolean end = false;
            int len;
            int count;
            int ch;
            int l1;
            int l2;
            int err = 0;
            int tms = 0;
            readloop:
            while (!end) {
                tms++;
                len = 0;
                count = 4;
                do {
//                    sleep(10);
                    ch = is.read();

                    // TODO should implement a better reading with wait and timeout
                    if (ch == -1) {
                        err++;
                        verbose("# Warning: Invalid byte sent " + Integer.toHexString(ch));
                        if (err > 10) {
                            if (bos.size() == 0) {
                                throw new OBEXException("Unknown error, cannot communicate properly. Try again.");
                            }
                            break readloop;
                        }
                        // WORKAROUND: need more time for the answer to be prepared ?
                        sleep(100);
                    }
                } while (ch == -1);
                if (ch == 0xA0) {
                    end = true;
                    verbose("# Success!");
                } else if (ch == 0x90) {
                    verbose("# Loading (" + tms + ")");
                } else if (ch == 0xC0) {
                    verbose("# Bad Request..");
                    throw new OBEXException("Bad Request"); //Need to be thrown ?
                } else if (ch == 0xC1 || ch == 0xC3) {
                    verbose("# Forbidden... " + Integer.toHexString(ch));
                }
//                sleep(10);
                is.skip(2);
//                sleep(10);
                l1 = is.read();
                if (l1 == 0xC3) {
//                    sleep(10);
                    is.skip(4);
                    count += 5;
                    sleep(10);
                    l1 = is.read();
                }
                if (l1 == 0x48) {
//                    sleep(10);
                    l1 = is.read();
                    if (l1 < 0) {
                        l1 += 256;
                    }
//                    sleep(10);
                    l2 = is.read();
                    if (l2 < 0) {
                        l2 += 256;
                    }
                    count += 3;
                    len = (l1 * 256) + l2;
                    count = 3;
                }
//                System.out.println("Package of " + len);

                if (!end) {
                    // Get payload bytes

                    int sizeToRead = len - count;
                    // prepare the byte array to read sizeToRead bytes
                    if (sizeToRead > 0) {
                        byte toread[] = new byte[sizeToRead];
                        // c: counter of read bytes
                        int c = 0;
                        while (c < sizeToRead) {
                            // read chunk, store in toread[] and return in 'i' how much bytes have been read
                            int i = is.read(toread, c, sizeToRead - c);
                            c += i;
                            //System.out.print(""+i+"|");
                        }
                        // add to read output stream
                        bos.write(toread);
                    }
                }
                os.write(GET_OPCODE);
                os.write(00);
                os.write(3);
                os.flush();
//                sleep(100);
                if (err > 10) {
                    throw new OBEXException("Unknown error, improper communication. Try again.");
                }
            }
            b = bos.toByteArray();
            bos.reset();
            bos = null;
        } catch (IOException ex) {
            ex.printStackTrace();
            throw new OBEXException(ex.getMessage(), ex);
        } catch (NegativeArraySizeException ex) {
            ex.printStackTrace();
            throw new OBEXException("Unknown error, try again " + ex.getMessage(), ex);
        }
        return b;
    }

    /**
     * function that formats the flash filesystem
     * @return true if successful
     * @throws OBEXException if the operation was not successful
     */
    public boolean format() throws OBEXException{
        try{
            verbose("Formatting");
            os.write(FORMAT);
            os.flush();
            byte[] ans = readAnswer(60000);
            verbose("Got: ");
            printHexChars(ans);
            if (ans.length > 0 && (ans[0]&0xff)==0xA0)
                return true;
            else
                return false;
        }catch(IOException ex){
            verbose("###OBEXProtocol::format - IOException: "+ex);
            throw new OBEXException("Error while formatting the flash filesystem" + ex.getMessage(), ex);
        }
    }

    /**
     * function for obex put operation
     * @param type of the put operation you want to realize
     * @param object the object you want to send or change settings
     * @return true if could put, false if not.
     * @throws OBEXException if operation was not successful
     */
    public boolean put(int type, OBEXFile object) throws OBEXException {
        if (object == null) {
            throw new InvalidParameterException("OBEXObject cannot be null");
        }
        if (object.getName().equals("")) {
            throw new InvalidParameterException("Invalid file");
        }
        if (type == PUT_CHANGE && (object.getUserPerm() == null || object.getUserPerm().length() <= 0)) {
            throw new InvalidParameterException("Change type needs OBEXObject property setted");
        }
        if (type >= PUT_SEND && (object.getInputStream() == null)) {
            throw new InvalidParameterException("To send files OBEXObject needs a filestream pointing to the file");
        }
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        byte[] b = null;
        boolean end = false;
        int pos = 0;
        try {
            boolean small = false;
            int maxp = this.maxpacket;
            if (type == PUT_SEND_MORE && object.available() <= 0) {
                return false;
            } else {
                maxp = maxpacket - 40;
            }
            if (type == PUT_SEND) {
                if (object.available() <= 512) {
                    small = true;
                }
            }
            if (type == PUT_SEND_MORE) {
                bos.write(0x02);
            } else if (type == PUT_SEND && !small) {
                bos.write(0x2);
            } else {
                bos.write(0x82);
            }
            bos.write(0);
            bos.write(0);//size, to be filled up later
            if (type < PUT_SEND_MORE) {
                bos.write(1);
                b = toBytes(object.getName());
                pos = b.length + 3;
                bos.write(pos >> 8);
                bos.write(pos & 255);
                bos.write(b);
                if (type == PUT_CHANGE) {
                    bos.write(0x4C);
                    bos.write(0);
                    bos.write(object.getUserPerm().length() + 3 + 2);
                    bos.write(0x38);
                    bos.write(object.getUserPerm().length());
                    bos.write(object.getUserPerm().getBytes());
                } else {
                    bos.write(0xC3);
                    if (type == PUT_DELETE) {
                        bos.write(0);
                        bos.write(0);
                        bos.write(0);
                        bos.write(0);
                    } else {
                        int flength = object.available();
                        verbose("@ Length of file is: " + flength);
                        int x = (flength >> 24) & 255;
                        bos.write(x);

                        x = (flength >> 16) & 255;
                        bos.write(x);

                        x = (flength >> 8) & 255;
                        bos.write(x);

                        x = (flength & 255);
                        bos.write(x);

                        bos.write(0x44);
                        bos.write(0);
                        bos.write(0x12);
                        bos.write(getTime().getBytes());
                        pos = (maxp - bos.size());

                        if (small) {
                            bos.write(0x49);
                        } else {
                            bos.write(0x48);
                        }


                        if ((pos - 3) > object.available()) {
                            flength += 3;
                        }
                        bos.write(pos >> 8);
                        bos.write(pos & 255);

                        b = new byte[pos - 3];
                        object.read(b);
                        bos.write(b);
                    }
                }
            } else {
                bos.write(0x48);
                pos = maxp - 6;
                if (object.available() < pos) {
                    pos = object.available() + 3;
                    end = true;
                } else {
                    pos += 3;
                }
                bos.write(pos >> 8);
                bos.write(pos & 255);
                b = new byte[pos - 3];
                object.read(b);

                //System.out.println("Header of block:" + pos);
                //System.out.println("other block: " + b.length);
                bos.write(b);
            }

            b = bos.toByteArray();
            pos = b.length;
            //System.out.println("packet:" + pos);
            b[1] = (byte) (pos >> 8);
            b[2] = (byte) (pos & 255);
            if (end && type == PUT_SEND_MORE) {
                //System.out.println("end of file " + object.getFileStream().available());
                b[0] = (byte) 0x82;
                b[3] = (byte) 0x49;
            }

            os.write(b);
            //verbose("File Data:\n");
            //verbose(new String(b));
            //verbose("\nHex output:\n");
            //printHexChars(b);
            //verbose("\nFile End");

            os.flush();

            return true;
        } catch (IOException ex) {
            throw new OBEXException("Put operation error " + ex.getMessage(), ex);
        }

    }

    private void printHexChars(byte[] b){
        if (!verbose)
            return;
        for(int i=0;i<b.length;i++)
            System.out.print(Integer.toHexString(b[i]&0xff)+"-");
    }

    private void printHexChars(int[] b){
        if (verbose == false)
            return;
        for(int i=0;i<b.length;i++)
            System.out.print(Integer.toHexString(b[i]&0xff)+"-");
    }

    /**
     * This function just be used in multithreading for aborting put and get operations.
     * Its not implemented yet. (It will be implemented someday?)
     * @return byte array with response
     * @throws OBEXException
     */
    public byte[] abort() throws OBEXException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * Opens an Obex connection with the serial streams.
     * Needs to be in datamode.
     * @return true if success (should be void? since it will never return false)
     * @throws OBEXException if fails.
     */
    public boolean enterOBEXMode() throws OBEXException {
        boolean success = false;
        try {
            switch (getFlowControl()) {
                case FLOW_NONE:
                    send("at\\q0\r\n");
                    break;
                case FLOW_XONXOFF:
                    send("at\\q1\r\n");
                    break;
                case FLOW_RTSCTS:
                    send("at\\q3\r\n");
                    break;
            }

            send("AT^SQWE=0\r\n");
            if (send("AT^SQWE=3\r\n").indexOf("ERROR") > -1) {
                throw new OBEXException("RGGGSM mode is wrong.");
            }
            sleep(200);
            os.write(OBEX_CONNECT);
            os.flush();
            sleep(500);
            byte[] b = readAll();
            printHexChars(b);
            if (b.length > 0 && (b[0]&0xFF) == 0xA0) {
                verbose("# Connected!");
                success = true;
//               int length = (256 * b[1]) + b[2];
//               int flags = b[4];
                //double obexVersion = b[3];
                //maxpacket = (256 * b[5]) + b[6];
                maxpacket = ((b[5]&0xFF)<<8) | b[6]&0xFF;

//                    verbose("# Length " + length);
//                verbose("# ObexVersion " + obexVersion / 10);
//                verbose("# MaxPacket " + maxpacket);
//                    verbose("# Flags " + flags);
                obexConnected = true;
            } else {
                String message = "Error activating OBEX, invalid response: ";
                if (b.length > 0) {
                    message += Integer.toHexString(b[0]);
                } else {
                    message += "Sem resposta";
                }
                message += ". Please try again. If the error persists you should restart your TC65 module.";
                throw new OBEXException(message);
            }
        } catch (OBEXException ex) {
            throw ex;
        } catch (IOException ex) {
            throw new OBEXException(ex.getMessage(), ex);
        }
        return success;
    }

    /**
     * Closes the obexconnection, and switch to commandmode
     * @return true if switch to commandmode was sucessful (should be void?)
     * @throws OBEXException if there was an error disconnecting
     */
    public boolean leaveOBEXMode() throws OBEXException {
        byte b[] = null;
        try {
            if (isCommandMode()) {
                obexConnected = false;
                return true;
            }
            sleep(500);
            os.write(OBEXInterface.DISCONNECT_OPCODE);
            os.write(0x00);
            os.write(0x03);

            os.flush();
            sleep(500);
            byte[] av = readAll();
            for (int i = 0; i < av.length; i++) {
                System.out.print(av[i] + " ");
            }
            verbose("# Leaving OBEX mode...");
            for (int i = 0; i < 5; i++) {
                if (send("+++", 1500).toUpperCase().indexOf("OK") > -1) {
                    obexConnected = false;
                    return true;
                }
            }
            if (isCommandMode()) {
                obexConnected = false;
                return true;
            } else {
                throw new OBEXException("Could not leave OBEX mode");
            }
        } catch (OBEXException ex) {
            throw ex;
        } catch (IOException ex) {
            throw new OBEXException("Could not enter OBEX mode " + ex.getMessage(), ex);
        }
    }

    /**
     *
     * @param path of the file inside module to run
     * @return true if could start the application.
     * @throws IOException if
     */
    public void runApp(String path) throws IOException {
        verbose("# Starting " + path + " in module.");
        try {
            leaveOBEXMode();
            if (send("at^sjra=" + path + "\r\n", 2000).indexOf("OK") > 0) {
                verbose("# Application running in module");
                return;
            } else {
                throw new OBEXException("Wrong application path or mode");
            }
        } catch (IOException ex) {
            verbose("### Error starting application, throwing. " + ex.getMessage());
            throw ex;
        }
    }

    /**
     * This functions trys to send an simple command (ATZ) to module to see if there is OK response
     * @return true if OK was found in ATZ response. false if no response.
     * @throws IOException if could not send data to stream
     */
    private boolean isCommandMode() throws IOException {
        if (send("ATZ\r\n").indexOf("OK") < 0) {
            return false;
        }
        return true;
    }

    /**
     * This function builds a prepared now time to put operations
     * @return a string cointaing the now date and time.
     */
    private String getTime() {
        StringBuffer buffer = new StringBuffer();
        Calendar calendar = Calendar.getInstance();
        buffer.append(calendar.get(Calendar.YEAR));
        int month = calendar.get(Calendar.MONTH) + 1;
        if (month < 9) {
            buffer.append(0);
        }
        buffer.append(month);
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        if (day < 10) {
            buffer.append(0);
        }
        buffer.append(day);
        buffer.append("T");
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        if (hour < 10) {
            buffer.append(0);
        }
        buffer.append(hour);
        int minute = calendar.get(Calendar.MINUTE);
        if (minute < 10) {
            buffer.append(0);
        }
        buffer.append(minute);
        int second = calendar.get(Calendar.SECOND);
        if (second < 10) {
            buffer.append(0);
        }
        buffer.append(second);
        String t = buffer.toString();
        buffer = null;
        return t;
    }
}
