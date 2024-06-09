# !/bin/bash

# Maven compilation commands
mvn clean
mvn install
mvn compile

# Decompress the commands' bash executables
cd client/target
tar xvzf tp2-g3-pod-client-1.0-SNAPSHOT-bin.tar.gz

# cd client/target