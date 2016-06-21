#!/bin/bash 

java -jar -Dlog4j2.disable.jmx=true -Dlog4j.configurationFile=./log4j2.yaml -jar ./episim.jar ./eve.yaml &> episim.log &
