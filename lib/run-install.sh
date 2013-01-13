#!/bin/sh

# check that we're root
if [ $(whoami) != "root" ]
then
  echo "You must run this script as root. Try running 'su -'"
  exit 1
fi

if [ -z "${JAVA_HOME}" ]
then
  echo "Your JAVA_HOME environment variable must be set"
  exit 1
fi

echo "Installing nrjavaserial Build to JAVA_HOME=${JAVA_HOME}"

cp nrjavaserial-3.8.4.jar $JAVA_HOME/jre/lib/ext/
if [ "$?" -ne 0 ]; then echo "Copy failed"; exit 1; fi 

echo "nrjavaserial Build Installed"
