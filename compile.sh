#!/bin/env bash

echo "----------------------------------------------" 
echo "-   Evolutionary Algorithm compiling utiliy  -" 
echo "----------------------------------------------" 

mvn clean install
cp target/AEProject-0.0.1-SNAPSHOT.jar ae.jar

echo "generated .jar @ current work directory"