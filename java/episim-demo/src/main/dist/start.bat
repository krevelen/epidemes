@ECHO OFF
REM $Id$
CLS
CD /d %~dp0
ECHO Running EPIDEMES DEMO
REM  -Dlog4j.configurationFactory=org.apache.logging.log4j.core.config.yaml.YamlConfigurationFactory ^
CALL "java" -Xmx11500M ^
 -Djava.util.logging.manager=org.apache.logging.log4j.jul.LogManager ^
 -Dlog4j.configurationFile=./log4j2.xml ^
 -jar ./epidemes-demo-full.jar %*
REM IF NOT ["%ERRORLEVEL%"]==["0"] ^
PAUSE
