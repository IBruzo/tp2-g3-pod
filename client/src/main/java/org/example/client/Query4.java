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
import org.example.query4.InfractionsInNeighborhoodCollator;
import org.example.query4.InfractionsInNeighborhoodMapper;
import org.example.query4.InfractionsInNeighborhoodReducerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;

import static org.example.client.DocumentUtils.writeTimeToFile;

public class Query4 {

    private static final Logger logger = LoggerFactory.getLogger(Query1.class);

    private static final String DEFAULT_ADDRESS = "127.0.0.1:5701";
    private static final String DEFAULT_CITY = "CHI";
    private static final String DEFAULT_DIRECTORY = "/Users/felixlopezmenardi/Documents/pod/TPE-2/csv-tp2/";
    private static final String DEFAULT_WRITE_DIRECTORY = "/Users/felixlopezmenardi/Documents/pod/TPE-2/write/";
    private static final String DEFAULT_TIMESTAMP_DIRECTORY = "/Users/felixlopezmenardi/Documents/pod/TPE-2/timestamp/";
    private static final String DEFAULT_FROM = "01/01/1970";
    private static final String DEFAULT_TO = "31/12/2023";


    @SuppressWarnings("deprecation")
    public static void main(String[] args) throws ExecutionException, InterruptedException, IOException {
        logger.info("hz-config Client Starting ...");

        String addressProperty = System.getProperty("addresses", DEFAULT_ADDRESS);
        String[] addresses = addressProperty.split(";");
        String cityProperty = System.getProperty("city", DEFAULT_CITY);
        String inPath = System.getProperty("inPath", DEFAULT_DIRECTORY); // directory
        String outPath = System.getProperty("outPath", DEFAULT_WRITE_DIRECTORY); // directory
        String timePath = System.getProperty("timePath", DEFAULT_TIMESTAMP_DIRECTORY); // directory
        int batchSize = Integer.parseInt(System.getProperty("batchSize", String.valueOf(1000000)));
        int limit = Integer.parseInt(System.getProperty("limit", String.valueOf(0)));

        HazelcastInstance hazelcastInstance =  HazelConfig.connect(addresses);

        IMap<String, Infraction> infractionMap = hazelcastInstance.getMap("infractions");
        IMap<String, String> codeInfraction = hazelcastInstance.getMap("codes");

        DocumentUtils documentUtils = new DocumentUtils();

        writeTimeToFile(4, "Inicio de la lectura del archivo", timePath);
        documentUtils.readCSV(infractionMap, codeInfraction, cityProperty, inPath,batchSize,limit);
        writeTimeToFile(4, "Fin de la lectura del archivo", timePath);

        JobTracker jobTracker = hazelcastInstance.getJobTracker("default");
        KeyValueSource<String, Infraction> source = KeyValueSource.fromMap(infractionMap);
        Job<String, Infraction> job = jobTracker.newJob(source);

        writeTimeToFile(4, "Inicio del trabajo map/reduce", timePath);
        ICompletableFuture<List<String>> future = job
                .mapper(new InfractionsInNeighborhoodMapper(DEFAULT_FROM, DEFAULT_TO))
                .reducer(new InfractionsInNeighborhoodReducerFactory())
                .submit(new InfractionsInNeighborhoodCollator());

        List<String> result = future.get();
        writeTimeToFile(4, "Fin del trabajo map/reduce", timePath);

        DocumentUtils.writeQuery4CSV(outPath + "query4_results.csv", result);

        // Shutdown
        HazelcastClient.shutdownAll();
    }

}
