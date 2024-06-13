import subprocess
import os

def run_query(query_number, city, limit, batch_size, in_path, out_path, gigas_ram, output_file_name, number_of_agencies, date_from, date_to, addresses):
    working_directory = "client/target/tp2-g3-pod-client-1.0-SNAPSHOT"
    if number_of_agencies != None:
        command = (
            f"sh query{query_number}.sh"
            f" -Xmx{gigas_ram}g"
            f" -Dcity={city}"
            f" -Dlimit={limit}"
            f" -DbatchSize={batch_size}"
            f" -DinPath={in_path}"
            f" -DoutPath={out_path}"
            f" -DoutputFileName={output_file_name}"
            f" -Dn={number_of_agencies}"
            f" -Daddresses={addresses}"
        )
    if date_to != None:
        command = (
            f"sh query{query_number}.sh"
            f" -Xmx{gigas_ram}g"
            f" -Dcity={city}"
            f" -Dlimit={limit}"
            f" -DbatchSize={batch_size}"
            f" -DinPath={in_path}"
            f" -DoutPath={out_path}"
            f" -DoutputFileName={output_file_name}"
            f" -Dfrom={date_from}"
            f" -Dto={date_to}"
            f" -Daddresses={addresses}"
        )
    command = (
        f"sh query{query_number}.sh"
        f" -Xmx{gigas_ram}g"
        f" -Dcity={city}"
        f" -Dlimit={limit}"
        f" -DbatchSize={batch_size}"
        f" -DinPath={in_path}"
        f" -DoutPath={out_path}"
        f" -DoutputFileName={output_file_name}"
        f" -Daddresses={addresses}"
    )
    run_command(command, working_directory)
    return read_output_file(out_path, query_number)

def read_output_file(out_path, query_number):
    output_file = os.path.join(out_path, f"time{query_number}.txt")
    with open(output_file, 'r') as file:
        log_content = file.read()
    return log_content

def run_command(command, working_directory):
    print_command_execution(command, working_directory)
    original_directory = os.getcwd()
    try:
        if not os.path.isdir(working_directory):
            raise FileNotFoundError(f"The directory '{working_directory}' does not exist.")

        os.chdir(working_directory)
        result = subprocess.run([command], shell=True, capture_output=True, text=True)
        return result.stdout
    finally:
        os.chdir(original_directory)

def print_command_execution(command, working_directory):
    print(f"Executing '{command}' in directory '{working_directory}'...")
