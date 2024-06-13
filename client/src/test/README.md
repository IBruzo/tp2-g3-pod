# Testing Suite

## How to run the analytics?

### Install Python Dependencies
```bash
pip install -r client/src/test/requirements.txt
```

### Tweak Input Values
In ```client/src/test/query_lib/config.py``` you can change the configuration, this was the last configuration used, adding values may add executions in a non linear manner
```python
cities_config = {
    "NYC": {
        "line_counts": [1000000, 2000000, 3000000, 4000000, 5000000],
        "batch_sizes": [10000, 25000, 50000, 100000, 200000],
        "number_of_agencies": [50, 200, 400],
        "date_range" : [["01/01/2017", "7/01/2017"], ["01/01/2017", "31/01/2017"], ["01/01/2017", "1/03/2017"]]
    },
    "CHI": {
        "line_counts": [1000000, 2000000, 3000000, 4000000, 5000000],
        "batch_sizes": [10000, 25000, 50000, 100000, 200000],
        "number_of_agencies": [50, 200, 400],
        "date_range" : [["01/01/2017", "7/01/2017"], ["01/01/2017", "31/01/2017"], ["01/01/2017", "1/03/2017"]]
    }
}
```

### Execute Commander
In order to test the execution of the query and producing the graphs, execute from project root the following command with the values you desire
```bash
bash client/src/test/commander.sh CLIENT_RAM SERVER_RAM PLOT_OUTPUT_PATH IN_PATH OUTPUT_PATH ADDRESSES
```
For example:
```bash
bash client/src/test/commander.sh 6 6 /home/joaquin/Desktop/pod_data_plots/ /home/joaquin/Desktop/pod_data_sets/ /home/joaquin/Desktop/pod_data_outputs/ 127.0.0.1:5701
```

### How to connect multiple nodes
1. Connect all nodes to the old modem via Ethernet (better MTU).
2. Each node must identify the interface that is connected, usually labeled (Ethernet).
3. Each node should apply a mask with the command ```sudo ifconfig enp3s0 192.168.1.x netmask 255.255.255.0 up```, where each node chooses a different x, different from 1 because the modem usually has that address.
4. Verify the IPs with ```ip addr show <interface>``` (interface usually is enp3s0 or enp8s0).
5. Ping between the computers and use Wireshark to verify the source IP addresses.
6. On one node, run ```bash client/src/test/node_set_up.sh <server_ram> "192.168.1.*"``` from the root of the project.
7. Check which IP the previous server started on and execute on the other nodes ```bash client/src/test/node_set_up.sh <server_ram> <first_server_ip>``` also from project root


#### Another way to do this Step 3 (delete the interfaces file afterward, otherwise when you restart the PC you will lose that interface):
1. Execute sudo nvim /etc/network/interfaces
2. Paste the following, making the necessary modifications:
    - The gateway should be the IP address of the modem.
    - The address should be different on each node.
    - The interface is not necessarily enp8s0, it could be enp3s0.
```
auto enp8s0
iface enp8s0 inet static
address 192.168.1.11
netmask 255.255.255.0
gateway 192.168.1.1
```
3. Restart the interface with ```sudo ip link set enp8s0 down``` and then ```sudo ip link set enp8s0 up```.
