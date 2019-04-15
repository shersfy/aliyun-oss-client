@echo off
rem author: pengyang@xmi01.com
rem date: 2019-09-01
cd %~dp0
cd ..

rem set title
set appname=Aliyun OSS Client
set /p dhversion= < version/version
title %appname%-%dhversion%

set javahome=%JAVA_HOME%
if "%javahome%"=="" (
	echo error: JAVA_HOME not exist  
	pause
)
echo JAVA_HOME=%javahome%

rem read java version
if not exist "%javahome%/bin/java.exe" (
    echo error: %javahome%/bin/java.exe not exist  
    pause
)
if not exist logs (
   md logs
)

"%javahome%/bin/java" -version 2>version/java.version
set /p jversion= < version/java.version
echo JAVA_VERSION=%jversion%

set v1=1.8
set v2=%jversion:~14,3%

if not %v1% equ %v2% (
	echo error: %appname% rely on JRE version least is 1.8
	pause
)

echo Welcome to %appname%
echo %DATE:~0,10% %TIME:~0,8% %appname% is running...
"%javahome%/bin/java" -cp config/;lib/*;./ org.shersfy.oss.boot.Application