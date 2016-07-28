@ECHO OFF
REM $Id$
CLS
CD /d %~dp0
ECHO Running EPISIM with settings:>CON
ECHO.>CON
MORE eve.yaml >CON
CALL "java" -Dlog4j2.disable.jmx=true -Dlog4j.configurationFile=file:/%~dp0/log4j2.yaml -jar ./episim-mas.jar ./eve.yaml 1>episim.log 2>&1
IF NOT ["%ERRORLEVEL%"]==["0"] PAUSE
