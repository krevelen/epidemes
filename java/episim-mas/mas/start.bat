@ECHO OFF
REM $Id: 26d479094f7ba34c44fb1c1f50995e90d8b76b3b $
CLS
CD /d %~dp0
ECHO Running EPISIM
CALL "java" -jar ./episim-mas.jar %1
IF NOT ["%ERRORLEVEL%"]==["0"] PAUSE
