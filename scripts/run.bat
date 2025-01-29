@echo off

cd %~dp0

set CLASSPATH=.;lib\*

echo Running the application...
java -cp "%CLASSPATH%" com.quentity.Application
