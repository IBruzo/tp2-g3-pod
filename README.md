
# READ ME
## Integrantes
- Iñaki Bengolea (63515), ibengolea@itba.edu.ar
- Felix Lopez Menardi (62707), flopezmenardi@itba.edu.ar
- Joaquín Eduardo Girod (63512), jgirod@itba.edu.a
- Ignacio Bruzone (62038), ibruzone@itba.edu.ar

## Prerrequisitos
Para poder correr el proyecto se deberá tener instalado y funcional:
- [Maven](https://maven.apache.org/install.html)
- [Java 17](https://www.java.com/en/download/help/download_options.html)

## Instalacion
1) Clonar el repositorio del proyecto. Para esto se podrá utilizar la consola con el comando:
   `git clone https://github.com/IBruzo/tp2-g3-pod`
2) `cd tp2-g3-pod`
3) `mvn clean install`
4) `mvn package`
5)  Ubicar la consola en server/target/ y ejecutar los siguientes comandos:
    `tar xvzf tp2-g3-pod-server-1.0-SNAPSHOT-bin.tar.gz`
    `cd tp2-g3-pod-server-1.0-SNAPSHOT`
    ` chmod -R +x *.sh`
6) Ubicar la consola en client/target/ y ejecutar los siguientes comandos:
   `tar xvzf tp2-g3-pod-client-1.0-SNAPSHOT-bin.tar.gz`
   `cd tp2-g3-pod-client-1.0-SNAPSHOT`
   ` chmod -R +x *.sh`

## Empezar el servidor
Ubicar en la consola  server/target/tp2-g3-pod-server-1.0-SNAPSHOT y ejecutar el siguiente comando:
```
./run-server.sh
```
Se puede usar -Dinterface para poder cambiar la interfaz que usa el servidor, por default tiene 127.0.0.*

## Correr clientes
Ubicar en otra consola /client/target/tp2-g3-pod-client-1.0-SNAPSHOT y correr el script de bash que desea usar
Como convencion para usar los scripts de las querys se usa:
```
$> sh queryX.sh -Daddresses='xx.xx.xx.xx:XXXX;yy.yy.yy.yy:YYYY' -Dcity=ABC
-DinPath=XX -DoutPath=YY [params]
```
-   queryX.sh es el script que corre la query X.
-   -Daddresses refiere a las direcciones IP de los nodos con sus puertos (una o más, separadas por punto y coma)
-   -Dcity indica con qué dataset de ciudad se desea trabajar. Los únicos valores posibles son NYC y CHI.
-   -DinPath indica el path donde están los archivos de entrada de multas e infracciones
    
-   -DoutPath indica el path donde estarán ambos archivos de salida query1_results.csv y time1.txt
-   [params] son los parámetros extras que corresponden para algunas queries.

Adicionalmente para testear hay 2 parametros mas que son: -Dlimit y -DbatchSize para poder limitar la cantidad de lineas que se leen y para controlar el tamaño del bache en que se procesan los CSV.

### QUERY1
Devuelve el tipo de infraccion y la cantidad de veces que se sanciono.
(No recibe parametros adicionales)
Ejemplo:
  ```
  sh query1.sh -Daddresses='10.6.0.1:5701' -Dcity=NYC -DinPath=./resources/
  -DoutPath=./resources/
  ```
  Su resultado estara en query1_results.csv y sus tiempo van a estar en time1.txt

### QUERY2
Devuelve el Top 3 de las infracciones mas populares por barrio.
(No recibe parametros adicionales)
```
  sh query2.sh -Daddresses='10.6.0.1:5701' -Dcity=NYC -DinPath=./resources/
  -DoutPath=./resources/
```
 Su resultado estara en query2_results.csv y sus tiempo van a estar en time2.txt
### QUERY3
Devuelve el Top N de agencias con mayor recaudacion
- usa -Dn como parametro adicional para cortar el la cantidad, si esta en 0 se muestran todos

```
sh query3.sh -Daddresses='10.6.0.1:5701' -Dcity=NYC -DinPath=./resources/
  -DoutPath=./resources/ -Dn=4
```
 Su resultado estara en query3_results.csv y sus tiempo van a estar en time3.txt
### QUERY4
Devuelve la patente con mas infracciones por barrio en un rango de dias
- -Dfrom y -Dto para especificar el rango de tiempo que se queire consultar 
	**(String DD/MM/YY)**
```
sh query4.sh -Daddresses='10.6.0.1:5701' -Dcity=NYC -DinPath=./resources/
  -DoutPath=./resources/ -Dfrom=27/12/2001 -Dto=10/04/2002
```
 Su resultado estara en query4_results.csv y sus tiempo van a estar en time4.txt
### QUERY5
Devuelve los Pares de infracciones que tienen, en grupos de a cientos, el mismo promedio de monto de multa
(no recibe parametros adicionales)

```
sh query5.sh -Daddresses='10.6.0.1:5701' -Dcity=NYC -DinPath=./resources/
  -DoutPath=./resources/
```
 Su resultado estara en query5_results.csv y sus tiempo van a estar en time5.txt
