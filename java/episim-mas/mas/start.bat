@ECHO OFF
REM $Id$
CLS
CD /d %~dp0
ECHO Running EPISIM
CALL "java" -jar ./episim-mas.jar %1
IF NOT ["%ERRORLEVEL%"]==["0"] PAUSE
