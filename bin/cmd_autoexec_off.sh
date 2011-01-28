#!/bin/bash
# This file is part of JObexFTP utils <http://www.github.com/3esmit/jobexftp>.
#
# JObexFTP is free software: you can redistribute it and/or modify
# it under the terms of the GNU Lesser General Public License as published by
# the Free Software Foundation, either version 3 of the License, or
# (at your option) any later version.
#
# JObexFTP is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU Lesser General Public License for more details.
# 
# You should have received a copy of the GNU Lesser General Public License
# along with JObexFTP.  If not, see <http://www.gnu.org/licenses/>.
#####
# Script for disable autostart in TC65
#####
# Help improving this script, and DON'T FORGET TO SEND ME YOUR IMPROVMENTS! Thank you!
#
# Written by Ricardo Guilherme Schmidt (3esmit@gmail.com) 

if [ $# -lt 1 ]; then
	echo "No enough arguments. usage: cmd_autoexec_off.sh [ttypath] <password>"
	exit 0
fi
ttypath=$1
password=$2

if [ ! -e $ttypath ]; then
	echo "Device must be plugged in"
	exit 0
fi

echo "Turn off device now"

while [ -e $ttypath ]
do
	cat $ttypath
	sleep 0.1
done

echo "Device turned off, waiting for it to come back"

while [ ! -e $ttypath ]
do
	sleep 0.01
done

sleep 0.3

cleared=`cat $ttypath & fuser -k $ttypath`

echo "Sending command"
echo -e "AT^SCFG=\"UserWare/Autostart\",\"$password\",0\r" > $ttypath 
timeout 3 cat $ttypath
