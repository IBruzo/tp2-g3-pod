package org.example.client;

import com.hazelcast.client.HazelcastClient;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.ICompletableFuture;
import com.hazelcast.core.IMap;
import com.hazelcast.mapreduce.Job;
import com.hazelcast.mapreduce.JobTracker;
import com.hazelcast.mapreduce.KeyValueSource;
import org.example.client.models.LogEntry;
import org.example.models.Pair;
import org.example.models.Q5Infraction;
import org.example.query5.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutionException;

import static org.example.client.DocumentUtils.*;

public class Query5 {
    private static final Logger logger = LoggerFactory.getLogger(Query5.class);

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
        String timeOutputFileName = System.getProperty("timeOutputFileName", "time5");

        HazelcastInstance hazelcastInstance = HazelConfig.connect(addresses);

        IMap<String, Q5Infraction> infractionMap = hazelcastInstance.getMap("infractions5");
        IMap<String, String> codeInfraction = hazelcastInstance.getMap("codes5");

        DocumentUtils documentUtils = new DocumentUtils();

        logEntries.add(createLogEntry("Inicio de la lectura del archivo"));
        documentUtils.readQ5CSV(infractionMap, codeInfraction, cityProperty, inPath, batchSize, limit);
        logEntries.add(createLogEntry("Fin de la lectura del archivo"));

        Set<String> validKeys = new HashSet<>(codeInfraction.keySet());
        hazelcastInstance.getList("validKeys").addAll(codeInfraction.keySet());

        JobTracker infractionFinejobTracker = hazelcastInstance.getJobTracker("default");
        KeyValueSource<String, Q5Infraction> infractionFineSource = KeyValueSource.fromMap(infractionMap);
        Job<String, Q5Infraction> infractionFineJob = infractionFinejobTracker.newJob(infractionFineSource);

        logEntries.add(createLogEntry("Inicio del trabajo map/reduce"));
        ICompletableFuture<Map<String, Double>> infractionFineFuture = infractionFineJob
                .mapper(new InfractionPairMapper())
                .combiner(new InfractionPairCombinerFactory())
                .reducer(new InfractionPairReducerFactory())
                .submit(new InfractionPairCollator(codeInfraction));

        Map<String, Double> infractionFineResult = infractionFineFuture.get();
        logEntries.add(createLogEntry("Fin del trabajo map/reduce"));

        // Prepare second map/reduce
        IMap<String, Double> infractionFineIMap = hazelcastInstance.getMap("infractionFineResult");
        infractionFineIMap.putAll(infractionFineResult);

        JobTracker bracketInfractionJobTracker = hazelcastInstance.getJobTracker("default");
        KeyValueSource<String, Double> bracketInfractionSource = KeyValueSource.fromMap(infractionFineIMap);
        Job<String, Double> bracketInfractionJob = bracketInfractionJobTracker.newJob(bracketInfractionSource);

        // Second map/reduce
        logEntries.add(createLogEntry("Inicio del trabajo map/reduce"));
        ICompletableFuture<Map<Integer, List<Pair<String, String>>>> bracketInfractionFuture = bracketInfractionJob
                .mapper(new BracketInfractionMapper())
                .submit(new BracketInfractionCollator());

        Map<Integer, List<Pair<String, String>>> bracketInfractionResult = bracketInfractionFuture.get();
        logEntries.add(createLogEntry("Fin del trabajo map/reduce"));

        DocumentUtils.writeQuery5CSV(outPath + "query5_results.csv", bracketInfractionResult);
        writeLogEntriesToFile(5, logEntries, outPath, timeOutputFileName);

        // Shutdown
        HazelcastClient.shutdownAll();
    }

}