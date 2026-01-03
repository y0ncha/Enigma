#!/bin/bash

git clone https://github.com/y0ncha/Enigma.git ./enigma

cd enigma

mvn clean install

cd target

java -jar enigma-machine-ex2.jarrun.sh