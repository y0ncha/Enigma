#!/bin/bash

git clone https://github.com/y0ncha/Enigma.git ./enigma

cd enigma

mvn clean install

cd enigma-console/target

java -jar enigma-machine-ex2.jar

