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
import org.example.client.models.LogEntry;
import org.example.models.Infraction;
import org.example.query4.InfractionsInNeighborhoodCollator;
import org.example.query4.InfractionsInNeighborhoodMapper;
import org.example.query4.InfractionsInNeighborhoodReducerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import static org.example.client.DocumentUtils.*;

public class Query4 {

    private static final Logger logger = LoggerFactory.getLogger(Query4.class);

    private static final String DEFAULT_ADDRESS = "127.0.0.1:5701";
    private static final String DEFAULT_CITY = "NYC";
    private static final String DEFAULT_DIRECTORY = "/Users/inakibengolea/tp2-g3-pod/client/src/main/resources/";
    private static final String DEFAULT_WRITE_DIRECTORY = "/Users/inakibengolea/tp2-g3-pod/client/src/main/resources/";
    private static final String DEFAULT_FROM = "01/01/2017";
    private static final String DEFAULT_TO = "31/12/2017";

    @SuppressWarnings("deprecation")
    public static void main(String[] args) throws ExecutionException, InterruptedException, IOException {
        logger.info("hz-config Client Starting ...");
        List<LogEntry> logEntries = new ArrayList<>();

        String addressProperty = System.getProperty("addresses", DEFAULT_ADDRESS);
        String[] addresses = addressProperty.split(";");
        String cityProperty = System.getProperty("city", DEFAULT_CITY);
        String inPath = System.getProperty("inPath", DEFAULT_DIRECTORY); // directory
        String outPath = System.getProperty("outPath", DEFAULT_WRITE_DIRECTORY); // directory
        String from = System.getProperty("from", DEFAULT_FROM); // directory
        String to = System.getProperty("to", DEFAULT_TO); // directory
        int batchSize = Integer.parseInt(System.getProperty("batchSize", String.valueOf(1000000)));
        int limit = Integer.parseInt(System.getProperty("limit", String.valueOf(0)));
        String timeOutputFileName = System.getProperty("timeOutputFileName", "time4");

        HazelcastInstance hazelcastInstance = HazelConfig.connect(addresses);

        IMap<String, Infraction> infractionMap = hazelcastInstance.getMap("infractions");
        IMap<String, String> codeInfraction = hazelcastInstance.getMap("codes");

        DocumentUtils documentUtils = new DocumentUtils();

        logEntries.add(createLogEntry("Inicio de la lectura del archivo"));
        documentUtils.readCSV(infractionMap, codeInfraction, cityProperty, inPath, batchSize, limit);
        logEntries.add(createLogEntry("Fin de la lectura del archivo"));

        JobTracker jobTracker = hazelcastInstance.getJobTracker("default");
        KeyValueSource<String, Infraction> source = KeyValueSource.fromMap(infractionMap);
        Job<String, Infraction> job = jobTracker.newJob(source);

        logEntries.add(createLogEntry("Inicio del trabajo map/reduce"));
        ICompletableFuture<List<String>> future = job
                .mapper(new InfractionsInNeighborhoodMapper(from, to))
                .reducer(new InfractionsInNeighborhoodReducerFactory())
                .submit(new InfractionsInNeighborhoodCollator());

        List<String> result = future.get();
        logEntries.add(createLogEntry("Fin del trabajo map/reduce"));

        DocumentUtils.writeQuery4CSV(outPath + "query4_results.csv", result);
        writeLogEntriesToFile(4, logEntries, outPath, timeOutputFileName);

        // Shutdown
        HazelcastClient.shutdownAll();
    }

}
