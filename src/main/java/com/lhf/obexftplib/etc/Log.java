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
package com.lhf.obexftplib.etc;

public abstract class Log {

    public static int LOG_NONE = 0;
    public static int LOG_INFO = 1;
    public static int LOG_DEBUG = 2;
    public static int logLevel = LOG_INFO;

    public static void info(String msg) {
        info(msg, null);
    }

    public static void info(String msg, Throwable t) {
        if (logLevel >= LOG_INFO) {
            System.out.print(msg);
            if (t != null) {
                if (t.getMessage() != null) {
                    System.out.print(t.getMessage());
                } else if (t.getLocalizedMessage() != null){
                    System.out.print(t.getLocalizedMessage());
                }else{
                    t.printStackTrace();
                }
            }
        }
        System.out.println();
    }

    public static void debug(Class<?> klass, String msg) {
        debug(klass, msg, null);
    }

    public static void debug(Class<?> klass, String msg, Throwable t) {
        if (logLevel >= LOG_DEBUG) {
            System.out.println("[" + klass.getName() + "]" + msg);
            if (t != null) {
                t.printStackTrace();
            }
        }
    }
}
