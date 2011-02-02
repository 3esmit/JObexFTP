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

import com.lhf.obexftplib.etc.Log;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TelnetUserInterface implements UserInterface, Runnable {

    private int port;
    private Socket clientSocket;
    private boolean mustShutdown = false;

    public TelnetUserInterface(int port) throws IOException {
        super();
        this.port = port;

        Thread thread = new Thread(this);
        thread.setDaemon(true);
        thread.start();
    }

    @Override
    public void run() {
        try {
            ServerSocket serverSocket = new ServerSocket(port);
            Log.info("waiting for telnet connection");
            while (!mustShutdown) {
                Socket socket = serverSocket.accept();
                Log.info("new telnet connection from " + socket.getRemoteSocketAddress().toString());
                if (clientSocket == null) {
                    clientSocket = socket;
                } else {
                    socket.getOutputStream().write("another user is currently logged in, please try again later\r\n".getBytes());
                    safeClose(socket);
                }
            }
            serverSocket.close();
        } catch (Throwable t) {
            Log.info("unexpected error in run()", t);
        }
    }

    @Override
    public String readCommand() {
        while (clientSocket == null) {
            sleep(500);
        }
        String command = null;
        try {
            clientSocket.getOutputStream().write("tc65sh>".getBytes());
            StringBuilder buf = new StringBuilder();
            while (command == null) {
                int c = clientSocket.getInputStream().read();
                if (c != 13) {
                    if (c == 10) {
                        command = buf.toString();
                    } else {
                        buf.append((char) c);
                    }
                }
                if (c == -1) {
                    return null;
                }
            }
            if (command.trim().startsWith("exit")) {
                safeClose(clientSocket);
                clientSocket = null;
                command = null;
            }
        } catch (IOException e) {
            Log.info("client connection closed", e);
            safeClose(clientSocket);
            clientSocket = null;
        }
        return command;
    }

    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException ex) {
            // ignore
        }
    }

    @Override
    public void println(String message) {
        println(message, null);
    }

    @Override
    public void println(String message, Throwable t) {
        if (clientSocket == null) {
            return;
        }
        try {
            OutputStream out = clientSocket.getOutputStream();
            out.write((message + "\r\n").getBytes());
            if (t != null) {
                PrintWriter pw = new PrintWriter(out);
                t.printStackTrace(pw);
            }
            out.flush();
        } catch (Exception e) {
            Log.debug(getClass(), "", e);
        }
    }

    private void safeClose(Socket sock) {
        try {
            if (sock != null) {
                sock.close();
                sock = null;
            }
        } catch (Exception e) {
            Log.debug(getClass(), "cannot close a socket", e);
        }
    }

    @Override
    public void echoCommand(String cmdline) {
        System.out.println(cmdline);
    }

    public void setDir(String dir) {
    }
    public void ATEvent(byte[] event) {
        try {
            clientSocket.getOutputStream().write(event);
        } catch (IOException ex) {
            Logger.getLogger(TelnetUserInterface.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
