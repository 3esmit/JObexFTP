# This Makefile builds jobexftp.jar on Chris Dew's AMD64 Ubuntu 10.04 desktop box.
# It might work for your machine, or you might know Ant inside out and write a good build mechanism
# for us all.
# It is not beautiful or efficient - it builds java files into class files using find :-(

JAVAC=/usr/bin/javac
JOPTS=-d builddir
JAVA=/usr/bin/java
BINARY=jobexftp.jar
JAR=jar
all: clean $(BINARY)

clean:
	# cleaning
	if test -f $(BINARY) ; then rm $(BINARY) ; fi
	if test -d builddir ; then rm -rf builddir ; fi
	
builddir: 
	# compiling...
	if ! test -d builddir ; then mkdir builddir ; fi
	$(JAVAC) $(JOPTS) `find src -name "*.java"`

$(BINARY): builddir
	# packaging $(BINARY)
	$(JAR) cf $(BINARY) -m MANIFEST.MF -C builddir com

install:
	if ! test `whoami` = "root" ; then echo "you need root privileges" ; exit 0 ; fi
	mkdir -p /usr/share/jobexftp/lib 
	if test -e $(BINARY) ; then mv $(BINARY) /usr/share/jobexftp/ ; rm /usr/bin/jobexftp ; echo "java -Djava.library.path=/usr/share/jobexftp/lib/ -cp /usr/share/jobexftp/RXTXcomm.jar -jar /usr/share/jobexftp/jobexftp.jar \"\$@\"" > /usr/bin/jobexftp ; chmod +x /usr/bin/jobexftp ; fi
	if test `uname -m` = "x86_64" ; then cp lib/x86_64/lib* /usr/share/jobexftp/lib/ ; cp lib/x86_64/RXTXcomm.jar /usr/share/jobexftp/ ; else cp lib/i386/lib* /usr/share/jobexftp/lib/ ; cp lib/i386/RXTXcomm.jar /usr/share/jobexftp/ ; fi
	if test -d builddir ; then rm -rf builddir ; fi
