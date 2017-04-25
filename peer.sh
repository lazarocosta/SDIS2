#start client
echo "Starting peer..."
java -classpath ./bin server.main.Peer 1.0 1 peer1 224.0.0.1 9001 224.0.0.2 9002 224.0.0.3 9003
