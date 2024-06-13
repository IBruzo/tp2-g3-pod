# !/bin/bash

SERVER_RAM=$1
INTERFACE=$2

if [ "$#" -ne 2 ]; then
    echo "Usage: $0 SERVER_RAM INTERFACE"
    exit 1
fi

# Maven
mvn clean
mvn install
mvn compile

# Decompress
cd client/target
tar xvzf tp2-g3-pod-client-1.0-SNAPSHOT-bin.tar.gz
cd ../..
cd server/target
tar xvzf tp2-g3-pod-server-1.0-SNAPSHOT-bin.tar.gz

# Run server
cd tp2-g3-pod-server-1.0-SNAPSHOT
sh run-server.sh -Xmx${SERVER_RAM}g -Dinterface=${INTERFACE}
