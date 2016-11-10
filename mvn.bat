@ECHO OFF
SET JAVA_HOME=R:\EPI\MOD\SoftwareHardware\JDK8
SET MAVEN_HOME=R:\EPI\MOD\SoftwareHardware\Maven2
SET PROJECT_HOME=R:\EPI\MOD\Projects\S_113010_EPIDEMES\WorkInProgress\epidemes\java
%MAVEN_HOME%\bin\mvn -Dmaven.test.skip=true -f %PROJECT_HOME% %*