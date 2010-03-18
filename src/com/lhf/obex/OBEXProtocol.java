/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
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
import java.util.Enumeration;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import com.lhf.obex.dao.OBEXFile;

/**
 *
 * @author Ricardo Guilherme Schmidt
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
            throw new InvalidParameterException("Parametro deve ser entre 0 e 2");
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
    private int maxpacket = 600;
    private String serial;
    private boolean wasTurnedOff = false;
    private boolean obexConnected = false;
    private boolean streamConnected = false;
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
        this.serial = serial;
        if (serial.indexOf("ttyS") < 0 && serial.indexOf("COM") < 0) {
            System.setProperty("gnu.io.rxtx.SerialPorts", serial);
        }
        verbose("# Conectando...");
        try {
            start(serial, check);
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
    public void openStreams(String portName) throws OBEXException {
        boolean b = false;
        try {
            openStreams(CommPortIdentifier.getPortIdentifier(portName));
        } catch (NoSuchPortException ex) {
            throw new OBEXException(portName + " não está disponível. " + ex.getMessage(), ex);
        }
    }

    /**
     * Open object streams with the tty/serial device
     * @param portIdentifier the port identifier
     * @return true if sucessful (can be void?)
     * @throws OBEXException if not sucessful
     */
    public void openStreams(CommPortIdentifier portIdentifier) throws OBEXException {
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
                streamConnected = true;
            }
        } catch (IOException ex) {
            verbose("### IOError: Não consigo abrir stream. " + ex.getMessage());
            throw new OBEXException("Não consigo abrir conexão " + ex.getMessage(), ex);
        } catch (UnsupportedCommOperationException ex) {
            verbose("### Error: Unsupported Communication Operation. " + ex.getMessage());
            throw new OBEXException("Unsupported Communication Operation" + ex.getMessage(), ex);
        } catch (PortInUseException ex) {
            verbose("### Error: " + portIdentifier.getName() + " está em uso. " + ex.getMessage());
            throw new OBEXException(portIdentifier.getName() + " está em uso por " + ex.getMessage(), ex);
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
            sleep(millis);
            os.flush();
            char c;
            while (is.available() > 0) {
                c = (char) is.read();
                buff.append(c);
            }
            if (veratcmd) {
                getOut().write(buff.toString().getBytes());
            }
        } catch (IOException ex) {
            verbose("### IOError: Impossível enviar " + new String(b));
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
                verbose("# Ligando o RGGGSM...");
                send("AT^SCFG=\"Userware/Autostart\",\"\",\"0\"\r\n");
                send("AT^SCFG=\"MEopMode/Airplane\",\"off\"\r\n", 1000);

            } else {
                wasTurnedOff = false;
            }
        } catch (IOException ex) {
            verbose("# Erro ao ligar o módulo.");
            throw ex;
        }
    }

    /**
     * Start function to openstream, turn on, switch to command mode and open obex stream
     * @param serial that will be connected to
     * @return true if connection was sucessful
     * @throws IOException something goes wrong
     */
    private void start(String serial, boolean check) throws IOException {
        try {
            openStreams(serial);
            if (check) {
                if (autoon > 0) {
                    turnOn();
                }
                if (!isCommandMode()) {
                    verbose("# Entrando em modo de comandos.");
                    try {
                        disconnect();
                    } catch (OBEXException e) {
                        throw new IOException("Não foi possível ativar modo de comandos, provavelmente por a aplicação estar rodando dentro do módulo ou por a porta USB estar em uso.");
                    }
                    sleep(500);
                    if (!isCommandMode()) {
                        throw new IOException("Não é possível comunicar-se com o módulo, reinicie-o e tente novamente.");
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
            verbose("# Desligando RGGGSM...");
            send("AT^SMSO\r\n", 1600);
        } catch (IOException ex) {
            verbose("### IOError: Não consegui desligar o RGGGSM . Ele disse" + ex.getMessage());
            ex.printStackTrace();
            throw ex;
        }
    }

    /**
     * function to close and clean the streams.
     * @throws IOException if was not possible to close streams (in use?)
     */
    private void closeStreams() throws IOException {
        if (os != null && is != null) {
            try {
                os.close();
                os = null;
                is.close();
                is = null;
                streamConnected = false;
            } catch (IOException ex) {
                verbose("### IOError: Não consegui fechar as streams.");
                ex.printStackTrace();
                throw ex;
            }
        }
    }

    /**
     * Function to read all data from inputstream
     * @return the data read
     * @throws IOException if could not read.
     */
    private int[] readAll() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        int res[] = new int[is.available()];
        try {
            int i = 0;

            while (is.available() > 0) { //TODO: Optimization
                res[i] = is.read();
                i++;
            }
//            byte buf[] = new byte[maxpacket];
//            int c = is.read(buf,0,buf.length);
//            int zeros = 0; // workaround: for some reason we don't get -1 when there are no more bytes to read, so we let max 10 zero reads befor timeout
//            while(c != -1 && zeros<10){
//            	if(c==0) zeros++;
//            	//System.out.print(c+"|");
//            	baos.write(buf,0,c);
//            	c = is.read(buf,0,buf.length);
//            }
        } catch (IOException ex) {
            verbose("### Error: Não consigo ler dados disponiveis. " + ex.getMessage());
            throw ex;
        }
        // transform byte[] to int[]
//        int[] res = null;
//        res = new int[baos.size()];
//        byte baosb[] = baos.toByteArray();
//        for(int i=0;i<res.length;i++){
//        	res[i] = (int)baosb[i];
//        	if(res[i] < 0 ) res[i] += 256;
//        }
//        baos.reset();
//        baos = null;
//        baosb = null;
        return res;
    }

    /**
     * Function that closes everyting and disconnect module.
     * @throws OBEXException if something goes wrong
     */
    public void close() throws OBEXException {
        verbose("# Desconectando...");
        sleep(1000);
        try {
            if (obexConnected) {
                disconnect();
            }
            if (wasTurnedOff && autoon > 1) {
                turnOff();
            }
            closeStreams();
        } catch (IOException ex) {
            ex.printStackTrace();
            verbose("### IOError: Não consegui desconectar corretamente." + ex.getMessage());
            throw new OBEXException(ex.getMessage(), ex);
        }
        if (serialPort != null) {
            serialPort.close();
        }


    }

    /**
     * Function to get a folderlisting
     * @param path if not null goes to that folder, if null lists the actual folder
     * @return xml string containg the folder listing
     * @throws OBEXException if get operation fails
     */
    public String getFolderListing(String path) throws OBEXException {
        verbose("# Listando pasta...");
        if (path != null) {
            goTo(path);
        }

        String s = get("x-obex/folder-listing").trim();
        //For some misterious reason, in windows, sometimes the header came broken, this is a bad workaround in case it happens
        if (s.startsWith("listing version=\"1.0\">")) {
            verbose("# Cabeçalho XML quebrado, tentado arrumar...");
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
        if (fileNameWithoutPath.endsWith(".jar")) {
            throw new OBEXException("Não é possivel baixar arquivos Jar.");
        }
        verbose("# Baixando " + fileNameWithoutPath + "...");
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
                verbose("# Definindo diretorio para " + tmp);
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
        verbose("# Deletando o arquivo: " + object.getName());
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
            throw new OBEXException("Erro lendo resposta de PUT_DELETE.", ex);
        }
        return success;
    }

    public void changeUserPerm(OBEXFile object) {
    }

    /**
     * function to send files
     * @param object the object with filename and contents (or file stream)
     * @throws OBEXException
     */
    public void sendFile(OBEXFile object) throws OBEXException {
        try {
            boolean end = false;
            boolean ok = false;
            //goTo(filename);
            deleteFile(object);
            verbose("# Enviando: " + object.getName());
            put(PUT_SEND, object);
            end = false;
            while (!end) {
                while (is.available() > 0) {
                    is.read();
                    end = true;
                }
            }
            if (object.available() > 0) {
                while (put(PUT_SEND_MORE, object)) {
                    end = false;
                    ok = false;
                    while (!end) {
                        int ch;
                        while (is.available() > 0) {
                            //System.out.print(Integer.toHexString(ch) + " ");
                            ch = is.read();
                            if (ch == 0x90 || ch == 0xA0) {
                                ok = true;
                            }
                            //end = true;
                            // Important: we wait for the device to send at least one valid byte.
                            if (ch >= 0) {
                                end = true;
                            } else {
                                //if(!ok) System.out.print(".   ");
                            }
                            sleep(10);
                        }
                    }
//                    System.out.print((char) 8);
//                    System.out.print((char) 8);
//                    System.out.print((char) 8);
//                    System.out.print((pocitadlo < 100 ? "0" : "") + (pocitadlo < 10 ? "0" : "") + pocitadlo);
                    if (!ok) {
                        System.out.println("\r\n# Erro de transmissão");
                    }
                }
            }
            if (object.getName().toLowerCase().indexOf(".jar") > -1 || object.getName().toLowerCase().indexOf(".jad") > -1) {
                object.setUserPerm("\"RWD\"");
                put(PUT_CHANGE, object);
                sleep(500);
                readAll();
            }
        } catch (IOException ex) {
            System.out.println("### IOError enviando arquivo.");
            throw new OBEXException(ex.getMessage(), ex);
        }

    }
//    @Deprecated
//    public void sendFile(OBEXObject object) throws OBEXException {
//        try {
//            boolean end = false;
//            boolean ok = false;
//            //goTo(filename);
//
//            verbose("# Enviando: " + object.getFileName());
//            put(PUT_SEND, object);
//            end = false;
//            while (!end) {
//                while (is.available() > 0) {
//                    is.read();
//                    end = true;
//                }
//            }
//            int pocitadlo = 0;
//            if (object.available() > 0) {
//                while (put(PUT_SEND_MORE, object)) {
//                    pocitadlo++;
//                    end = false;
//                    ok = false;
//                    while (!end) {
//                        int ch;
//                        while (is.available() > 0) {
//                            //System.out.print(Integer.toHexString(ch) + " ");
//                            ch = is.read();
//                            if (ch == 0x90 || ch == 0xA0) {
//                                ok = true;
//                            }
//                            //end = true;
//                            // Important: we wait for the device to send at least one valid byte.
//                            if (ch >= 0) {
//                                end = true;
//                            } else {
//                                //if(!ok) System.out.print(".   ");
//                            }
//                            sleep(10);
//                        }
//                    }
////                    System.out.print((char) 8);
////                    System.out.print((char) 8);
////                    System.out.print((char) 8);
////                    System.out.print((pocitadlo < 100 ? "0" : "") + (pocitadlo < 10 ? "0" : "") + pocitadlo);
//                    if (!ok) {
//                        System.out.println("\r\n# Erro de transmissão");
//                    }
//                }
//            }
//            if (object.getFileName().toLowerCase().indexOf(".jar") > -1 || object.getFileName().toLowerCase().indexOf(".jad") > -1) {
//                object.setProperty("\"RWD\"");
//                put(PUT_CHANGE, object);
//                sleep(500);
//                readAll();
//            }
//        } catch (IOException ex) {
//            System.out.println("### IOError enviando arquivo.");
//            throw new OBEXException(ex.getMessage(), ex);
//        }
//
//    }

    /**
     * function to send files
     * @param filename the name of the file to be written
     * @param f the stream containing the file
     * @throws OBEXException if put operation fails
     */
    public void sendFile(String filename, FileInputStream f) throws OBEXException {
        OBEXFile object = new OBEXFile(f, filename);
        sendFile(object);
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
    public boolean isObexConnected() {
        return obexConnected;
    }

    /**
     * function that goes up or down a folder in the obex connection,
     * and can create folders
     * @param path to be changed
     * @param beckwards if its backwards (not tested)
     * @param create if the folder need to  be created
     * @return the obex response
     * @throws OBEXException
     */
    public int[] setPath(String path, boolean beckwards, boolean create) throws OBEXException {
        byte[] b;
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            bos.write((beckwards ? SETPATH2_OPCODE : SETPATH_OPCODE)); //setpath
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
            throw new OBEXException("Não consigo selecionar diretório (" + ex.getMessage() + ")", ex);
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
            throw new OBEXException("Não consigo trazer (" + ex.getMessage() + ")", ex);
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

                    // TODO should implement a batter reading with wait and timeout  
                    if (ch == -1) {
                        err++;
                        verbose("# Aviso: windows enviou byte invalido: " + Integer.toHexString(ch));
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
                    verbose("# Sucesso!");
                } else if (ch == 0x90) {
                    verbose("# Carregando (" + tms + ")");
                } else if (ch == 0xC0) {
                    verbose("# Request ruim..");
                    throw new OBEXException("Bad Request"); //Need to be thrown ?
                } else if (ch == 0xC1 || ch == 0xC3) {
                    verbose("# Proíbido... " + Integer.toHexString(ch));
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
                    throw new OBEXException("Erro desconhecido, comunicação impropria. Tente novamente.");
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
            throw new OBEXException("Erro desconhecido, tente novamente. " + ex.getMessage(), ex);
        }
        return b;
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
            throw new InvalidParameterException("Arquivo inválido.");
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
            bos.write(0);
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
                        //     System.out.println("Lenght of file is: " + flength);
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
            os.flush();

            return true;
        } catch (IOException ex) {
            throw new OBEXException("Não consegui colocar OP." + type + " em " + object.getName() + "." + ex.getMessage(), ex);
        }

    }

//    /**
//     * function for obex put operation
//     * @param type of the put operation you want to realize
//     * @param object the object you want to send or change settings
//     * @return true if could put, false if not.
//     * @throws OBEXException if operation was not successful
//     */
//    public boolean put(int type, OBEXObject object) throws OBEXException {
//        if (object == null) {
//            throw new InvalidParameterException("OBEXObject cannot be null");
//        }
//        if (object.getFileName().equals("")) {
//            throw new InvalidParameterException("Arquivo inválido.");
//        }
//        if (type == PUT_CHANGE && (object.getProperty() == null || object.getProperty().length() <= 0)) {
//            throw new InvalidParameterException("Change type needs OBEXObject property setted");
//        }
//        if (type >= PUT_SEND && (object.getInputStream() == null)) {
//            throw new InvalidParameterException("To send files OBEXObject needs a filestream pointing to the file");
//        }
//        ByteArrayOutputStream bos = new ByteArrayOutputStream();
//        byte[] b = null;
//        boolean end = false;
//        int pos = 0;
//        try {
//            boolean small = false;
//            int maxp = this.maxpacket;
//            if (type == PUT_SEND_MORE && object.available() <= 0) {
//                return false;
//            } else {
//                maxp = maxpacket - 40;
//            }
//            if (type == PUT_SEND) {
//                if (object.available() <= 512) {
//                    small = true;
//                }
//            }
//            if (type == PUT_SEND_MORE) {
//                bos.write(0x02);
//            } else if (type == PUT_SEND && !small) {
//                bos.write(0x2);
//            } else {
//                bos.write(0x82);
//            }
//            bos.write(0);
//            bos.write(0);
//            if (type < PUT_SEND_MORE) {
//                bos.write(1);
//                b = toBytes(object.getFileName());
//                pos = b.length + 3;
//                bos.write(pos >> 8);
//                bos.write(pos & 255);
//                bos.write(b);
//                if (type == PUT_CHANGE) {
//                    bos.write(0x4C);
//                    bos.write(0);
//                    bos.write(object.getProperty().length() + 3 + 2);
//                    bos.write(0x38);
//                    bos.write(object.getProperty().length());
//                    bos.write(object.getProperty().getBytes());
//                } else {
//                    bos.write(0xC3);
//                    if (type == PUT_DELETE) {
//                        bos.write(0);
//                        bos.write(0);
//                        bos.write(0);
//                        bos.write(0);
//                    } else {
//                        int flength = object.available();
//                        //     System.out.println("Lenght of file is: " + flength);
//                        int x = (flength >> 24) & 255;
//                        bos.write(x);
//
//                        x = (flength >> 16) & 255;
//                        bos.write(x);
//
//                        x = (flength >> 8) & 255;
//                        bos.write(x);
//
//                        x = (flength & 255);
//                        bos.write(x);
//
//                        bos.write(0x44);
//                        bos.write(0);
//                        bos.write(0x12);
//                        bos.write(getTime().getBytes());
//                        pos = (maxp - bos.size());
//
//                        if (small) {
//                            bos.write(0x49);
//                        } else {
//                            bos.write(0x48);
//                        }
//
//
//                        if ((pos - 3) > object.available()) {
//                            flength += 3;
//                        }
//                        bos.write(pos >> 8);
//                        bos.write(pos & 255);
//
//                        b = new byte[pos - 3];
//                        object.read(b);
//                        bos.write(b);
//                    }
//                }
//            } else {
//                bos.write(0x48);
//                pos = maxp - 6;
//                if (object.available() < pos) {
//                    pos = object.available() + 3;
//                    end = true;
//                } else {
//                    pos += 3;
//                }
//                bos.write(pos >> 8);
//                bos.write(pos & 255);
//                b = new byte[pos - 3];
//                object.read(b);
//
//                //System.out.println("Header of block:" + pos);
//                //System.out.println("other block: " + b.length);
//                bos.write(b);
//            }
//
//            b = bos.toByteArray();
//            pos = b.length;
//            //System.out.println("packet:" + pos);
//            b[1] = (byte) (pos >> 8);
//            b[2] = (byte) (pos & 255);
//            if (end && type == PUT_SEND_MORE) {
//                //System.out.println("end of file " + object.getFileStream().available());
//                b[0] = (byte) 0x82;
//                b[3] = (byte) 0x49;
//            }
//
//            os.write(b);
//            os.flush();
//
//            return true;
//        } catch (IOException ex) {
//            throw new OBEXException("Não consegui colocar OP." + type + " em " + object.getFileName() + "." + ex.getMessage(), ex);
//        }
//
//    }
    /**
     * This function just be used in multithreading for aborting put and get operations.
     * Its not implemented yet. (It will be implemented someday?)
     * @return array of int with response
     * @throws OBEXException
     */
    public int[] abort() throws OBEXException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * Opens an Obex connection with the serial streams.
     * Needs to be in datamode.
     * @return true if success (should be void? since it will never return false)
     * @throws OBEXException if fails.
     */
    public boolean connect() throws OBEXException {
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
                throw new OBEXException("O RGGGSM deve estar em modo errado.");
            }
            sleep(200);
            os.write(OBEX_CONNECT);
            os.flush();
            sleep(500);
            int b[] = readAll();

            if (b.length > 0 && b[0] == 0xA0) {
                verbose("# Conectado!");
                success = true;
//               int length = (256 * b[1]) + b[2];
//               int flags = b[4];
                double obexVersion = b[3];
                maxpacket = (256 * b[5]) + b[6];

//                    verbose("# Length " + length);
//                verbose("# ObexVersion " + obexVersion / 10);
//                verbose("# MaxPacket " + maxpacket);
//                    verbose("# Flags " + flags);
                obexConnected = true;
            } else {
                String message = "Erro ao ativar OBEX, resposta inválida: ";
                if (b.length > 0) {
                    message += Integer.toHexString(b[0]);
                } else {
                    message += "Sem resposta";
                }
                message += ". Tente novamente, se persistir, reinicie o RGGGSM.";
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
    public boolean disconnect() throws OBEXException {
        byte b[] = null;
        try {
            if (isCommandMode()) {
                obexConnected = false;
                return true;
            }
            sleep(500);
            //byte[] disc = {(byte) 0x81, 00, 3};
            os.write(OBEXInterface.DISCONNECT_OPCODE);
            os.write(0x00);
            os.write(0x03);

            os.flush();
            sleep(500);
            int[] av = readAll();
            for (int i = 0; i < av.length; i++) {
                System.out.print(av[i] + " ");
            }
            verbose("# Saindo do modo de dados...");
            for (int i = 0; i < 5; i++) {
                if (send("+++", 1200).indexOf("OK") > -1) {
                    obexConnected = false;
                    return true;
                }
            }
            if (isCommandMode()) {
                obexConnected = false;
                return true;
            } else {
                throw new OBEXException("Não foi possível sair do modo de dados.");
            }
        } catch (OBEXException ex) {
            throw ex;
        } catch (IOException ex) {
            throw new OBEXException("Não foi possivel fechar a comunicação OBEX. " + ex.getMessage(), ex);
        }
    }

    /**
     *
     * @param path of the file inside module to run
     * @return true if could start the application.
     * @throws IOException if
     */
    public void runApp(String path) throws IOException {
        path = path.replaceAll("\\", "/");
        if (!path.startsWith("a:/")) {
            if (!path.startsWith("/")) {
                path = "/" + path;
            }
            path = "a:" + path;
        }
        verbose("# Starting " + path + " in module.");
        try {
            if (send("at^sjra=\"" + path + "\"\r\n", 2000).indexOf("OK") > 0) {
                return;
            } else {
                throw new OBEXException("Wrong application or mode");
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

    /**
     * @return the streamConnected
     */
    public boolean isStreamConnected() {
        return streamConnected;
    }
}
