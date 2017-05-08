mkdir -p ./bin
javac -d "bin" -cp "lib/*" ./src/gui/*.java ./src/database/*.java
java -cp "bin:lib/*" gui.Login
