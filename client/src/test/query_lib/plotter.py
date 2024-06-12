import os
import matplotlib.pyplot as plt

def plot_batch_size_vs_read_time(df, city, output_dir, query_number):
    plt.figure(figsize=(8, 6))
    for lines in df['lines'].unique():
        lines_df = df[df['lines'] == lines]
        if not lines_df.empty:
            plt.plot(lines_df['batch_size'].values, lines_df['read_time'].values, marker='o', label=f'Inputted Lines: {lines}')
            # for x, y in zip(lines_df['batch_size'].values, lines_df['read_time'].values):
            #     plt.text(x, y, f'({x}, {y:.2f})', fontsize=8, ha='right')
    plt.title(f'Evolution of Inputted Lines - {city}')
    plt.xlabel('Batch Size')
    plt.ylabel('Read Time (s)')
    plt.legend()
    plt.grid(True)
    plt.tight_layout()
    plt.savefig(os.path.join(output_dir, f'{city}_{query_number}_plot_batch_size_vs_read_time.png'))

def plot_batch_size_vs_map_reduce_time(df, city, output_dir, query_number):
    plt.figure(figsize=(8, 6))
    for lines in df['lines'].unique():
        lines_df = df[df['lines'] == lines]
        if not lines_df.empty:
            plt.plot(lines_df['batch_size'].values, lines_df['mapreduce_time'].values, marker='o', label=f'Inputted Lines: {lines}')
            # for x, y in zip(lines_df['batch_size'].values, lines_df['read_time'].values):
            #     plt.text(x, y, f'({x}, {y:.2f})', fontsize=8, ha='right')
    plt.title(f'Evolution of Inputted Lines - {city}')
    plt.xlabel('Batch Size')
    plt.ylabel('Map Reduce Time (s)')
    plt.legend()
    plt.grid(True)
    plt.tight_layout()
    plt.savefig(os.path.join(output_dir, f'{city}_{query_number}_plot_batch_size_vs_map_reduce_time.png'))

def plot_number_of_agencies_vs_read_time(df, city, output_dir, query_number):
    plt.figure(figsize=(8, 6))
    for lines in df['lines'].unique():
        lines_df = df[df['lines'] == lines]
        if not lines_df.empty:
            plt.plot(lines_df['number_of_agencies'].values, lines_df['read_time'].values, marker='o', label=f'Inputted Lines: {lines}')
            # for x, y in zip(lines_df['number_of_agencies'].values, lines_df['read_time'].values):
            #     plt.text(x, y, f'({x}, {y:.2f})', fontsize=8, ha='right')

    plt.title(f'Impact of Number of Agencies - {city}')
    plt.xlabel('Number of Agencies')
    plt.ylabel('Read Time (s)')
    plt.legend()
    plt.grid(True)
    plt.tight_layout()
    plt.savefig(os.path.join(output_dir, f'{city}_{query_number}_plot_number_of_agencies_vs_read_time.png'))
    plt.close()

def plot_number_of_agencies_vs_map_reduce_time(df, city, output_dir, query_number):
    plt.figure(figsize=(8, 6))
    for lines in df['lines'].unique():
        lines_df = df[df['lines'] == lines]
        if not lines_df.empty:
            plt.plot(lines_df['number_of_agencies'].values, lines_df['mapreduce_time'].values, marker='o', label=f'Inputted Lines: {lines}')
            # for x, y in zip(lines_df['number_of_agencies'].values, lines_df['read_time'].values):
            #     plt.text(x, y, f'({x}, {y:.2f})', fontsize=8, ha='right')

    plt.title(f'Impact of Number of Agencies - {city}')
    plt.xlabel('Number of Agencies')
    plt.ylabel('Map Reduce Time (s)')
    plt.legend()
    plt.grid(True)
    plt.tight_layout()
    plt.savefig(os.path.join(output_dir, f'{city}_{query_number}_plot_number_of_agencies_vs_map_reduce_time.png'))
    plt.close()

def plot_date_vs_read_time(df, city, output_dir, query_number):
    plt.figure(figsize=(8, 6))
    for lines in df['lines'].unique():
        lines_df = df[df['lines'] == lines]
        if not lines_df.empty:
            plt.plot(lines_df['date_range'].values, lines_df['read_time'].values, marker='o', label=f'Inputted Lines: {lines}')
            # for x, y in zip(lines_df['date_range'].values, lines_df['read_time'].values):
            #     plt.text(x, y, f'({x}, {y:.2f})', fontsize=8, ha='right')

    plt.title(f'Impact of Number of Agencies - {city}')
    plt.xlabel('Number of Agencies')
    plt.ylabel('Read Time (s)')
    plt.legend()
    plt.grid(True)
    plt.tight_layout()
    plt.savefig(os.path.join(output_dir, f'{city}_{query_number}_plot_date_vs_read_time.png'))
    plt.close()

def plot_date_vs_map_reduce_time(df, city, output_dir, query_number):
    plt.figure(figsize=(8, 6))
    for lines in df['lines'].unique():
        lines_df = df[df['lines'] == lines]
        if not lines_df.empty:
            plt.plot(lines_df['date_range'].values, lines_df['mapreduce_time'].values, marker='o', label=f'Inputted Lines: {lines}')
            # for x, y in zip(lines_df['date_range'].values, lines_df['read_time'].values):
            #     plt.text(x, y, f'({x}, {y:.2f})', fontsize=8, ha='right')

    plt.title(f'Impact of Number of Agencies - {city}')
    plt.xlabel('Number of Agencies')
    plt.ylabel('Map Reduce Time (s)')
    plt.legend()
    plt.grid(True)
    plt.tight_layout()
    plt.savefig(os.path.join(output_dir, f'{city}_{query_number}_plot_date_vs_map_reduce_time.png'))
    plt.close()