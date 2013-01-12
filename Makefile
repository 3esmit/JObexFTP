# This Makefile builds jobexftp.jar on Chris Dew's AMD64 Ubuntu 10.04 desktop box.
# It might work for your machine, or you might know Ant inside out and write a good build mechanism
# for us all.
# It is not beautiful or efficient - it builds java files into class files using find :-(

JAVAC=/usr/bin/javac
JOPTS=-d builddir -cp lib/nrjavaserial-3.8.4.jar
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
	$(JAR) cfm $(BINARY) MANIFEST.MF -C builddir com

install:
	if ! test `whoami` = "root" ; then echo "you need root privileges" ; exit 0 ; fi
	mkdir -p /usr/share/jobexftp/lib 
	if test -e $(BINARY) ; then mv $(BINARY) /usr/share/jobexftp/ ; rm /usr/bin/jobexftp ; echo 'java -cp /usr/share/jobexftp/nrjavaserial-3.8.4.jar:/usr/share/jobexftp/jobexftp.jar com.lhf.jobexftp.StandAloneApp "$$@"' > /usr/bin/jobexftp ; chmod +x /usr/bin/jobexftp ; fi
	cp lib/nrjavaserial-3.8.4.jar /usr/share/jobexftp/
	if test -d builddir ; then rm -rf builddir ; fi


#echo "java -Djava.library.path=/usr/share/jobexftp/lib/ -cp /usr/share/jobexftp/RXTXcomm.jar -jar /usr/share/jobexftp/jobexftp.jar \"\$@\""
