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
	$(JAR) cfe $(BINARY) com.lhf.jobexftp.StandAloneApp -C builddir com
