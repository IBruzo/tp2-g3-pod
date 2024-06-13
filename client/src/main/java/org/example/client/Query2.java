package org.example.client;

import com.hazelcast.client.HazelcastClient;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.ICompletableFuture;
import com.hazelcast.core.IMap;
import com.hazelcast.mapreduce.Job;
import com.hazelcast.mapreduce.JobTracker;
import com.hazelcast.mapreduce.KeyValueSource;
import org.example.client.models.LogEntry;
import org.example.models.Q2Infraction;
import org.example.query2.PopularInfractionsCollator;
import org.example.query2.PopularInfractionsCombinerFactory;
import org.example.query2.PopularInfractionsMapper;
import org.example.query2.PopularInfractionsReducerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutionException;

import static org.example.client.DocumentUtils.*;

public class Query2 {
    private static final Logger logger = LoggerFactory.getLogger(Query2.class);

    private static final String DEFAULT_ADDRESS = "127.0.0.1:5701";
    private static final String DEFAULT_CITY = "NYC";
    private static final String DEFAULT_DIRECTORY = "/Users/felixlopezmenardi/Documents/pod/TPE-2/csv-tp2/";
    private static final String DEFAULT_WRITE_DIRECTORY = "/Users/felixlopezmenardi/Documents/pod/TPE-2/write/";

    @SuppressWarnings("deprecation")
    public static void main(String[] args) throws ExecutionException, InterruptedException, IOException {
        logger.info("hz-config Client Starting ...");
        List<LogEntry> logEntries = new ArrayList<>();

        String addressProperty = System.getProperty("addresses", DEFAULT_ADDRESS);
        String[] addresses = addressProperty.split(";");
        String cityProperty = System.getProperty("city", DEFAULT_CITY);
        String inPath = System.getProperty("inPath", DEFAULT_DIRECTORY); // directory
        String outPath = System.getProperty("outPath", DEFAULT_WRITE_DIRECTORY); // directory
        int batchSize = Integer.parseInt(System.getProperty("batchSize", String.valueOf(100)));
        int limit = Integer.parseInt(System.getProperty("limit", String.valueOf(1000)));
        if(batchSize > limit)
            batchSize = limit;
        String timeOutputFileName = System.getProperty("timeOutputFileName", "time2");

        HazelcastInstance hazelcastInstance = HazelConfig.connect(addresses);

        IMap<String, Q2Infraction> infractionMap = hazelcastInstance.getMap("infractions");
        IMap<String, String> codeInfraction = hazelcastInstance.getMap("codes");

        DocumentUtils documentUtils = new DocumentUtils();

        logEntries.add(createLogEntry("Inicio de la lectura del archivo"));
        documentUtils.readQ2CSV(infractionMap, codeInfraction, cityProperty, inPath, batchSize, limit);
        logEntries.add(createLogEntry("Fin de la lectura del archivo"));

        hazelcastInstance.getList("validKeys").addAll(codeInfraction.keySet());

        JobTracker jobTracker = hazelcastInstance.getJobTracker("default");
        KeyValueSource<String, Q2Infraction> source = KeyValueSource.fromMap(infractionMap);
        Job<String, Q2Infraction> job = jobTracker.newJob(source);

        logEntries.add(createLogEntry("Inicio del trabajo map/reduce"));
        ICompletableFuture<Map<String, List<String>>> future = job
                .mapper(new PopularInfractionsMapper())
                .combiner(new PopularInfractionsCombinerFactory())
                .reducer(new PopularInfractionsReducerFactory())
                .submit(new PopularInfractionsCollator(codeInfraction));

        Map<String, List<String>> result = future.get();
        logEntries.add(createLogEntry("Fin del trabajo map/reduce"));

        DocumentUtils.writeQuery2CSV(outPath + "query2_results.csv", result);
        writeLogEntriesToFile(2, logEntries, outPath, timeOutputFileName);

        // Shutdown
        HazelcastClient.shutdownAll();
    }
}
