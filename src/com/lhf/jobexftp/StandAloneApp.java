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
package com.lhf.jobexftp;

import com.lhf.obexftplib.etc.OBEXDevice;
import com.lhf.obexftplib.etc.Utility;
import com.lhf.obexftplib.fs.OBEXFile;
import com.lhf.obexftplib.io.ATConnection;
import com.lhf.obexftplib.io.OBEXClient;
import gnu.io.NoSuchPortException;
import gnu.io.PortInUseException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

/*
 * TODO:
 * commands to implement in interactive mode:
 * cd <dir>             => change directory
 * ls [<dir>]           => list directory
 * rm <file>            => removes a file
 * mv <file> <dest>     => moves a file
 * get <file> [<ldest>] => downloads a file
 * put <lsrc> [<dest>]  => uploads a file
 * erasedisk            => cleans up all files in unit
 *
 *
 */
/**
 *
 * @author Ricardo Guilherme Schmidt <ricardo@lhf.ind.br>
 */
public class StandAloneApp {

    private static final Logger logger = Utility.getLogger();
    private EnumSet<Option> options = EnumSet.noneOf(Option.class);
    private EnumSet<Command> commands = EnumSet.noneOf(Command.class);
    private ATConnection atConn = null;
    private OBEXClient obexClient = null;
    private boolean write = true;
    private int atsleep = 0;

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        if (args.length < 1) {
            printDescription();
            printUsage();
            printCredits();
            return;
        }
        if (args[0].startsWith("--")) {
        } else {
            StandAloneApp standAloneApp = new StandAloneApp(args);
        }
    }

    public StandAloneApp(String[] args) {
        processArgs(args);
        execute();
        close();
    }

    private void close() {
        if (atConn != null) {
            try {
                closeObexClient();
                atConn.setConnMode(0);
            } catch (IOException ex) {
                logger.log(Level.SEVERE, null, ex);
            }
        }
    }

    private void execute() {
        for (Iterator<Option> it = options.iterator(); it.hasNext();) {
            it.next().execute(this);
        }
        for (Iterator<Command> it = commands.iterator(); it.hasNext();) {
            it.next().execute(this);
        }
    }

    private void processArgs(String[] args) {
        if (!args[0].startsWith("-")) {
            try {
                atConn = new ATConnection(args[0]);
            } catch (NoSuchPortException ex) {
                logger.log(Level.SEVERE, null, ex);
            } catch (PortInUseException ex) {
                logger.log(Level.SEVERE, null, ex);
            }
        }
        ArrayList<String> arg = new ArrayList<String>();
        for (int i = 0; i < args.length; i++) {
            if (args[i].startsWith("-")) {
                int j = i + 1;
                for (; j < args.length && !args[j].startsWith("-"); j++) {
                    arg.add(args[j]);
                }
                String[] s = new String[arg.size()];
                add(args[i].charAt(1), arg.toArray(s));
                arg.clear();
                i = --j;
            }
        }
    }

    private void add(char c, String[] args) {
        if (Character.isUpperCase(c)) {
            options.add(Option.getOption(c, args));
        } else {
            commands.add(Command.getCommand(c, args));
        }
    }

    private void setDevice(String dev) {
        if (dev.toLowerCase().indexOf("sie") > -1 || dev.toLowerCase().indexOf("cint") > -1 || dev.toLowerCase().indexOf("tc65") > -1) {
            atConn.setDevice(OBEXDevice.TC65);
        }
    }

    private void closeObexClient() {
        try {
            if (obexClient != null) {
                obexClient.disconnect();
                obexClient = null;
            }
            atConn.setConnMode(1);
        } catch (IOException ex) {
            logger.log(Level.SEVERE, null, ex);
        }

    }

    private OBEXClient getObexClient() {
        if (obexClient == null) {
            try {
                atConn.setConnMode(2);
                obexClient = new OBEXClient(atConn);
                obexClient.connect();
            } catch (IOException ex) {
                logger.log(Level.SEVERE, null, ex);
            }

        }
        return obexClient;
    }

    private void listFolder(String[] args) {
        try {
            OBEXClient c = getObexClient();
            if (args != null && args.length > 0 && !args[0].equalsIgnoreCase("")) {
                c.changeDirectory(args[0], false);
            }
            if (write) {
                System.out.print(c.loadFolderListing().getListing());
            } else {
                System.out.print(new String(c.loadFolderListing().getContents()));

            }
            if (args != null && args.length > 0 && !args[0].equalsIgnoreCase("")) {
                c.changeDirectory("/", false);
            }

        } catch (IOException ex) {
            logger.log(Level.SEVERE, null, ex);
        }
    }

    private void uploadFile(String[] args) {
        try {
            OBEXClient c = getObexClient();
            if (args != null && args.length > 0) {
                String filenameS;
                String path = "/";
                File f;
                if (!args[0].equalsIgnoreCase("")) {
                    System.out.println("Sending " + args[0]);
                    f = new File(args[0]);
                    filenameS = f.getName();
                } else {
                    return;
                }
                if (args.length > 1 && !args[1].equalsIgnoreCase("")) {
                    filenameS = Utility.getLastFolder(args[1]);
                    if (filenameS == null || filenameS.equalsIgnoreCase("")) {
                        filenameS = f.getName();
                    }
                    path = Utility.removeLastFolder(args[1]);
                    c.changeDirectory(path, false);
                }

                OBEXFile file = new OBEXFile(filenameS);
                InputStream is = new FileInputStream(f);
                file.setInputStream(is);
                c.writeFile(file);
                c.changeDirectory("/", false);
            }
        } catch (IOException ex) {
            logger.log(Level.SEVERE, null, ex);
        }
    }
    private void createFolder(String[] args) {
        try {
            OBEXClient c = getObexClient();
            if (args != null && args.length > 0 && !args[0].equalsIgnoreCase("")) {
                c.changeDirectory(args[0], true);
                c.changeDirectory("/", false);
            }
        } catch (IOException ex) {
            logger.log(Level.SEVERE, null, ex);
        }
    }

    private void deleteFile(String[] args) {
        try {
            OBEXClient c = getObexClient();
            if (args != null && args.length > 0 && !args[0].equalsIgnoreCase("")) {
                String filenameS;
                String path = "/";
                filenameS = Utility.getLastFolder(args[0]);
                path = Utility.preparePath(Utility.removeLastFolder(args[0]));
                c.changeDirectory(path, false);
                boolean b = c.removeObject(new OBEXFile(Utility.createSimbolicFolderTree(path), filenameS));
                System.out.println("Removed " + filenameS + " in " + c.getCurrentFolder().getPath() + (b ? " with success" : "with error"));

                c.changeDirectory("/", false);
            }
        } catch (IOException ex) {
            logger.log(Level.SEVERE, null, ex);
        }
    }

    private void downloadFile(String[] args) {
        try {
            OBEXClient c = getObexClient();
            if (args != null && args.length > 0 && !args[0].equalsIgnoreCase("")) {
                String filenameS, folder;
                String path = Utility.preparePath(args[0]);
                filenameS = Utility.getLastFolder(path);
                folder = Utility.removeLastFolder(path);
                c.changeDirectory(folder, false);
                byte[] r = c.readFile(filenameS).getContents();
                c.changeDirectory("/", false);
                if (write) {
                    String savePath;
                    if (args.length > 1 && !args[1].equalsIgnoreCase("")) {
                        savePath = args[1];
                    } else {
                        savePath = "." + File.separator + filenameS;
                    }
                    File file = new File(savePath);
                    if (file.isDirectory()) {
                        file = new File(savePath + File.separator + filenameS);
                    }
                    FileOutputStream f = new FileOutputStream(file);
                    f.write(r);
                    f.close();
                } else {
                    System.out.println(new String(r));
                }
            }
        } catch (IOException ex) {
            logger.log(Level.SEVERE, null, ex);
        }
    }

    private void prompt() {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    private void eraseDisk() {
        try {
            OBEXClient c = getObexClient();
            c.eraseDisk();
        } catch (IOException ex) {
            logger.log(Level.SEVERE, null, ex);
        }
    }

    private void sendAtc(String[] args) {
        try {
            if (args != null && args.length > 0) {
                closeObexClient();
                for (int i = 0; i < args.length; i++) {
                    Thread.sleep(atsleep);
                    atConn.send((args[i] + "\r").getBytes(), 5000);
                }
            }
        } catch (Exception ex) {
            logger.log(Level.SEVERE, null, ex);
        }
    }

    private void showVersion() {
        printVersion();
    }

    private enum Option {

        FLOWCONTROL('F'), BAUDRATE('B'), MANUFACTER('D'), LOG_LEVEL('V'), DONTWRITE('S'), ATSLEEP('Z');
        private final char d;
        private String[] args;

        private Option(char d) {
            this.d = d;
        }

        static Option getOption(char c, String[] args) {
            Option[] cs = values();
            Option r = null;
            for (int i = 0; i < cs.length; i++) {
                Option options = cs[i];
                if (options.d == c) {
                    r = options;
                    r.args = args;
                }
            }
            return r;
        }

        public void execute(StandAloneApp app) {
            try {
                switch (this) {
                    case BAUDRATE:
                        app.atConn.setBaudRate(Integer.parseInt(args[0]));
                        break;
                    case DONTWRITE:
                        app.write = false;
                        break;
                    case FLOWCONTROL:
                        app.atConn.setFlowControl(Byte.parseByte(args[0]));
                        break;
                    case MANUFACTER:
                        app.setDevice(args[0]);
                        break;
                    case LOG_LEVEL:
                        Utility.configLogger(Level.parse(args[0]));
                        break;
                    case ATSLEEP:
                        app.atsleep = Integer.parseInt(args[0]);
                        break;
                }
            } catch (Throwable t) {
                t.printStackTrace();
                System.err.println("Error processing option " + this.d + ". Message: " + t.getClass().getName());
            }
        }
    }

    private enum Command {

        LISTFOLDER('l'), UPLOADFILE('u'), CREATEFOLDER('c'), DOWNLOADFILE('d'), DELETEFILE('r'), PROMPT('i'), ERASEDISK('f'), SENDATC('a'), SHOWVERSION('v');
        private final char d;
        private ArrayList<String[]> args = new ArrayList<String[]>();

        private Command(char d) {
            this.d = d;
        }

        public void execute(StandAloneApp app) {
            try {
                switch (this) {
                    case LISTFOLDER:
                        for (Iterator<String[]> it = args.iterator(); it.hasNext();) {
                            app.listFolder(it.next());
                        }
                        break;
                    case UPLOADFILE:
                        for (Iterator<String[]> it = args.iterator(); it.hasNext();) {
                            app.uploadFile(it.next());
                        }
                        break;
                    case CREATEFOLDER:
                        for (Iterator<String[]> it = args.iterator(); it.hasNext();) {
                            app.createFolder(it.next());
                        }
                        break;
                    case DELETEFILE:
                        for (Iterator<String[]> it = args.iterator(); it.hasNext();) {
                            app.deleteFile(it.next());
                        }
                        break;
                    case DOWNLOADFILE:
                        for (Iterator<String[]> it = args.iterator(); it.hasNext();) {
                            app.downloadFile(it.next());
                        }
                        break;
                    case PROMPT:
                        app.prompt();
                        break;
                    case ERASEDISK:
                        app.eraseDisk();
                        break;
                    case SENDATC:
                        for (Iterator<String[]> it = args.iterator(); it.hasNext();) {
                            app.sendAtc(it.next());
                        }
                        break;
                    case SHOWVERSION:
                        app.showVersion();
                        break;
                }
            } catch (Throwable t) {
                t.printStackTrace();

                System.err.println("Error processing command " + this.d + "." + "Message: " + t.getClass().getName());
            }
        }

        static Command getCommand(char c, String[] args) {
            Command[] cs = values();
            Command r = null;
            for (int i = 0; i < cs.length; i++) {
                Command commands = cs[i];
                if (commands.d == c) {
                    r = commands;
                    r.args.add(args);
                }
            }
            return r;
        }
    }

    private static void printDescription() {
        System.out.println("JObexFTP " + OBEXClient.version + " (15/10/2010)");
        System.out.println("Java Obex File Transfer Protocol application and library");
        System.out.println("Developed under/using 100% free software.");
        System.out.println("For more information access: http://www.lhf.ind.br/jobexftp/");
    }

    private static void printUsage() {
        System.out.println();
        System.out.println("Usage: jobexftp <serialPort> [<commands>] [<options>]");
        System.out.println("Commands");
        System.out.println(" -l [<folderPath>]             \tList folder [in choosen <folderPath>]");
        System.out.println(" -u <localFile> [<devicePath>] \tWrites the file in <localPath> to [<devicePath> in] device.");
        System.out.println(" -c <folderPath>               \tCreate <folderPath> structure in device");
        System.out.println(" -d <devicePath> [<localPath>] \tDownload file from device [and move it to <localPath>]");
        System.out.println(" -r <remotePath>               \tDelete the file or folder in device");
        System.out.println(" -i                            \tPrompts an interactive command line to device");
        System.out.println(" -f                            \tErases the device's flash filesystem");
        System.out.println(" -a <atc1> <atc2>...     \tSend at commands to device");
        System.out.println(" -v                            \tPrint out the version");
        System.out.println();
        System.out.println("Options");
        System.out.println(" -F <flowControl>              \tSet the flowcontrol to be used");
        System.out.println(" -B <baudRate>                 \tPrint out the version");
        System.out.println(" -V <logLevel>                 \tDefine console loglevel verbose.");
        System.out.println(" -D <manufacturer>             \tDefines manually the manufacturer device profile.");
        System.out.println(" -S                            \tDon\'t write files to local disk, just print them.");
        System.out.println(" -Z <time>                     \tTime to sleep (ms) in each atcmd sent by -a option");

    }

    private static void printCredits() {
        System.out.println();
        System.out.println("LHF JObexFTP Copyright (C) 2010 Ricardo Guilherme Schmidt <ricardo@lhf.ind.br>");
        System.out.println("This program comes with ABSOLUTELY NO WARRANTY; for details type `jobexftp --w'.");
        System.out.println("This is free software, and you are welcome to redistribute it under certain");
        System.out.println("conditions; type `jobexftp --c' for details.\n\n");
    }

    private static void printVersion() {
        System.out.println();
        System.out.println(OBEXClient.version);
    }
}
