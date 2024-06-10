package org.example.client;

import com.hazelcast.client.HazelcastClient;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.ICompletableFuture;
import com.hazelcast.core.IMap;
import com.hazelcast.mapreduce.Job;
import com.hazelcast.mapreduce.JobTracker;
import com.hazelcast.mapreduce.KeyValueSource;
import org.example.client.models.LogEntry;
import org.example.models.Infraction;
import org.example.models.Pair;
import org.example.query3.InfractionPercentageCollator;
import org.example.query3.InfractionPercentageMapper;
import org.example.query3.InfractionPercentageReducerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import static org.example.client.DocumentUtils.*;

public class Query3 {
    private static final Logger logger = LoggerFactory.getLogger(Query3.class);

    private static final String DEFAULT_ADDRESS = "127.0.0.1:5701";
    private static final String DEFAULT_CITY = "CHI";
    private static final String DEFAULT_DIRECTORY = "/Users/felixlopezmenardi/Documents/pod/TPE-2/csv-tp2/";
    private static final String DEFAULT_WRITE_DIRECTORY = "/Users/felixlopezmenardi/Documents/pod/TPE-2/write/";

    public static void main(String[] args) throws ExecutionException, InterruptedException, IOException {
        logger.info("hz-config Client Starting ...");
        List<LogEntry> logEntries = new ArrayList<>();

        String addressProperty = System.getProperty("addresses", DEFAULT_ADDRESS);
        String[] addresses = addressProperty.split(";");
        int topn = Integer.parseInt(System.getProperty("n", "0"));
        String cityProperty = System.getProperty("city", DEFAULT_CITY);
        String inPath = System.getProperty("inPath", DEFAULT_DIRECTORY); // directory
        String outPath = System.getProperty("outPath", DEFAULT_WRITE_DIRECTORY); // directory
        int batchSize = Integer.parseInt(System.getProperty("batchSize", String.valueOf(1000000)));
        int limit = Integer.parseInt(System.getProperty("limit", String.valueOf(1000)));
        String timeOutputFileName = System.getProperty("timeOutputFileName", "time1");

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
        ICompletableFuture<List<Pair<String, Double>>> future = job
                .mapper(new InfractionPercentageMapper())
                .reducer(new InfractionPercentageReducerFactory())
                .submit(new InfractionPercentageCollator());

        List<Pair<String, Double>> result = future.get();
        logEntries.add(createLogEntry("Fin del trabajo map/reduce"));
        if (topn != 0)
            result = result.subList(0, topn);

        DocumentUtils.writeQuery3CSV(outPath + "query3_results.csv", result);
        writeLogEntriesToFile(3, logEntries, outPath, timeOutputFileName);

        // Shutdown
        HazelcastClient.shutdownAll();
    }

}
