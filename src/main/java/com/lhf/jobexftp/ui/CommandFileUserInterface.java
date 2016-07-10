// This file is part of TC65SH.
// 
// TC65SH is free software: you can redistribute it and/or modify
// it under the terms of the GNU Lesser General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
// 
// TC65SH is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
// GNU Lesser General Public License for more details.
// 
// You should have received a copy of the GNU Lesser General Public License
// along with TC65SH. If not, see <http://www.gnu.org/licenses/>.
// 
package com.lhf.jobexftp.ui;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.util.LinkedList;

/**
 * sample command file:
 * 
 * connect <portname> <baudrate>
 * cd fw
 * put D:/temp/device_test.txt test.txt
 * get test.txt device_test.txt
 * disconnect
 *    
 */
public class CommandFileUserInterface implements UserInterface {

    private LinkedList<String> commands = new LinkedList<String>();

    public CommandFileUserInterface(File inputFile) throws IOException {
        LineNumberReader r = null;
        try {
            r = new LineNumberReader(new InputStreamReader(new FileInputStream(inputFile)));
            String line;
            while ((line = r.readLine()) != null) {
                line = line.trim();
                if (line.length() > 0 && !line.startsWith("#") && !line.startsWith("//")) {
                    commands.add(line);
                }
            }
        } finally {
            if (r != null) {
                r.close();
            }
        }
    }

    @Override
    public String readCommand() {
        if (commands.isEmpty()) {
            return null;
        }
        return commands.removeFirst();
    }

    @Override
    public void println(String message) {
        println(message, null);
    }

    @Override
    public void println(String message, Throwable t) {
        System.out.println(message);
        if (t != null) {
            t.printStackTrace(System.out);
        }
    }

    @Override
    public void echoCommand(String cmdline) {
        System.out.println(cmdline);
    }

    public void setDir(String dir) {
//        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void ATEvent(byte[] event) {
    }
}
