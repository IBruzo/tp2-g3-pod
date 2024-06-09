import subprocess
import argparse


### What am i to do?

# Use bash command for query 1 with extra flags to run many executions of the query
# after each execution it should parse the output into some kind of graph

# result = subprocess.run(["dir"], shell=True, capture_output=True, text=True)

# TO DO
#   run query 1, store in x file, parse x file, store values
#   alter properties so the amount of lines read from the csv can be stored

# python3 ../../../client/src/test/query_1_tester.py

def main():

    run_query_1()

    # decompress_file(RELATIVE_PATH_TO_COMPRESSED_COMMANDS, RELATIVE_PATH_TO_UNCOMPRESSED_COMMANDS)

    # run_query_1()

    # cd client/target
    # tar xvzf client/target/tp2-g3-pod-client-1.0-SNAPSHOT-bin.tar.gz

    # run_maven_clean()





def run_query_1():
    run_command("sh run-query1.sh")

def run_command(command):
    print_command_execution(command)
    subprocess.run([command], shell=True)

def print_command_execution(command):
    print(f"""

          Executing '{command}'...

          """)

if __name__ == "__main__":
    main()