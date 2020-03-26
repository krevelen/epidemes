@ECHO OFF
REM $Id$
CLS
SET startDir=%CD%
CD /d %~dp0\..
ECHO Running EPIDEMES
mvn exec:java -Dexec.mainClass="nl.rivm.cib.epidemes.demo.impl.Main" %*
CD /d %startDir%
IF NOT ["%ERRORLEVEL%"]==["0"] PAUSE
