/**
 * Last updated in 29/Jan/2011
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
import com.lhf.obexftplib.event.ATEventListener;
import com.lhf.obexftplib.fs.OBEXFile;
import com.lhf.obexftplib.io.ATConnection;
import com.lhf.obexftplib.io.OBEXClient;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Ricardo Guilherme Schmidt <ricardo@lhf.ind.br>, Christoph Vilsmeier <cv _at_ vilsmeier _minus_ consulting _dot_ de>
 */
public class StandAloneApp {

    public static void main(String[] args) {
        try {
            if (args.length == 0) {
                printUsage();
                System.exit(0);
            }
            for (String arg : args) {
                if ("--help".equals(arg) || "-h".equals(arg)) {
                    printUsage();
                    System.exit(0);
                } else if ("--version".equals(arg) || "-v".equals(arg)) {
                    printVersion();
                    System.exit(0);
                }
            }
            new StandAloneApp().exec(args);
        } catch (gnu.io.NoSuchPortException e) {
            Log.info("Fatal: Port is unavaliable.", e);
            System.exit(1);
        } catch (Exception e) {
            e.printStackTrace();
            Log.info("Fatal: ", e);
            System.exit(1);
        }
    }

    private static void printUsage() {
        printDescription();
        System.out.println();
        System.out.println("usage:");
        System.out.println("  jobexftp -p PORT [mode] [options]");
        System.out.println();

        System.out.println("MODES are:");
        System.out.println("  -f --file <commandFile>");
        System.out.println("        Do not enter interactive mode but read commands");
        System.out.println("        from a command file instead");
        System.out.println("");
        System.out.println("  -c --commands <commands>");
        System.out.println("        Do not enter interactive mode but execute the given");
        System.out.println("        commands. Commands are separated by semikolon.");
        System.out.println("        Example: ... -c \"mkdir tmp;cd tmp;put test.txt\"");
        System.out.println("");
        System.out.println("  -t --telnet <telnetPort>");
        System.out.println("        Do not enter interactive mode but read commands");
        System.out.println("        from a telnet connection (a TCP/IP socket).");
        System.out.println("");
        System.out.println("  -r --run [jar/jad path]");
        System.out.println("        Do not enter interactive mode but run and output");
        System.out.println("        the program inside module. If no path, just output.");
        System.out.println("");
        System.out.println("OPTIONS are:");
        System.out.println("");
        System.out.println("  -p --portname <portname>");
        System.out.println("        Sets the serial portname.");
        System.out.println("        Use COM1, COM2, .. on windows");
        System.out.println("        or /dev/ttyS0 or similar on linux");
        System.out.println("");
        System.out.println("  -b --baudrate <baudrate>");
        System.out.println("         Sets the baudrate. Default is 115200");
        System.out.println("");
        System.out.println("  -fc --flowcontrol <flowcontrol>");
        System.out.println("        none for NONE flowcontrol");
        System.out.println("        rtscts for RTS/CTS");
//        System.out.println("        xonxoff for XON/XOFF"); This is not supported since is character-encoded. Binary dont work.
        System.out.println("        Default is NONE");
        System.out.println("");
        System.out.println("  -w --wait <seconds>");
        System.out.println("        Wait X seconds for timeout in atcommands");
        System.out.println("        Default is 2000 (means: 2 seconds)");
        System.out.println("");
        System.out.println("  -d --debug");
        System.out.println("        Puts out messages useful for debugging");
        System.out.println("");
        System.out.println("  -q --quiet");
        System.out.println("        Say (almost) nothing");
        System.out.println("");
        System.out.println("  -h --help");
        System.out.println("        Shows this help screen");
        System.out.println("");
        printCredits();
    }

    private static void printDescription() {
        System.out.println("JObexFTP " + OBEXClient.version + " (29/01/2011)");
        System.out.println("Java Obex File Transfer Protocol.");
        System.out.println("http://www.github.com/3esmit/jobexftp/");
    }

    private static void printCredits() {
        System.out.println("JObexFTP Copyright (C) 2011 Ricardo Guilherme Schmidt <3esmit at gmail.com>");
        System.out.println("TC65SH UI Copyright (C) 2011 Christoph Vilsmeier <cv at vilsmeier-consulting.de>");
        System.out.println("This is free software, and you are welcome to redistribute it under certain conditions;");
    }

    private static void printVersion() {
        System.out.print(OBEXClient.version);
    }
    private byte flowControl = ATConnection.FLOW_NONE;
    private String portname = null;
    private int baudrate = 115200;
    private int timeout = 1000;
    private String runMode = null;

    public void exec(String[] args) throws Exception {
        Log.logLevel = Log.LOG_INFO;
        UserInterface ui = null;
        for (int i = 0; i < args.length; i++) {
            if ("-p".equals(args[i]) || "--portname".equals(args[i])) {
                portname = args[i + 1];
            } else if ("-b".equals(args[i]) || "--baudrate".equals(args[i])) {
                try {
                    baudrate = Integer.parseInt(args[i + 1]);
                } catch (NumberFormatException ex) {
                    Utility.getLogger().severe("Could not convert baudrate value. Please insert only numbers");
                }
            } else if ("-w".equals(args[i]) || "--wait".equals(args[i])) {
                try {
                    timeout = Integer.parseInt(args[i + 1]);
                } catch (NumberFormatException ex) {
                    Utility.getLogger().severe("Could not convert timeout value. Please insert only numbers");
                }
            } else if ("-fc".equals(args[i]) || "--flowcontrol".equals(args[i])) {
                String w = args[i + 1].toLowerCase();
                if (w.startsWith("none")) {
                    flowControl = ATConnection.FLOW_NONE;
                } else if (w.startsWith("xon")) {
                    flowControl = ATConnection.FLOW_XONXOFF;
                } else if (!w.startsWith("rts")) {
                    Utility.getLogger().severe("Invalid flow control.");
                }
            } else if ("-d".equals(args[i]) || "--debug".equals(args[i])) {
                Log.logLevel = Log.LOG_DEBUG;
                Utility.getLogger().setLevel(Level.FINEST);
            } else if ("-q".equals(args[i]) || "--quiet".equals(args[i])) {
                Log.logLevel = Log.LOG_NONE;
                Utility.getLogger().setLevel(Level.SEVERE);
            } else if ("-f".equals(args[i]) || "--file".equals(args[i])) {
                Log.info("processing command file " + args[i + 1]);
                ui = new CommandFileUserInterface(new File(args[i + 1]));
            } else if ("-c".equals(args[i]) || "--commands".equals(args[i])) {
                Log.info("processing commands " + args[i + 1]);
                ui = new CommandLineUserInterface(args[i + 1]);
            } else if ("-t".equals(args[i]) || "--telnet".equals(args[i])) {
                ui = new TelnetUserInterface(Integer.parseInt(args[i + 1]));
            } else if ("-r".equals(args[i]) || "--run".equals(args[i])) {
                runMode = "";
                if (args.length > i + 1 && !args[i + 1].startsWith("-")) {
                    runMode = args[i + 1];
                }
            }
        }
        if (portname == null) {
            Log.info("Please specify the port.");
            System.exit(0);
        }
        if (runMode != null) {
            run(portname, runMode);
            return;
        }
        ATConnection device = new ATConnection(portname);
        device.setBaudRate(baudrate);
        device.setFlowControl(flowControl);
        Log.info("connecting to serial " + portname + " " + baudrate);

        try {

            if (ui == null) {
                ui = new InteractiveUserInterface();
                Log.info("starting interactive mode, type 'exit' to exit, 'help' for help.");
            }
            processUserInterface(device, ui);

        } finally {
            if (runMode == null) {
                Log.info("disconnecting serial port");
                device.setConnMode(ATConnection.MODE_DISCONNECTED);
            }
        }

    }

    private void updateDir(UserInterface ui, OBEXClient obexClient) {
        ui.setDir(obexClient.getCurrentFolder().getPath() + "/");
    }

    private void processUserInterface(ATConnection device, UserInterface ui) throws IOException {
        OBEXClient obexClient = new OBEXClient(device);
        try {
            String cmdline;
            while ((cmdline = ui.readCommand()) != null) {
                cmdline = cmdline.trim();
                Log.debug(getClass(), "executing command '" + cmdline + "'");
                String tok[] = Utility.split(cmdline);
                device.removeATEventListener(ui);
                if (tok[0].startsWith("#") || tok[0].startsWith("//")) {
                    // do nothing, it's a comment
                } else if (tok[0].toUpperCase().startsWith("AT")) {
                    device.addATEventListener(ui);
                    obexClient.disconnect();
                    device.setConnMode(ATConnection.MODE_AT);
                    ui.setDir("$");
                    int to;
                    try {
                        to = Integer.parseInt(tok[1]);
                    } catch (Throwable t) {
                        to = timeout;
                    }
                    device.send((tok[0] + "\r").getBytes(), to);
                } else if (tok[0].equals("run")) {
                    obexClient.disconnect();
                    ui.setDir("$");
                    device.setConnMode(ATConnection.MODE_AT);
                    device.addATEventListener(new ATEventListener() {

                        public void ATEvent(byte[] event) {
                            System.out.print(new String(event));
                        }
                    });
                    if (tok.length > 1) {
                        if (!sjra(device, tok[1])) {
                            ui.println("Could not execute program");
                            continue;
                        }
                    }
                    runMode = "";
                    break;
                } else if (tok[0].equals("sleep")) {
                    if (tok.length > 1) {
                        long millis = Long.parseLong(tok[1]);
                        try {
                            Thread.sleep(millis);
                        } catch (InterruptedException ex) {
                        }
                    }
                } else if (tok[0].equals("help")) {
                    printHelp(ui);
                } else if (tok[0].equals("about")) {
                    printAbout(ui, device, obexClient);
                } else {
                    device.setConnMode(ATConnection.MODE_DATA);
                    obexClient.connect();
                    updateDir(ui, obexClient);
                    if (tok[0].equals("cd")) {
                        if (tok.length > 1) {
                            obexClient.changeDirectory(tok[1], false);
                        }
                        updateDir(ui, obexClient);
                    } else if (tok[0].equals("mkdir")) {
                        if (tok.length > 1) {
                            obexClient.changeDirectory(tok[1], true);
                        }
                        updateDir(ui, obexClient);
                    } else if (tok[0].equals("mv")) {
                        if (tok.length > 2) {

                            tok[1] = Utility.preparePath(tok[1]);
                            tok[2] = Utility.preparePath(tok[2]);

                            if (!tok[1].startsWith("/")) {
                                tok[1] = Utility.preparePath(obexClient.getCurrentFolder().getPath() + "/" + tok[1]);

                            }
                            if (!tok[2].startsWith("/")) {
                                tok[2] = Utility.preparePath(obexClient.getCurrentFolder().getPath() + "/" + tok[2]);
                            }
                            obexClient.moveObject(tok[1], tok[2]);
                        }
                    } else if (tok[0].equals("ls") || tok[0].equals("dir")) {
                        ui.println(obexClient.loadFolderListing().getListing());
                    } else if (tok[0].equals("rm") || tok[0].equals("del")) {
                        if (tok.length > 1) {
                            obexClient.removeObject(Utility.nameToBytes(tok[1]));
                        }
                    } else if (tok[0].equals("put")) {
                        if (tok.length > 1) {
                            OBEXFile fileHolder = loadLocalFile(tok[1]);
                            if (tok.length > 2) {
                                fileHolder.setName(Utility.getLastFolder(tok[2]));
                            }
                            obexClient.removeObject(fileHolder);
                            obexClient.writeFile(fileHolder);
                        }
                    } else if (tok[0].equals("chmod")) {
                        String user = "", group = "";
                        switch (tok.length) {
                            case 4:
                                group = tok[3];
                            case 3:
                                user = tok[2];
                            case 2:
                                obexClient.setObjectPerm(new OBEXFile(obexClient.getCurrentFolder(), tok[1]), user.toUpperCase(), group.toUpperCase());
                        }
                    } else if (tok[0].equals("get")) {
                        if (tok.length > 1) {
                            OBEXFile fh = obexClient.readFile(tok[1]);
                            File f = null;
                            if (tok.length > 2) {
                                f = new File(tok[2]);
                            }
                            saveLocalFile(fh, f);
                        }
                    } else if (tok[0].equals("cat")) {
                        if (tok.length > 1) {
                            OBEXFile fh = obexClient.readFile(tok[1]);
                            String catString = new String(fh.getContents());
                            ui.println(catString);
                        }
                    } else if (tok[0].equals("erasedisk")) {
                        if (tok.length > 1) {
                            if (tok[1].toLowerCase().startsWith("-y")) {
                                obexClient.eraseDisk();
                            }
                        } else {
                            Log.info("Are you sure you want to erase ALL disk?");
                            if (Utility.readStandardInput().toLowerCase().startsWith("y")) {
                                obexClient.eraseDisk();
                            }
                        }
                    } else if (!tok[0].trim().equals("")) {
                        ui.println("command " + tok[0] + " not recognized.");
                    }
                }
            }
        } finally {
            if (!obexClient.disconnect()) {
                Log.info("disconnecting obex");
            }
        }

    }

    private boolean sjra(ATConnection device, String absolutePath) throws IOException {
        if (!(absolutePath.startsWith("a:/"))) {
            if (absolutePath.startsWith("/")) {
                absolutePath = "a:" + absolutePath;
            } else {
                absolutePath = "a:/" + absolutePath;
            }
        }
        return !(new String(device.send(("AT^SJRA=" + absolutePath + "\r").getBytes(), 500)).contains("ERROR"));
    }

    private void printAbout(UserInterface ui, ATConnection device, OBEXClient obexClient) {
        String helper;
        ui.println("Connected to: " + device.getCommPortIdentifier().getName());
        ui.println("Device identified: " + device.getDevice().name());
        switch (device.getFlowControl()) {
            case ATConnection.FLOW_XONXOFF:
                helper = "XON/XOFF";
                break;
            case ATConnection.FLOW_RTSCTS:
                helper = "RTS/CTS";
                break;
            default:
                helper = "None";
                break;
        }
        ui.println("FlowControl: " + helper);
        ui.println("Baudrate: " + device.getBaudRate());
        switch (device.getConnMode()) {

            case ATConnection.MODE_AT:
                helper = "ATCommand";
                break;
            case ATConnection.MODE_DATA:
                helper = "Data";
                break;
            default:
                helper = "Disconnected";
                break;
        }
        ui.println("Mode: " + helper);
        if (device.getConnMode() == ATConnection.MODE_DATA) {
            try {
                ui.println("Current folder: " + obexClient.getCurrentFolder().getPath());
                ui.println("Disk total space: " + Utility.humanReadableByteCount(obexClient.getDiskSpace(), true));
                ui.println("Disk free space: " + Utility.humanReadableByteCount(obexClient.getFreeSpace(), true));
            } catch (IOException ex) {
                Logger.getLogger(StandAloneApp.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    private void printHelp(UserInterface ui) {
        ui.println("available commands:");
        ui.println("  cd <directory>");
        ui.println("  mkdir <directory>");
        ui.println("  mv <oldPath> <newPath>");
        ui.println("  dir (or ls)");
        ui.println("  put <localFilepath> <deviceFilename>");
        ui.println("  get <deviceFilename> <localFilepath>");
        ui.println("  rm (or del) <deviceFilename>");
        ui.println("  chmod <deviceFilename> [userPerm] [groupPerm]");
        ui.println("  sleep <milliseconds>");
        ui.println("  run [jar/jad path]");
        ui.println("  erasedisk [-y] ");
        ui.println("  about");
        ui.println("  help");
        ui.println("  exit");
    }

    private OBEXFile loadLocalFile(String filepath) throws IOException {
        File f = new File(filepath);
        OBEXFile file = new OBEXFile(f.getName());
        file.setInputStream(new FileInputStream(f));
        return file;
    }

    private void saveLocalFile(OBEXFile fileHolder, File f) throws IOException {
        if (f == null) {
            f = new File(fileHolder.getName());
        }
        FileOutputStream out = new FileOutputStream(f);
        out.write(fileHolder.getContents());
        out.close();
    }

    private void run(String portname, String absolutePath) throws Exception {
        ATConnection device = new ATConnection(portname) {

            @Override
            protected void onOpen() throws IOException {
                //does notting
            }

            @Override
            protected void onClose() throws IOException {
                //does notting
            }
        };
        device.addATEventListener(new ATEventListener() {

            public void ATEvent(byte[] event) {
                System.out.print(new String(event));
            }
        });
        device.setConnMode(1);
        sjra(device, absolutePath);
    }
}
