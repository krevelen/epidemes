@ECHO OFF
REM $Id$
CLS
SET startDir=%CD%
CD /d %~dp0
ECHO Running EPIDEMES MAS
CALL "java" -Xmx11500M -Dlog4j.configurationFile=./log4j2.yaml ^
	-Djava.util.logging.manager=org.apache.logging.log4j.jul.LogManager ^
	-jar ./epidemes-mas-full.jar %*
CD /d %startDir%
IF NOT ["%ERRORLEVEL%"]==["0"] PAUSE
