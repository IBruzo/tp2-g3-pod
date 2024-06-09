package org.example.client;

import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.client.config.ClientNetworkConfig;
import com.hazelcast.config.GroupConfig;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.ICompletableFuture;
import com.hazelcast.core.IMap;
import com.hazelcast.mapreduce.Job;
import com.hazelcast.mapreduce.JobTracker;
import com.hazelcast.mapreduce.KeyValueSource;
import org.example.models.Infraction;
import org.example.query1.TicketsPerInfractionCollator;
import org.example.query1.TicketsPerInfractionMapper;
import org.example.query1.TicketsPerInfractionReducerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import static org.example.client.DocumentUtils.writeTimeToFile;

public class Query1 {
    private static final Logger logger = LoggerFactory.getLogger(Query1.class);

    private static final String DEFAULT_ADDRESS = "127.0.0.1:5701";
    private static final String DEFAULT_CITY = "CHI";
    private static final String DEFAULT_DIRECTORY = "/Users/felixlopezmenardi/Documents/pod/TPE-2/csv-tp2/";
    private static final String DEFAULT_WRITE_DIRECTORY = "/Users/felixlopezmenardi/Documents/pod/TPE-2/write/";
    private static final String DEFAULT_TIMESTAMP_DIRECTORY = "/Users/felixlopezmenardi/Documents/pod/TPE-2/timestamp/";

    @SuppressWarnings("deprecation")
    public static void main(String[] args) throws ExecutionException, InterruptedException, IOException {
        logger.info("hz-config Client Starting ...");

        String addressProperty = System.getProperty("addresses", DEFAULT_ADDRESS);
        String[] addresses = addressProperty.split(";");
        String cityProperty = System.getProperty("city", DEFAULT_CITY);
        String inPath = System.getProperty("inPath", DEFAULT_DIRECTORY); // directory
        String outPath = System.getProperty("outPath", DEFAULT_WRITE_DIRECTORY); // directory
        String timePath = System.getProperty("timePath", DEFAULT_TIMESTAMP_DIRECTORY); // directory

        // Client Config
        ClientConfig clientConfig = new ClientConfig();
        // Group Config
        GroupConfig groupConfig = new GroupConfig().setName("g0").setPassword("g0-pass");
        clientConfig.setGroupConfig(groupConfig);
        // Client Network Config
        ClientNetworkConfig clientNetworkConfig = new ClientNetworkConfig();

        clientNetworkConfig.addAddress(addresses);
        clientConfig.setNetworkConfig(clientNetworkConfig);
        HazelcastInstance hazelcastInstance = HazelcastClient.newHazelcastClient(clientConfig);

        IMap<String, Infraction> infractionMap = hazelcastInstance.getMap("infractions");
        IMap<String, String> codeInfraction = hazelcastInstance.getMap("codes");

        DocumentUtils documentUtils = new DocumentUtils();

        writeTimeToFile(1, "Inicio de la lectura del archivo", timePath);
        documentUtils.readCSV(infractionMap, codeInfraction, cityProperty, inPath);
        writeTimeToFile(1, "Fin de la lectura del archivo", timePath);

        Set<String> validKeys = new HashSet<>(codeInfraction.keySet());
        hazelcastInstance.getList("validKeys").addAll(codeInfraction.keySet());

        JobTracker jobTracker = hazelcastInstance.getJobTracker("default");
        KeyValueSource<String, Infraction> source = KeyValueSource.fromMap(infractionMap); //.fromList(infractionList);
        Job<String, Infraction> job = jobTracker.newJob(source);

        writeTimeToFile(1, "Inicio del trabajo map/reduce", timePath);
        ICompletableFuture<Map<String, Integer>> future = job
                .mapper(new TicketsPerInfractionMapper())
                .reducer(new TicketsPerInfractionReducerFactory())
                .submit(new TicketsPerInfractionCollator(codeInfraction));

        Map<String, Integer> result = future.get();
        writeTimeToFile(1, "Fin del trabajo map/reduce", timePath);

        DocumentUtils.writeQuery1CSV(outPath + "query1_results.csv", result);

        // Shutdown
        HazelcastClient.shutdownAll();
    }

//    private static void writeTimeToFile(int queryNumber, String message) throws IOException {
//        String fileName = timePath + "time" + queryNumber + ".txt";
//        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss:SSSS");
//        String formattedTime = LocalDateTime.now().format(formatter);
//        try (var writer = Files.newBufferedWriter(Path.of(fileName), StandardOpenOption.CREATE, StandardOpenOption.APPEND)) {
//            writer.write( formattedTime + " - " + message + "\n");
//        }
//    }
}
