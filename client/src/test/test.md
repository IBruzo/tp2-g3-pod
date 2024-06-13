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
bash client/src/test/commander.sh CLIENT_RAM SERVER_RAM PLOT_OUTPUT_PATH IN_PATH OUTPUT_PATH ADDRESSES
```
For example:
```bash
bash client/src/test/commander.sh 6 6 /home/joaquin/Desktop/pod_data_plots/ /home/joaquin/Desktop/pod_data_sets/ /home/joaquin/Desktop/pod_data_outputs/ 127.0.0.1:5701
```

### How to connect multiple nodes
- Conectar a traves de ethernet al modem viejo (mejor MTU)
- Puede que el modem les asigne una ip, o puede que no les asigne una ip cuando se conecten, en el segundo caso hay que hacer lo siguiente:
    - Ejecutar ```sudo nvim /etc/network/interfaces```
    - Pegar esto haciendo las modificaciones necesarias:
        - El gateway tiene que ser la ip del modem
        - La address tiene que ser distinta en cada nodo
        - La interfaz no necesariamente es enp8s0, puede ser enp3s0
    ```
    auto enp8s0
    iface enp8s0 inet static
    address 192.168.1.11
    netmask 255.255.255.0
    gateway 191.168.1.1
    ```
    - Reiniciar la interfaz con ```sudo ip link set enp8s0 down``` y luego ```sudo ip link set enp8s0 up```
- Verificar las IPs con ```ip addr show enp8s0``` (o la interfaz que sea)
- Hacer pings entre las computadoras y se puede usar wireshark para verificar de que IP vienen
- Levantar el servidor en una de ellas
- Correr 'bash client/src/test/node_set_up.sh <server_ram> <interface>' desde el root del proyecto
