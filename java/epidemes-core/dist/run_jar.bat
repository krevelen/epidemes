@ECHO OFF
REM $Id$
CLS
SET startDir=%CD%
CD /d %~dp0
ECHO Running EPIDEMES -conf=%~dp0
CALL "java"  -Dconfig.base=./ -jar epidemes-full.jar %*
CD /d %startDir%
IF NOT ["%ERRORLEVEL%"]==["0"] PAUSE
