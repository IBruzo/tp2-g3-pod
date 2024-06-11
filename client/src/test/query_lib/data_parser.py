import re
import pandas as pd
import os

def parse_timestamps(log_content):
    timestamps = {}
    patterns = {
        "start_read": r"(\d{2}/\d{2}/\d{4} \d{2}:\d{2}:\d{2}:\d{4}) - Inicio de la lectura del archivo",
        "end_read": r"(\d{2}/\d{2}/\d{4} \d{2}:\d{2}:\d{2}:\d{4}) - Fin de la lectura del archivo",
        "start_mapreduce": r"(\d{2}/\d{2}/\d{4} \d{2}:\d{2}:\d{2}:\d{4}) - Inicio del trabajo map/reduce",
        "end_mapreduce": r"(\d{2}/\d{2}/\d{4} \d{2}:\d{2}:\d{2}:\d{4}) - Fin del trabajo map/reduce"
    }

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

def save_results_to_csv(df, output_dir, query, city):
    output_file = os.path.join(output_dir, f"results-{query}{city}.csv")
    df.to_csv(output_file, index=False)
    print(f"Results saved to {output_file}")
