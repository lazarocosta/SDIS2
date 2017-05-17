mkdir -p ./bin
javac -d "bin" -cp "lib/*" ./src/gui/*.java ./src/database/*.java ./src/client/*.java ./src/server/main/*.java ./src/server/protocol/*.java ./src/server/task/commonPeer/*.java ./src/server/task/initiatorPeer/*.java  ./src/utils/*.java
java -cp "bin:lib/*" gui.Login
