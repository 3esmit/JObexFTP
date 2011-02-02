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

import com.lhf.obexftplib.etc.Utility;

public class InteractiveUserInterface implements UserInterface {

    private String dir = "";

    @Override
    public String readCommand() {
        if (!"$".equals(dir)) {
            System.out.print(dir + ">");
        }
        String command = Utility.readStandardInput();
        if ("exit".equals(command)) {
            command = null;
        }
        return command;
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
    }

    public void setDir(String dir) {
        this.dir = dir;
    }

    public void ATEvent(byte[] event) {
        System.out.print(new String(event));
    }
}
