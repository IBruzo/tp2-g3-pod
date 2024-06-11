import os
import matplotlib.pyplot as plt

def plot_results(df, parameter, city, output_dir, query_number):
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
    filename = f"{city}_{parameter}_plot_{query_number}.png"
    filepath = os.path.join(output_dir, filename)
    plt.savefig(filepath)

def plot_batch_size_evolution(df, city, output_dir, query_number):
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
    plt.savefig(os.path.join(output_dir, f'{city}_{query_number}_batch_size_evolution.png'))

def plot_lines_evolution(df, city, output_dir, query_number):
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
    plt.savefig(os.path.join(output_dir, f'{city}_{query_number}_lines_evolution.png'))
