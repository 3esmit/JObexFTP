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

/**
 * JObexFTP standalone implementation
 * @author Ricardo Guilherme Schmidt, Radu Petrișor
 */
public class JObexFTP {

    public static final String version = "0.15 build 070610";
    private Vector<String> listPaths;
    private Vector<String> downloadPaths;
    private Vector<String> removePaths;
    private Vector<String> uploadPaths;
    private Vector<String> createPaths;
    private String moveTo = null;
    private String runJarPath = "";
    private boolean download = true;
    public static boolean verbose = false;
    public static boolean autorun = false;
    public static boolean veratcmd = false;
    public static boolean format = false;
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
                obexComm.enterOBEXMode();
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
            } else if (string.startsWith("-f")) {
                format = true;
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
            }else if (string.startsWith("-c")) {
                if (createPaths == null) {
                    createPaths = new Vector<String>();
                }
                for (int j = i + 1; j < args.length && !args[j].startsWith("-"); j++) {
                    createPaths.add(args[j]);
                }
            }else if (string.startsWith("-o")) {
                if(args.length > i+1){
                    String[] params = args[i+1].split(":");
                    try{
                        obexComm = new OBEXProtocol(params[0],Integer.parseInt(params[1]),true);
                        obexComm.enterOBEXMode();
                    }catch(Exception ex){
                        System.out.println("Exception: "+ex);
                        ex.printStackTrace();
                        System.exit(0);
                    }
                }
            } else if (string.startsWith("-V")) {
                verbose = true;
                
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
                int j = i + 1;
                if (j < args.length && !args[j].startsWith("-")) {
                    runJarPath = args[i+1];
                    autorun = true;
                } else {
                    System.out.println("Missing jar file path to run");
                }
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
        if (verbose) {
            if (obexComm != null) {
                    obexComm.setVerbose(true);
            }
        }
        if (format){
            if (obexComm != null)
                try{
                    if (obexComm.format()){
                        System.out.println("Format successful");
                    }else
                        System.out.println("Format failed");
                }catch(OBEXException ex){
                    System.out.println("Format failed: "+ex.getMessage());
                }
        }
        if (removePaths != null) {
            if (removePaths.size() > 0) {
                for (int i = 0; i < removePaths.size(); i++) {
                    String path = removePaths.elementAt(i);
                    navigateTo(path);
                    removePath(path);
                    navigateFrom(path);
                }
            }
        }
        if (createPaths != null) {
            if (createPaths.size() > 0) {
                for (int i = 0; i < createPaths.size(); i++) {
                    String path = createPaths.elementAt(i);
                    createFolder(path);
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
        if (autorun) {
            if (obexComm != null) {
                try {
                    obexComm.runApp(runJarPath);
                } catch (IOException ex) {
                    Logger.getLogger(JObexFTP.class.getName()).log(Level.SEVERE, null, ex);
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
            System.out.println("Usage: JObexFTP [SERIAL_INTERFACE] [-o IP_ADDRESS:PORT] COMMAND <arguments> [OPTIONS]");
            System.out.println("Ex. Linux: JObexFTP /dev/ttyS0 -u /full_path/fileforupload -V");
            System.out.println("Ex. Windows: JObexFTP COM1 -u c:/full_path/fileforupload");
            System.out.println();
            System.out.println("Commands:");
            System.out.println("-l [<FOLDER>]\tListing folder");
            System.out.println("-u <PATH> [<FOLDER>]\tUpload file to a:/ in module");
            System.out.println("-c <FOLDER>  \tCreate a folder on the module");
            System.out.println("-d <PATH>    \tDownload file from module");
            System.out.println("-r <PATH>    \tDelete a file or folder");
            System.out.println("-f           \tFormat the flash filesystem");
            System.out.println("-v           \tPrint out the version");
            System.out.println("Options:");
            // System.out.println("-M <FOLDER>  \tMove to a folder, before/to uploading");
            System.out.println("-B <value>   \tChange BaudRate (def.: 115200)");
            System.out.println("-F <type>    \tChange FlowControl (def.: RTSCTS)");
            System.out.println("             \tTypes: 0: None; 1: XonXoff; 2: RTSCTS.  ");
            System.out.println("-R <JARPATH> \tAutorun jar in <JARPATH>");
            System.out.println("-S           \tJust stdout the file, don't save.");
            System.out.println("-V           \tView detalied process");
            System.out.println("-A           \tView ATCommand communication");
            System.out.println("-T <type>    \tChange auto turn on/off module.  ");
            System.out.println("             \tTypes: 0: disable auto on; 1: disable auto off;  ");
            System.out.println("             \t       2: normal behavior. ");
            System.out.println();
            System.out.println();
            System.out.println("Application developed by: ");
            System.out.println("Ondrej Janovsk� <oj@alarex.cz> www.m2marchitect.com");
            System.out.println("Ricardo Guilherme Schmidt <3esmit@gmail.com> www.lhf-instrumentacao.com");
            System.out.println("Radu Petrișor <radu.petrisor@safefleet.eu> www.safefleet.eu\n\n");
            System.out.println("Florian Chiș <florian.chis@scada.ro> www.scada.ro\n\n");
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
            if(obexComm.sendFile(path, f))
                System.out.println("File " + path + " successfuly uploaded.");
            else
                System.out.println("Error while uploading " + path);

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

    private String navigateTo(String path){
        System.out.println("# Going to "+path);
        String[] pathList = path.split("/");
        for (int i=0;i<pathList.length-1;i++)
            try{
                obexComm.setPath(pathList[i], false, false);
            }catch(OBEXException ex){
                Logger.getLogger(JObexFTP.class.getName()).log(Level.SEVERE, null, ex);
            }
        return pathList[pathList.length-1];
    }

    private void navigateFrom(String path){
        System.out.println("# Going backwards from "+path);
        String[] pathList = path.split("/");
        for (int i=pathList.length-1;i>=0;i--)
            try{
                obexComm.setPath(pathList[i], true, false);
            }catch(OBEXException ex){
                Logger.getLogger(JObexFTP.class.getName()).log(Level.SEVERE, null, ex);
            }
    }

    private void createFolder(String folderName){
        try{
            obexComm.setPath(folderName, false, true);
        }catch(OBEXException ex){
            Logger.getLogger(JObexFTP.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
