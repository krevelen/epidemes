@ECHO OFF
REM $Id$
CLS
CD %~dp0
CALL "java" -jar ./episim-full.jar episim.properties
IF NOT ["%ERRORLEVEL%"]==["0"] PAUSE
