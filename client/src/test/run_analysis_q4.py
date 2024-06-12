import argparse
from query_lib.query_runner import run_query, read_output_file
from query_lib.data_parser import parse_timestamps, analyze_data, save_results_to_csv
from query_lib.plotter import plot_batch_size_vs_read_time, plot_date_vs_read_time, plot_batch_size_vs_map_reduce_time, plot_date_vs_map_reduce_time
from query_lib.config import cities_config

def parse_args():
    parser = argparse.ArgumentParser(description='Run queries and analyze data.')
    parser.add_argument('--in_path', type=str, required=True, help='Input file path')
    parser.add_argument('--out_path', type=str, required=True, help='Output file path')
    parser.add_argument('--plot_out_path', type=str, required=True, help='Plot output file path')
    parser.add_argument('--gigas_ram', type=str, required=True, help='RAM allocation in gigabytes')
    parser.add_argument('--cities', type=str, nargs='+', choices=['NYC', 'CHI', 'both'], default='both', help='Cities to test: NYC, CHI, or both')
    return parser.parse_args()

def main():
    args = parse_args()

    query_number = "4"
    in_path = args.in_path
    out_path = args.out_path
    plot_out_path = args.plot_out_path
    gigas_ram = args.gigas_ram
    cities = args.cities
    number_of_agencies = None

    if cities == 'both':
        selected_cities = cities_config.keys()
    else:
        selected_cities = cities if isinstance(cities, list) else [cities]

    data = []
    data_2 = []

    # Correct the math for total_iterations
    total_iterations = sum(len(cities_config[city]["line_counts"]) * (len(cities_config[city]["batch_sizes"]) + len(cities_config[city]["date_range"])) for city in selected_cities)
    current_iteration = 0

    for city in selected_cities:
        config = cities_config[city]
        for lines in config["line_counts"]:
            for batch_size in config["batch_sizes"]:
                current_iteration += 1
                print(f"Executing {current_iteration}/{total_iterations}...")
                log_content = run_query(query_number, city, str(lines), str(batch_size), in_path, out_path, gigas_ram, f"time1-{str(current_iteration)}", number_of_agencies, None, None)
                timestamps = parse_timestamps(log_content)
                if timestamps:
                    timestamps['city'] = city
                    timestamps['lines'] = lines
                    timestamps['batch_size'] = batch_size
                    data.append(timestamps)
            for date_range in config["date_range"]:
                current_iteration += 1
                print(f"Executing {current_iteration}/{total_iterations}...")
                log_content = run_query(query_number, city, str(lines), str(batch_size), in_path, out_path, gigas_ram, f"time1-{str(current_iteration)}", number_of_agencies, date_range[0], date_range[1])
                timestamps = parse_timestamps(log_content)
                if timestamps:
                    timestamps['city'] = city
                    timestamps['lines'] = lines
                    timestamps['batch_size'] = batch_size
                    timestamps['date_range'] = f"{date_range[0]}-{date_range[1]}"
                    data_2.append(timestamps)

    parsed_data = analyze_data(data)
    parsed_data_2 = analyze_data(data_2)

    save_results_to_csv(parsed_data, plot_out_path, query_number, cities, "1")
    save_results_to_csv(parsed_data_2, plot_out_path, query_number, cities, "2")

    for city in selected_cities:
        city_df = parsed_data[parsed_data['city'] == city]
        plot_batch_size_vs_read_time(city_df, city, plot_out_path, query_number)
        plot_batch_size_vs_map_reduce_time(city_df, city, plot_out_path, query_number)
        city_df_2 = parsed_data_2[parsed_data_2['city'] == city]
        plot_date_vs_read_time(city_df_2, city, plot_out_path, query_number)
        plot_date_vs_map_reduce_time(city_df_2, city, plot_out_path, query_number)

if __name__ == "__main__":
    main()
