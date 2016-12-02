@ECHO OFF
REM $Id$
CLS
CD /d %~dp0
ECHO Running EPISIM
CALL "java" -Djava.util.logging.manager=org.apache.logging.log4j.jul.LogManager -jar ./epidemes-mas-full.jar %1
IF NOT ["%ERRORLEVEL%"]==["0"] PAUSE
