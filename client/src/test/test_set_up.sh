# !/bin/bash

# Maven compilation commands
mvn clean
mvn install
mvn compile

# Decompress the commands' bash executables
cd client/target
tar xvzf tp2-g3-pod-client-1.0-SNAPSHOT-bin.tar.gz
cd ../..
cd server/target
tar xvzf tp2-g3-pod-server-1.0-SNAPSHOT-bin.tar.gz

# Run server
cd tp2-g3-pod-server-1.0-SNAPSHOT
sh run-server.sh
