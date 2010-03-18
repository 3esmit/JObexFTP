
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.lhf.obexftp;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import com.lhf.obex.OBEXProtocol;
import com.lhf.obex.OBEXException;
import com.lhf.obex.dao.OBEXFile;
import com.lhf.obex.dao.OBEXObject;

/**
 *
 * @author Ricardo Guilherme Schmidt
 */
public class JObexFTP {

    public static final String version = "0.12 beta";
    private Vector<String> listPaths;
    private Vector<String> downloadPaths;
    private Vector<String> removePaths;
    private Vector<String> uploadPaths;
    private String moveTo = null;
    private boolean download = true;
    public static boolean verbose = false;
    public static boolean autorun = false;
    public static boolean veratcmd = false;
    public static int autoon = 2;
    private OBEXProtocol obexComm;

    /**
     *
     * @param args
     */
    public JObexFTP(String[] args) {
        if (!args[0].startsWith("-")) {
            try {
                obexComm = new OBEXProtocol(args[0], true);
                obexComm.connect();
            } catch (OBEXException ex) {
                System.exit(0);
            }
        }
        for (int i = 0; i < args.length; i++) { //TODO: optimize
            String string = args[i].trim();
            if (string.startsWith("-d")) {
                if (downloadPaths == null) {
                    downloadPaths = new Vector<String>();
                }
                for (int j = i + 1; j < args.length && !args[j].startsWith("-"); j++) {
                    downloadPaths.add(args[j]);
                }
            } else if (string.startsWith("-r")) {
                if (removePaths == null) {
                    removePaths = new Vector<String>();
                }
                for (int j = i + 1; j < args.length && !args[j].startsWith("-"); j++) {
                    removePaths.add(args[j]);
                }
            } else if (string.startsWith("-l")) {
                if (listPaths == null) {
                    listPaths = new Vector<String>();
                }
                for (int j = i + 1; j < args.length && !args[j].startsWith("-"); j++) {
                    listPaths.add(args[j]);
                }
            } else if (string.startsWith("-u")) {
                if (uploadPaths == null) {
                    uploadPaths = new Vector<String>();
                }
                for (int j = i + 1; j < args.length && !args[j].startsWith("-"); j++) {
                    uploadPaths.add(args[j]);
                }
            } else if (string.startsWith("-V")) {
                if (obexComm != null) {
                    obexComm.setVerbose(true);
                }
            } else if (string.startsWith("-M")) {
                int j = i + 1;
                if (j < args.length && !args[j].startsWith("-")) {
                    try {
                        moveTo = args[j];//TODO: this
                    } catch (Exception e) {
                    }
                }
            } else if (string.startsWith("-A")) {
                if (obexComm != null) {
                    obexComm.setVeratcmd(true);
                }
            } else if (string.startsWith("-R")) {
                autorun = true; //TODO: this
            } else if (string.startsWith("-S")) {
                download = false;
            } else if (string.startsWith("-T")) {
                int j = i + 1;
                if (j < args.length && !args[j].startsWith("-")) {
                    try {
                        int nv = Integer.parseInt(args[j]);
                        if (nv >= 0 && nv < 2) {
                            if (obexComm != null) {
                                obexComm.setAutoon(nv);
                            }
                        }
                    } catch (Exception e) {
                    }
                }
            } else if (string.startsWith("-F")) {
                int j = i + 1;
                if (j < args.length && !args[j].startsWith("-")) {
                    try {
                        int nv = Integer.parseInt(args[j]);
                        if (nv >= 0 && nv < 2) {
                            if (obexComm != null) {
                                obexComm.setFlowControl(nv);
                            }
                        }
                    } catch (Exception e) {
                    }
                }
            } else if (string.startsWith("-B")) {
                int j = i + 1;
                if (j < args.length && !args[j].startsWith("-")) {
                    try {
                        int nv = Integer.parseInt(args[j]);
                        if (obexComm != null) {
                            obexComm.setBaudRate(nv);
                        }
                    } catch (Exception e) {
                    }
                }
            }
        }

        if (removePaths != null) {
            if (removePaths.size() > 0) {
                for (int i = 0; i < removePaths.size(); i++) {
                    String path = removePaths.elementAt(i);
                    removePath(path);
                }
            }
        }
        if (uploadPaths != null) {
            if (uploadPaths.size() > 0) {
                for (int i = 0; i < uploadPaths.size(); i++) {
                    String path = uploadPaths.elementAt(i);
                    uploadPath(path);
                }
            }
        }
        if (listPaths != null) {
            if (listPaths.size() > 0) {
                for (int i = 0; i < listPaths.size(); i++) {
                    String path = listPaths.elementAt(i);
                    try {
                        System.out.println(obexComm.getFolderListing(path).trim());
                    } catch (OBEXException ex) {
                        Logger.getLogger(JObexFTP.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            } else {
                try {
                    System.out.println(obexComm.getFolderListing(null).trim());
                } catch (OBEXException ex) {
                    Logger.getLogger(JObexFTP.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        if (downloadPaths != null) {
            if (downloadPaths.size() > 0) {
                for (int i = 0; i < downloadPaths.size(); i++) {
                    String path = downloadPaths.elementAt(i);
                    downloadFile(path);
                }
            }
        }
        try {
            obexComm.close();
        } catch (OBEXException ex) {
            Logger.getLogger(JObexFTP.class.getName()).log(Level.SEVERE, null, ex);
        }
        obexComm = null;
    }

    /**
     * Performs a download
     * @param path
     */
    private void downloadFile(String path) {
        FileOutputStream f = null;
        String content = "";
        try {
            content = obexComm.getFile(path).trim();
        } catch (OBEXException ex) {
            System.out.println("### Error: " + ex.getMessage());
        }
        if (download) {
            try {
                f = new FileOutputStream(path);
                f.write(content.getBytes());
                f.flush();
                f.close();
            } catch (IOException ex) {
                Logger.getLogger(JObexFTP.class.getName()).log(Level.SEVERE, null, ex);
            } finally {
                f = null;
            }
        } else {
            System.out.println(content);
        }

    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Java Obex FileTransfer for Siemens/Cinterion Wireless modules TC65/XT65 ....");
            System.out.println();
            System.out.println("Usage: JObexFTP SERIAL_INTERFACE COMMAND <arguments> [OPTIONS]");
            System.out.println("Ex. Linux: JObexFTP /dev/ttyS0 -u /full_path/fileforupload -V");
            System.out.println("Ex. Windows: JObexFTP COM1 -u c:/full_path/fileforupload");
            System.out.println();
            System.out.println("Commands:");
            System.out.println("-l [<FOLDER>]\tListing folder");
            System.out.println("-u <PATH> [<FOLDER>]\tUpload file to a:/ in module");
            System.out.println("-d <PATH>    \tDownload file from module");
            System.out.println("-r <PATH>    \tDeleting file or folder");
            System.out.println("-v           \tPrint out the version");
            System.out.println("Options:");
            // System.out.println("-M <FOLDER>  \tMove to a folder, before/to uploading");
            System.out.println("-B <value>   \tChange BaudRate (def.: 115200)");
            System.out.println("-F <type>    \tChange FlowControl (def.: RTSCTS)");
            System.out.println("             \tTypes: 0: None; 1: XonXoff; 2: RTSCTS.  ");
            System.out.println("-R           \tAutorun uploaded jars");
            System.out.println("-S           \tJust stdout the file, don't save.");
            System.out.println("-V           \tView detalied process");
            System.out.println("-A           \tView ATCommand communication");
            System.out.println("-T <type>    \tChange auto turn on/off module.  ");
            System.out.println("             \tTypes: 0: disable auto on; 1: disable auto off;  ");
            System.out.println("             \t       2: normal behavior. ");
            System.out.println();
            System.out.println();
            System.out.println("Application developed by: ");
            System.out.println("Ondrej Janovský <oj@alarex.cz> www.m2marchitect.com");
            System.out.println("Ricardo Guilherme Schmidt <3esmit@gmail.com> www.lhf-instrumentacao.com\n\n");
            System.out.println();
            System.out.println();
            System.out.println("Using RxTx library from http://rxtx.qbang.org/");
            System.exit(0);
        }

        if (args[0].startsWith("-v")) {
            System.out.println("JObexFTP v" + version);
            System.exit(0);
        }

        new JObexFTP(args);
    }

    /**
     * Performs an upload
     * @param path
     */
    private void uploadPath(String path) {
        FileInputStream f = null;
        try {
            f = new FileInputStream(path);
            obexComm.sendFile(path, f);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(JObexFTP.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(JObexFTP.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                f.close();
            } catch (IOException ex) {
                Logger.getLogger(JObexFTP.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    /**
     * Performs a delete
     * @param path
     */
    private void removePath(String path) {
        try {
            obexComm.deleteFile(new OBEXFile(path));
        } catch (OBEXException ex) {
            Logger.getLogger(JObexFTP.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
