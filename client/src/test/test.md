# Reporting

### Install Python Dependencies
```bash
pip install -r client/src/test/requirements.txt
```

### Tweak Plotting Values
In ```client/src/test/query_lib/config.py``` you can change the configuration, this is the current state
```python
cities_config = {
    "NYC": {
        "line_counts": [800000, 1600000, 3200000, 4800000, 6400000, 8000000],
        "batch_sizes": [50000, 100000, 200000, 400000, 800000]
    },
    "CHI": {
        "line_counts": [400000, 800000, 1600000, 3200000, 4800000],
        "batch_sizes": [50000, 100000, 200000, 400000]
    }
}
```

### Execute Commander
In order to test the execution of the query and producing the graphs, execute from project root the following command with the values you desire
```bash
bash client/src/test/commander.sh CLIENT_RAM SERVER_RAM PLOT_OUTPUT_PATH IN_PATH OUTPUT_PATH
```
For example:
```bash
bash client/src/test/commander.sh 6 6 /home/joaquin/Desktop/pod_data_plots/ /home/joaquin/Desktop/pod_data_sets/ /home/joaquin/Desktop/pod_data_outputs/
```
