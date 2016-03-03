#!/bin/bash 

/usr/lib/jvm/java-7-openjdk-amd64/jre/bin/java -jar -Dlog4j.configuration=./log4j.properties -jar ./episim.jar ./eve.yaml &> episim.log &
