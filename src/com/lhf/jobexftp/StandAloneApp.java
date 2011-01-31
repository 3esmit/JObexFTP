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
import com.lhf.obexftplib.fs.OBEXFile;
import com.lhf.obexftplib.io.ATConnection;
import com.lhf.obexftplib.io.OBEXClient;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.logging.Level;

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
        System.out.println("        none for no flowcontrol");
        System.out.println("        rtscts for RTS/CTS");
        System.out.println("        xonxoff for XON/XOFF");
        System.out.println("        Default is RTS/CTS");
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
        System.out.println();
        System.out.println(OBEXClient.version);
    }
    private byte flowControl = ATConnection.FLOW_RTSCTS;
    private String portname = null;
    private int baudrate = 115200;
    private int timeout = 2000;

    public void exec(String[] args) throws Exception {
        Log.logLevel = Log.LOG_INFO;
        UserInterface ui = null;
        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("-p") || args[i].equals("--portname")) {
                portname = args[i + 1];
            } else if (args[i].equals("-b") || args[i].equals("--baudrate")) {
                try {
                    baudrate = Integer.parseInt(args[i + 1]);
                } catch (NumberFormatException ex) {
                    Utility.getLogger().severe("Could not convert baudrate value. Please insert only numbers");
                }
            } else if (args[i].equals("-w") || args[i].equals("--wait")) {
                try {
                    timeout = Integer.parseInt(args[i + 1]);
                } catch (NumberFormatException ex) {
                    Utility.getLogger().severe("Could not convert timeout value. Please insert only numbers");
                }
            } else if (args[i].equals("-fc") || args[i].equals("--flowcontrol")) {
                String w = args[i + 1].toLowerCase();
                if (w.startsWith("none")) {
                    flowControl = ATConnection.FLOW_NONE;
                } else if (w.startsWith("xon")) {
                    flowControl = ATConnection.FLOW_XONXOFF;
                } else if (!w.startsWith("rts")) {
                    Utility.getLogger().severe("Invalid flow control.");
                }
            } else if (args[i].equals("-d") || args[i].equals("--debug")) {
                Log.logLevel = Log.LOG_DEBUG;
                Utility.getLogger().setLevel(Level.FINEST);
            } else if (args[i].equals("-q") || args[i].equals("--quiet")) {
                Log.logLevel = Log.LOG_NONE;
                Utility.getLogger().setLevel(Level.SEVERE);
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
        if (portname == null) {
            Log.info("Please specify the port.");
            System.exit(0);
        }

        ATConnection device = new ATConnection(portname);
        device.setBaudRate(baudrate);
        device.setFlowControl(flowControl);
        Log.info("connecting to serial " + portname + " " + baudrate);
        try {
            device.setConnMode(ATConnection.MODE_DATA);
            if (ui == null) {
                ui = new InteractiveUserInterface();
                Log.info("starting interactive mode, type 'exit' to exit, 'help' for help.");
            }
            run(device, ui);
        } finally {
            Log.info("disconnecting serial port");
            device.setConnMode(ATConnection.MODE_DISCONNECTED);
        }
    }

    private void run(ATConnection device, UserInterface ui) throws IOException {
        OBEXClient obexClient = new OBEXClient(device);
        Log.info("connecting obex");
        obexClient.connect();
        try {
            String cmdline;
            while ((cmdline = ui.readCommand()) != null) {
                cmdline = cmdline.trim();
                Log.debug(getClass(), "executing command '" + cmdline + "'");
                String tok[] = Utility.split(cmdline);
                if (tok[0].startsWith("#") || tok[0].startsWith("//")) {
                    // do nothing, it's a comment
                } else if (tok[0].startsWith("AT") || cmdline.startsWith("at")) {
                    if (device.getConnMode() != ATConnection.MODE_AT) {
                        obexClient.disconnect();
                        device.setConnMode(ATConnection.MODE_AT);
                    }
                    ui.println(tok[0]);
                    String response = new String(device.send(tok[0].getBytes(), timeout));
                    ui.println(response);
                } else {
                    if (device.getConnMode() != ATConnection.MODE_DATA) {
                        device.setConnMode(ATConnection.MODE_DATA);
                        obexClient.connect();
                    }
                    if (tok[0].equals("cd")) {
                        if (tok.length > 1) {
                            obexClient.changeDirectory(tok[1], false);
                        }
                        ui.setDir(obexClient.getCurrentFolder().getPath());
                    } else if (tok[0].equals("mkdir")) {
                        if (tok.length > 1) {
                            obexClient.changeDirectory(tok[1], true);
                        }
                        ui.setDir(obexClient.getCurrentFolder().getPath());
                    } else if (tok[0].equals("ls") || tok[0].equals("dir")) {
                        ui.println(obexClient.loadFolderListing().getListing());
                    } else if (tok[0].equals("rm") || tok[0].equals("del")) {
                        if (tok.length > 1) {
                            obexClient.removeObject(Utility.nameToBytes(tok[1]));
                        }
                    } else if (tok[0].equals("put")) {
                        if (tok.length > 2) {
                            OBEXFile fileHolder = loadLocalFile(tok[1]);
                            obexClient.removeObject(fileHolder);
                            obexClient.writeFile(fileHolder);
                        }
                    } else if (tok[0].equals("get")) {
                        if (tok.length > 2) {
                            OBEXFile fh = obexClient.readFile(tok[1]);
                            saveLocalFile(fh);
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
                    }
                }
            }
        } finally {
            Log.info("disconnecting obex");
            obexClient.disconnect();
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
        ui.println("  sleep <milliseconds>");
        ui.println("  erasedisk [-y] ");
        ui.println("  help");
        ui.println("  exit");
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
}
