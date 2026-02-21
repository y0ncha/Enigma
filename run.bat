git clone https://github.com/y0ncha/Enigma.git ./enigma

cd enigma

call mvn clean install

cd enigma-console/target

java -jar enigma-machine-server-ex3.jar
