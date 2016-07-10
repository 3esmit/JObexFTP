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
# Script for sending and executing aplication in Cinterion modules. 
# Needs: jobexftp (that needs java and JObexFTP.jar) 
# Change the baudrates in this file if your module uses different baudrates then 115200 
#####
# Help improving this script, and DON'T FORGET TO SEND ME YOUR IMPROVMENTS! Thank you!
# Written by Ricardo Guilherme Schmidt (3esmit@gmail.com) 

currentdir=`dirname $(readlink -f $0)`
jobexftpjar="JObexFTP2.jar"
if [ $# -lt 2 ]; then
	echo "No enough arguments."
	echo "Usage is: jobexftpfs.sh [ttypath] [jadpath]"
	exit 0
fi

command -v java > /dev/null
if [[ $? != 0 ]]; then
	echo "Java command not found."
	echo "Please configure you envoirment or enhance this script to auto find java wherever it is."
	exit 0
fi

if [ ! -e  $currentdir/$jobexftpjar ]; then
	echo "$currentdir/$jobexftpjar not found";
	exit 0
fi

ttypath=$1
jadpath=$2
jadfile=`basename $jadpath`
folderpath=`dirname $jadpath`
projectname=${jadfile%%[.]*}
jarfile="$projectname.jar"
jarpath=$folderpath"/"$jarfile

if [ -a $ttypath ]; then
	echo "Killing all process using $ttypath :"
	echo "`fuser -k $ttypath`"
	stty -F $ttypath raw ispeed 115200 ospeed 115200 min 1 time 0 -cstopb -evenp crtscts
	echo "Checking if tc65 is ready :"
	device=`echo -e "AT+CGMM\r" > $ttypath && timeout 1 cat $ttypath | head -n 2 | tail -n 1 | head -c 4`

	if [[ $device == "" ]]; then
		closeobex=`echo -e -n "+++" > $ttypath && sleep 1 && echo -e -n "+++" > $ttypath`
		timeout 1 cat $ttypath
		device=`echo -e "AT+CGMM\r" > $ttypath && timeout 1 cat $ttypath | head -n 2 | tail -n 1 | head -c 4`
	fi

	if [[ $device == "ERRO" ]]; then
		echo "Trying to sysstart device"
		echo -e -n "at^scfg=meopmode/airplane,off\r" > /dev/ttyACM0
		sleep 3
		timeout 1 cat $ttypath
		device=`echo -e "AT+CGMM\r" > $ttypath && timeout 1 cat $ttypath | head -n 2 | tail -n 1 | head -c 4`
	fi

	if [[ $device != "TC65" ]]; then
		echo " Failed to find a ready device. "
		exit 0
	fi
else
	echo " Failed to find a device ready for communication. "
	exit 0
fi
timeout 1 cat $ttypath
echo " Device $device at $ttypath "
echo " JAD $jadpath " 
echo " JAR $jarpath "
echo " Sending files with `jobexftp -v`:"
echo "`fuser -k $ttypath`"
bash $currentdir/jobexftp -p $ttypath -c "put $jarpath; put $jadpath; run $jarfile" 

