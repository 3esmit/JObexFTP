@echo off

REM adjust the following line (and comment out the autodetection 
REM block below) to hardwire your java installation

REM set JAVA_HOME=C:\Program Files\Java\jre7

REM java home autodetection:
set KeyName=HKEY_LOCAL_MACHINE\SOFTWARE\JavaSoft\Java Runtime Environment
set Cmd=reg query "%KeyName%" /s
for /f "tokens=2*" %%i in ('%Cmd% ^| find "CurrentVersion"') do set CurVer=%%j
set Cmd=reg query "%KeyName%\%CurVer%" /s
for /f "tokens=2*" %%i in ('%Cmd% ^| find "JavaHome"') do set JAVA_HOME=%%j


set _CP="%~dp0lib\nrjavaserial-3.8.4.jar"


"%JAVA_HOME%\bin\java.exe" -cp %_CP%;"%~dp0JObexFTP2.jar" com.lhf.jobexftp.StandAloneApp %1 %2 %3 %4 %5 %6 %7 %8 %9
pause

