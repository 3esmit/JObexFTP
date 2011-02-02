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

import com.lhf.obexftplib.event.ATEventListener;

public interface UserInterface extends ATEventListener {
    /**
     * Next command or null if no more commands are available.
     */
    public String readCommand();

    /**
     * Prints a message to the user.
     */
    public void println(String message);

    /**
     * Prints a message and a Throwable stack trace to the user.
     */
    public void println(String message, Throwable throwable);

    /**
     * Prints the current command line to the user.
     */
    public void echoCommand(String cmdline);

    public void setDir(String dir);
}
