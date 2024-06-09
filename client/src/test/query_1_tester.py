import subprocess
import os
import re
import pandas as pd
import matplotlib.pyplot as plt

# Issue with identical values, when

def run_query(query_number, city, limit, batch_size, in_path, out_path, gigas_ram):
    working_directory = "client/target/tp2-g3-pod-client-1.0-SNAPSHOT"
    command = (
        f"sh run-query{query_number}.sh"
        # f"-Xmx{gigas_ram}g"
        f" -Dcity={city}"
        f" -Dlimit={limit}"
        f" -DbatchSize={batch_size}"
        f" -DinPath={in_path}"
        f" -DoutPath={out_path}"
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
        # Check if the directory exists
        if not os.path.isdir(working_directory):
            raise FileNotFoundError(f"The directory '{working_directory}' does not exist.")

        os.chdir(working_directory)
        # Useful for debugging, stdout of script is redirected to this file´s stdout
        # result = subprocess.run([command], shell=True)
        result = subprocess.run([command], shell=True, capture_output=True, text=True)
        return result.stdout
    finally:
        os.chdir(original_directory)

def print_command_execution(command, working_directory):
    print(f"""
          Executing '{command}' in directory '{working_directory}'...
          """)

####################

def parse_timestamps(log_content):
    timestamps = {}
    # Patterns for each line of the output, kinda ugly regex
    patterns = {
        "start_read": r"(\d{2}/\d{2}/\d{4} \d{2}:\d{2}:\d{2}:\d{4}) - Inicio de la lectura del archivo",
        "end_read": r"(\d{2}/\d{2}/\d{4} \d{2}:\d{2}:\d{2}:\d{4}) - Fin de la lectura del archivo",
        "start_mapreduce": r"(\d{2}/\d{2}/\d{4} \d{2}:\d{2}:\d{2}:\d{4}) - Inicio del trabajo map/reduce",
        "end_mapreduce": r"(\d{2}/\d{2}/\d{4} \d{2}:\d{2}:\d{2}:\d{4}) - Fin del trabajo map/reduce"
    }

    # Search for the regex and store the value
    for key, pattern in patterns.items():
        match = re.search(pattern, log_content)
        if match:
            timestamps[key] = match.group(1)

    return timestamps

def analyze_data(data):
    df = pd.DataFrame(data)
    df['start_read'] = pd.to_datetime(df['start_read'], format='%d/%m/%Y %H:%M:%S:%f')
    df['end_read'] = pd.to_datetime(df['end_read'], format='%d/%m/%Y %H:%M:%S:%f')
    df['start_mapreduce'] = pd.to_datetime(df['start_mapreduce'], format='%d/%m/%Y %H:%M:%S:%f')
    df['end_mapreduce'] = pd.to_datetime(df['end_mapreduce'], format='%d/%m/%Y %H:%M:%S:%f')

    df['read_time'] = (df['end_read'] - df['start_read']).dt.total_seconds()
    df['mapreduce_time'] = (df['end_mapreduce'] - df['start_mapreduce']).dt.total_seconds()

    return df

def plot_results(df, parameter, city, output_dir):
    plt.figure(figsize=(12, 6))

    plt.subplot(1, 2, 1)
    plt.plot(df[parameter].values, df['read_time'].values, marker='o', label='Read Time')
    plt.title(f'Read Time vs {parameter} ({city})')
    plt.xlabel(parameter)
    plt.ylabel('Time (s)')
    plt.legend()

    plt.subplot(1, 2, 2)
    plt.plot(df[parameter].values, df['mapreduce_time'].values, marker='o', label='MapReduce Time')
    plt.title(f'MapReduce Time vs {parameter} ({city})')
    plt.xlabel(parameter)
    plt.ylabel('Time (s)')
    plt.legend()

    plt.tight_layout()

    # Save the plot
    filename = f"{city}_{parameter}_plot.png"
    filepath = os.path.join(output_dir, filename)
    plt.savefig(filepath)


def main():
    query_number = "1"
    in_path = "/home/joaquin/Desktop/pod_data_sets/"
    out_path = "/home/joaquin/Desktop/pod_data_outputs/"
    output_dir = "/home/joaquin/Desktop/pod_data_plots/"
    gigas_ram = "1"
    cities = ["NY", "CHI"]
    # Quick Test
    line_counts = [100000]
    batch_sizes = [25000]
    # Not So Quick Test
    # line_counts = [100000, 200000, 400000, 800000, 1600000, 3200000]
    # batch_sizes = [25000, 50000, 100000]
    # Full Test
    # line_counts = [100000, 200000, 400000, 800000, 1600000, 3200000] # Change these
    # batch_sizes = [1000, 10000, 25000, 50000, 100000]       # Change these

    data = []

    total_iterations = len(cities) * len(line_counts) * len(batch_sizes)
    current_iteration = 0

    # For each city, for each of the line counts specified, and for each of the batch size specified, the query is run
    for city in cities:
        for lines in line_counts:
            for batch_size in batch_sizes:
                current_iteration += 1
                print(f"Executing {current_iteration}/{total_iterations}...")
                log_content = run_query(query_number, city, str(lines), str(batch_size), in_path, out_path, gigas_ram)
                # Timestamp holds a dictionary with the right value association
                # {'start_read': '09/06/2024 17:41:19:7100',
                # 'end_read': '09/06/2024 17:41:31:0670',
                # 'start_mapreduce': '09/06/2024 17:41:31:0754',
                # 'end_mapreduce': '09/06/2024 17:41:40:7831'}
                timestamps = parse_timestamps(log_content)
                # Add the rest of the necessary information to the tuple
                #[{'start_read': '09/06/2024 17:41:19:7100',
                # 'end_read': '09/06/2024 17:41:31:0670',
                # 'start_mapreduce': '09/06/2024 17:41:31:0754',
                # 'end_mapreduce': '09/06/2024 17:41:40:7831',
                # 'city': 'NY',
                # 'lines': 100000,
                # 'batch_size': 25000},
                # {'start_read': '09/06/2024 17:41:19:7100',
                # 'end_read': '09/06/2024 17:41:31:0670',
                # 'start_mapreduce': '09/06/2024 17:41:31:0754', 'end_mapreduce': '09/06/2024 17:41:40:7831', 'city': 'CHI', 'lines': 100000, 'batch_size': 25000}]
                if timestamps:
                    timestamps['city'] = city
                    timestamps['lines'] = lines
                    timestamps['batch_size'] = batch_size
                    data.append(timestamps)
                print(timestamps)

    df = analyze_data(data)

    for city in cities:
        city_df = df[df['city'] == city]
        plot_results(city_df, 'lines', city, output_dir)
        plot_results(city_df, 'batch_size', city, output_dir)

if __name__ == "__main__":
    main()
