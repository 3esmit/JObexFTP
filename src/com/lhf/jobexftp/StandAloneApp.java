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

import com.lhf.jobexftp.ui.CommandFileUserInterface;
import com.lhf.jobexftp.ui.CommandLineUserInterface;
import com.lhf.jobexftp.ui.InteractiveUserInterface;
import com.lhf.jobexftp.ui.TelnetUserInterface;
import com.lhf.jobexftp.ui.UserInterface;
import com.lhf.obexftplib.etc.Log;
import com.lhf.obexftplib.etc.Utility;
import com.lhf.obexftplib.fs.OBEXFile;
import com.lhf.obexftplib.fs.OBEXFolder;
import com.lhf.obexftplib.io.ATConnection;
import com.lhf.obexftplib.io.OBEXClient;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;

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

    public static void main(String[] args) {
        try {
            if (args.length == 0) {
                new StandAloneApp().printUsage();
                System.exit(0);
            }
            for (String arg : args) {
                if ("--help".equals(arg) || "-h".equals(arg)) {
                    new StandAloneApp().printUsage();
                    System.exit(0);
                }
            }
            new StandAloneApp().exec(args);
        } catch (Exception e) {
            Log.info("PANIC", e);
        }
    }
    private String portname = "COM1";
    private int baudrate = 115200;

    public void exec(String[] args) throws Exception {
        Log.logLevel = Log.LOG_INFO;
//		Log.info("tc65sh v"+VERSION_STRING);
        UserInterface ui = null;
        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("-p") || args[i].equals("--portname")) {
                portname = args[i + 1];
            } else if (args[i].equals("-b") || args[i].equals("--baudrate")) {
                baudrate = Integer.parseInt(args[i + 1]);
            } else if (args[i].equals("-d") || args[i].equals("--debug")) {
                Log.logLevel = Log.LOG_DEBUG;
                Utility.getLogger().setLevel(Level.FINEST);
            } else if (args[i].equals("-q") || args[i].equals("--quiet")) {
                Log.logLevel = Log.LOG_NONE;
                Utility.getLogger().setLevel(Level.OFF);
            } else if (args[i].equals("-f") || args[i].equals("--file")) {
                Log.info("processing command file " + args[i + 1]);
                ui = new CommandFileUserInterface(new File(args[i + 1]));
            } else if (args[i].equals("-c") || args[i].equals("--commands")) {
                Log.info("processing commands " + args[i + 1]);
                ui = new CommandLineUserInterface(args[i + 1]);
            } else if (args[i].equals("-t") || args[i].equals("--telnet")) {
                ui = new TelnetUserInterface(Integer.parseInt(args[i + 1]));
            }
        }

        ATConnection device = new ATConnection(portname);
        device.setBaudRate(baudrate);
        try {
            Log.info("connecting to serial " + portname + " " + baudrate);
            device.setConnMode(ATConnection.MODE_DATA);
            OBEXClient obexClient = new OBEXClient(device);
            Log.info("connecting obex");
            obexClient.connect();
            if (ui == null) {
                ui = new InteractiveUserInterface();
                Log.info("starting interactive mode, type 'exit' to exit, 'help' for help.");
            }
            run(obexClient, ui);
            Log.info("disconnecting obex");
            obexClient.disconnect();
            Log.info("disconnecting serial port");
            device.setConnMode(ATConnection.MODE_DISCONNECTED);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void run(OBEXClient device, UserInterface ui) {
        try {
            String cmdline;
            while ((cmdline = ui.readCommand()) != null) {
                cmdline = cmdline.trim();
                Log.debug(getClass(), "executing command '" + cmdline + "'");
                String tok[] = Utility.split(cmdline);
                if (tok[0].equals("cd")) {
                    // cd <deviceDirectory>
                    if (tok.length > 1) {
                        device.changeDirectory(tok[1], false);
                    }
                    ui.setDir(device.getCurrentFolder().getPath());
                } else if (tok[0].equals("mkdir")) {
                    // mkdir <directory>
                    if (tok.length > 1) {
                        device.changeDirectory(tok[1], true);
                    }
                    ui.setDir(device.getCurrentFolder().getPath());
                } else if (tok[0].equals("ls") || tok[0].equals("dir")) {
                    // ls
                    ui.println(device.loadFolderListing().getListing());
                } else if (tok[0].equals("rm") || tok[0].equals("del")) {
                    // rm <deviceFilename>
                    if (tok.length > 1) {
                        device.removeObject(Utility.nameToBytes(tok[1]));
                    }
                } else if (tok[0].equals("put")) {
                    // put <localFilePath> <deviceFilename>
                    if (tok.length > 2) {
                        OBEXFile fileHolder = loadLocalFile(tok[1]);
                        device.removeObject(fileHolder);
                        device.writeFile(fileHolder);
                    }
                } else if (tok[0].equals("get")) {
                    // get <deviceFilename> <localFilePath>
                    if (tok.length > 2) {
                        OBEXFile fh = device.readFile(tok[1]);
                        saveLocalFile(fh);
                    }
                } else if (tok[0].equals("cat")) {
                    // cat <deviceFilename>
                    if (tok.length > 1) {
                        OBEXFile fh = device.readFile(tok[1]);
                        String catString = new String(fh.getContents());
                        ui.println(catString);
                    }
                } else if (tok[0].equals("erasedisk")) {
                    // erasedisk
                    device.eraseDisk();
                } else if (tok[0].equals("help")) {
                    printHelp(ui);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void printHelp(UserInterface ui) {
        ui.println("available commands:");
        ui.println("  cd <directory>");
        ui.println("  mkdir <directory>");
        ui.println("  dir (or ls)");
        ui.println("  put <localFilepath> <deviceFilename>");
        ui.println("  get <deviceFilename> <localFilepath>");
        ui.println("  rm (or del) <deviceFilename>");
        ui.println("  erasedisk (Attention!! Clears all content!!)");
        ui.println("  help");
        ui.println("  exit");
    }

    private void printUsage() {
        System.out.println("usage:");
        System.out.println("  tc65sh [OPTIONS]");
        System.out.println("");
        System.out.println("OPTIONS are:");
        System.out.println("  -p --portname <portname>");
        System.out.println("  -b --baudrate <baudrate>");
        System.out.println("  -d --debug");
        System.out.println("  -q --quiet");
        System.out.println("  -f --file <commandFile>");
        System.out.println("  -c --commands <command1;command2;...>");
        System.out.println("  -t --telnet <telnetPort>");
        System.out.println("  -h --help");
    }

    private String pad10(String s) {
        while (s.length() < 10) {
            s = " " + s;
        }
        return s;
    }

    private OBEXFile loadLocalFile(String filepath) throws IOException {
        File f = new File(filepath);
        FileInputStream in = new FileInputStream(f);
        byte[] buf = new byte[(int) f.length()];
        in.read(buf);
        in.close();
        OBEXFile file = new OBEXFile(f.getName());
        file.setContents(buf);
        return file;
    }

    private void saveLocalFile(OBEXFile fileHolder) throws IOException {
        File f = new File(fileHolder.getName());
        FileOutputStream out = new FileOutputStream(f);
        out.write(fileHolder.getContents());
        out.close();
    }

    private static void printDescription() {
        System.out.println("JObexFTP " + OBEXClient.version + " (15/10/2010)");
        System.out.println("Java Obex File Transfer Protocol application and library");
        System.out.println("Developed under/using 100% free software.");
        System.out.println("For more information access: http://www.lhf.ind.br/jobexftp/");
    }

    private static void printUsages() {
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
