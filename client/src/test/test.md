# Reporting

### Set Up
For decompressing the bash files in the client and server run from the root of the project the following command
```bash
sh client/src/test/set_up.sh
```

### Execution
In order to test the execution of the query and producing the graphs, execute the following command
Warning: the script has values set for usage of ram, query being tested and output locations which should all be changed into variable arguments instead of hardcoded variables
```bash
python3 client/src/test/query_1_tester.py
```
