#!/bin/bash
# Define paths and parameters

# Check if the correct number of arguments are provided
if [ "$#" -ne 5 ]; then
    echo "Usage: $0 CLIENT_RAM SERVER_RAM PLOT_OUTPUT_PATH IN_PATH OUTPUT_PATH"
    exit 1
fi

CLIENT_RAM="$1"
SERVER_RAM="$2"
PLOT_OUTPUT_PATH="$3"
IN_PATH="$4"
OUTPUT_PATH="$5"

CITIES=("NYC" "CHI")
QUERY_BASE_DIR="client/src/test/"
QUERY_1_PATH="${QUERY_BASE_DIR}run_analysis_q1.py"
QUERY_2_PATH="${QUERY_BASE_DIR}run_analysis_q2.py"
QUERY_3_PATH="${QUERY_BASE_DIR}run_analysis_q3.py"
QUERY_4_PATH="${QUERY_BASE_DIR}run_analysis_q4.py"
QUERY_5_PATH="${QUERY_BASE_DIR}run_analysis_q5.py"
NOTIFIER_PATH="${QUERY_BASE_DIR}notifier.py"

echo    "python3 $QUERY_2_PATH \
    --in_path $IN_PATH \
    --out_path $OUTPUT_PATH \
    --plot_out_path $PLOT_OUTPUT_PATH \
    --gigas_ram $CLIENT_RAM \
    --cities $city"

# Function to start the server
start_server() {
    # Maven compilation commands
    echo "Cleaning..."
    mvn clean &>/dev/null;
    echo "Installing..."
    mvn install &>/dev/null
    echo "Compiling..."
    mvn compile &>/dev/null

    # Decompress the commands' bash executables
    echo "Decompressing files..."
    cd client/target
    tar xvzf tp2-g3-pod-client-1.0-SNAPSHOT-bin.tar.gz &>/dev/null
    cd ../..
    cd server/target
    tar xvzf tp2-g3-pod-server-1.0-SNAPSHOT-bin.tar.gz &>/dev/null

    # Run server
    cd tp2-g3-pod-server-1.0-SNAPSHOT

    echo "Running Server..."
    sh run-server.sh -Xmx${SERVER_RAM}g &>/dev/null & disown;
    SERVER_PID=$!
    cd ../../..
}

# Function to stop the server
stop_server() {
    if [ ! -z "$SERVER_PID" ]; then
        echo "Stopping server with PID $SERVER_PID..."
        kill $SERVER_PID
        wait $SERVER_PID
        echo "Server stopped."
    else
        echo "Server PI D not set, cannot stop the server."
    fi
}

# QUERIES_PATH=($QUERY_1_PATH, $QUERY_2_PATH, $QUERY_3_PATH, $QUERY_4_PATH, $QUERY_5_PATH)
QUERIES_PATH=($QUERY_1_PATH, $QUERY_5_PATH)

for query_path in "${QUERIES_PATH[@]}"
do
    echo "Executing ${query_path}"
    for city in "${CITIES[@]}"
    do
        start_server
        sleep 5
        python3 $query_path \
        --in_path $IN_PATH \
        --out_path $OUTPUT_PATH \
        --plot_out_path $PLOT_OUTPUT_PATH \
        --gigas_ram $CLIENT_RAM \
        --cities $city
        stop_server
    done
done

python3 $NOTIFIER_PATH --subject "Q1 & Q5 Finished Executing" --body "Baby come back" --to joaquingirod@gmail.com

#python3 client/src/test/run_analysis_q2.py     --in_path /home/joaquin/Desktop/pod_data_sets/     --out_path /home/joaquin/Desktop/pod_data_outputs/     --plot_out_path /home/joaquin/Desktop/pod_data_plots/     --gigas_ram 6     --cities NYC