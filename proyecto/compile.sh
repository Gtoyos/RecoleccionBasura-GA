#!/bin/env bash

export JAVA_HOME="~/lejava"
~/lemaven/bin/mvn clean install
cp target/AEProject-0.0.1-SNAPSHOT.jar ae.jar