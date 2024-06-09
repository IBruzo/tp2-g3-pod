package org.example.client;

import com.hazelcast.client.HazelcastClient;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.ICompletableFuture;
import com.hazelcast.core.IMap;
import com.hazelcast.mapreduce.Job;
import com.hazelcast.mapreduce.JobTracker;
import com.hazelcast.mapreduce.KeyValueSource;
import org.example.models.Infraction;
import org.example.models.Pair;
import org.example.query3.InfractionPercentageCollator;
import org.example.query3.InfractionPercentageMapper;
import org.example.query3.InfractionPercentageReducerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;

import static org.example.client.DocumentUtils.writeTimeToFile;

public class Query3 {
    private static final Logger logger = LoggerFactory.getLogger(Query3.class);

    private static final String DEFAULT_ADDRESS = "127.0.0.1:5701";
    private static final String DEFAULT_CITY = "CHI";
    private static final String DEFAULT_DIRECTORY = "/Users/felixlopezmenardi/Documents/pod/TPE-2/csv-tp2/";
    private static final String DEFAULT_WRITE_DIRECTORY = "/Users/felixlopezmenardi/Documents/pod/TPE-2/write/";
    private static final String DEFAULT_TIMESTAMP_DIRECTORY = "/Users/felixlopezmenardi/Documents/pod/TPE-2/timestamp/";

    public static void main(String[] args) throws ExecutionException, InterruptedException, IOException {
        logger.info("hz-config Client Starting ...");

        String addressProperty = System.getProperty("addresses", DEFAULT_ADDRESS);
        String[] addresses = addressProperty.split(";");
        int topn = Integer.parseInt( System.getProperty("n","0"));
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

        writeTimeToFile(3, "Inicio de la lectura del archivo", timePath);
        documentUtils.readCSV(infractionMap, codeInfraction, cityProperty, inPath,batchSize,limit);
        writeTimeToFile(3, "Fin de la lectura del archivo", timePath);

        JobTracker jobTracker = hazelcastInstance.getJobTracker("default");
        KeyValueSource<String, Infraction> source = KeyValueSource.fromMap(infractionMap);
        Job<String, Infraction> job = jobTracker.newJob(source);

        writeTimeToFile(3, "Inicio del trabajo map/reduce", timePath);
        ICompletableFuture<List<Pair<String,Double>>> future = job
                .mapper(new InfractionPercentageMapper())
                .reducer(new InfractionPercentageReducerFactory())
                .submit(new InfractionPercentageCollator());

        List<Pair<String,Double>> result = future.get();
        writeTimeToFile(3, "Fin del trabajo map/reduce", timePath);
        if(topn !=0)
            result=result.subList(0, topn);

        DocumentUtils.writeQuery3CSV(outPath + "query3_results.csv", result);

        // Shutdown
        HazelcastClient.shutdownAll();
    }

}
