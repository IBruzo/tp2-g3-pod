import subprocess
import os
import re
import pandas as pd
import matplotlib.pyplot as plt
import smtplib
from email.mime.text import MIMEText
from email.mime.multipart import MIMEMultipart
import os

# Issue with identical values, when

def run_query(query_number, city, limit, batch_size, in_path, out_path, gigas_ram, output_file_name):
    working_directory = "client/target/tp2-g3-pod-client-1.0-SNAPSHOT"
    command = (
        f"sh run-query{query_number}.sh"
        f" -Xmx{gigas_ram}g"
        f" -Dcity={city}"
        f" -Dlimit={limit}"
        f" -DbatchSize={batch_size}"
        f" -DinPath={in_path}"
        f" -DoutPath={out_path}"
        f" -DoutputFileName={output_file_name}"
    )
    print(run_command(command, working_directory))
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

def send_email_notification(subject, body, to_email):
    from_email = "joaquingirodnotifier@gmail.com"
    from_password = "vtre ncgw lddl drkd "

    msg = MIMEMultipart()
    msg['From'] = from_email
    msg['To'] = to_email
    msg['Subject'] = subject

    msg.attach(MIMEText(body, 'plain'))

    try:
        server = smtplib.SMTP('smtp.gmail.com', 587)
        server.starttls()
        server.login(from_email, from_password)
        text = msg.as_string()
        server.sendmail(from_email, to_email, text)
        server.quit()
        print("Email sent successfully!")
    except Exception as e:
        print(f"Failed to send email: {e}")

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

def plot_batch_size_evolution(df, city, output_dir):
    plt.figure(figsize=(8, 6))
    for batch_size in df['batch_size'].unique():
        batch_df = df[df['batch_size'] == batch_size]
        if not batch_df.empty:
            plt.plot(batch_df['lines'].values, batch_df['read_time'].values, marker='o', label=f'Batch Size: {batch_size}')
            for x, y in zip(batch_df['lines'].values, batch_df['read_time'].values):
                plt.text(x, y, f'({x}, {y:.2f})', fontsize=8, ha='right')
    plt.title(f'Evolution of Batch Size - {city}')
    plt.xlabel('Inputted Lines')
    plt.ylabel('Read Time (s)')
    plt.legend()
    plt.grid(True)
    plt.tight_layout()
    plt.savefig(os.path.join(output_dir, f'{city}_batch_size_evolution.png'))

def plot_lines_evolution(df, city, output_dir):
    plt.figure(figsize=(8, 6))
    for lines in df['lines'].unique():
        lines_df = df[df['lines'] == lines]
        if not lines_df.empty:
            plt.plot(lines_df['batch_size'].values, lines_df['read_time'].values, marker='o', label=f'Inputted Lines: {lines}')
            for x, y in zip(lines_df['batch_size'].values, lines_df['read_time'].values):
                plt.text(x, y, f'({x}, {y:.2f})', fontsize=8, ha='right')
    plt.title(f'Evolution of Inputted Lines - {city}')
    plt.xlabel('Batch Size')
    plt.ylabel('Read Time (s)')
    plt.legend()
    plt.grid(True)
    plt.tight_layout()
    plt.savefig(os.path.join(output_dir, f'{city}_lines_evolution.png'))

def main():
    query_number = "1"
    in_path = "/home/joaquin/Desktop/pod_data_sets/"
    out_path = "/home/joaquin/Desktop/pod_data_outputs/"
    output_dir = "/home/joaquin/Desktop/pod_data_plots/"
    gigas_ram = "6"
    # cities_config = {
    #     "NYC": {
    #         "line_counts": [400000],
    #         "batch_sizes": [50000]
    #     },
    #     "CHI": {
    #         "line_counts": [400000],
    #         "batch_sizes": [50000]
    #     }
    # }
    cities_config = {
        "NYC": {
            "line_counts": [800000, 1600000, 3200000, 6400000, 12800000],
            "batch_sizes": [50000, 100000, 200000, 400000, 800000]
        },
        "CHI": {
            "line_counts": [400000, 800000, 1600000, 3200000, 4800000],
            "batch_sizes": [50000, 100000, 200000, 400000]
        }
    }

    data = []

    total_iterations = sum(len(config["line_counts"]) * len(config["batch_sizes"]) for config in cities_config.values())
    current_iteration = 0

    # For each city, for each of the line counts specified, and for each of the batch size specified, the query is run
    for city, config in cities_config.items():
        for lines in config["line_counts"]:
            for batch_size in config["batch_sizes"]:
                current_iteration += 1
                print(f"Executing {current_iteration}/{total_iterations}...")
                log_content = run_query(query_number, city, str(lines), str(batch_size), in_path, out_path, gigas_ram, f"time1-{str(current_iteration)}")
                # Timestamp holds a dictionary with the right value association
                # {'start_read': '09/06/2024 17:41:19:7100',
                # 'end_read': '09/06/2024 17:41:31:0670',
                # 'start_mapreduce': '09/06/2024 17:41:31:0754',
                # 'end_mapreduce': '09/06/2024 17:41:40:7831'}
                timestamps = parse_timestamps(log_content)
                # Add the rest of the necessary information to the tuple
                # DATA [{
                # 'start_read': '09/06/2024 19:51:06:0541',
                # 'end_read': '09/06/2024 19:51:06:7562',
                # 'start_mapreduce': '09/06/2024 19:51:06:7698',
                # 'end_mapreduce': '09/06/2024 19:51:07:1164',
                # 'city': 'NYC', 'lines': 100000, 'batch_size': 25000
                # }, {
                # 'start_read': '09/06/2024 19:51:07:5772',
                # 'end_read': '09/06/2024 19:51:08:3132',
                # 'start_mapreduce': '09/06/2024 19:51:08:3245',
                # 'end_mapreduce': '09/06/2024 19:51:08:7254',
                # 'city': 'CHI',
                # 'lines': 100000,
                # 'batch_size': 25000
                # }]
                if timestamps:
                    timestamps['city'] = city
                    timestamps['lines'] = lines
                    timestamps['batch_size'] = batch_size
                    data.append(timestamps)

    # Parse the string values for time into the time difference
    parsed_data = analyze_data(data)

    # print(parsed_data) for debuggin purposes

    for city in cities_config.keys():
        # Grab the values values corresponding to each city from the data outputs
        city_df = parsed_data[parsed_data['city'] == city]

        # Plot the evolution of each batch size when varying the inputted lines
        # If there are 5 batch sizes in the array then there should be 5 evolutions, which represent the joining of the points that
        # result of runnning for (BatchSize1, InputtedLines1), (BatchSize1, InputtedLines2), (BatchSize1, InputtedLines3)...
        plot_batch_size_evolution(city_df, city, output_dir)

        # Plot the evolution of each quantity of inputted lines when varying the batch size
        # If there are 5 elements in the inputted lines array then there should be 5 evolutions, which represent the joining of the points that
        # result of runnning for (BatchSize1, InputtedLines1), (BatchSize2, InputtedLines1), (BatchSize3, InputtedLines1)...
        plot_lines_evolution(city_df, city, output_dir)

    send_email_notification(
    subject="Script Execution Complete",
    body="The script has finished running.",
    to_email="joaquingirod@gmail.com"
    )




if __name__ == "__main__":
    main()
