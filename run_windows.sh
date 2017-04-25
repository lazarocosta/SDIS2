mkdir -p ./bin
javac -d "bin" -cp "lib/*" ./src/gui/*.java
java -cp "bin;lib/*" gui.Login
