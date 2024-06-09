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
import org.example.query2.PopularInfractionsCollator;
import org.example.query2.PopularInfractionsMapper;
import org.example.query2.PopularInfractionsReducerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import static org.example.client.DocumentUtils.writeTimeToFile;

public class Query2 {
    private static final Logger logger = LoggerFactory.getLogger(Query2.class);

    private static final String DEFAULT_ADDRESS = "127.0.0.1:5701";
    private static final String DEFAULT_CITY = "CHI";
    private static final String DEFAULT_DIRECTORY = "C:\\Users\\OEM\\Desktop\\facult\\POD\\tp2-g3-pod\\client\\src\\main\\resources\\";
    private static final String DEFAULT_WRITE_DIRECTORY = "C:\\Users\\OEM\\Desktop\\facult\\POD\\tp2-g3-pod\\client\\src\\main\\resources\\";
    private static final String DEFAULT_TIMESTAMP_DIRECTORY = "C:\\Users\\OEM\\Desktop\\facult\\POD\\tp2-g3-pod\\client\\src\\main\\resources\\";

    @SuppressWarnings("deprecation")
    public static void main(String[] args) throws ExecutionException, InterruptedException, IOException {
        logger.info("hz-config Client Starting ...");

        String addressProperty = System.getProperty("addresses", DEFAULT_ADDRESS);
        String[] addresses = addressProperty.split(";");
        String cityProperty = System.getProperty("city", DEFAULT_CITY);
        String inPath = System.getProperty("inPath", DEFAULT_DIRECTORY); // directory
        String outPath = System.getProperty("outPath", DEFAULT_WRITE_DIRECTORY); // directory
        String timePath = System.getProperty("timePath", DEFAULT_TIMESTAMP_DIRECTORY); // directory


        HazelcastInstance hazelcastInstance =  HazelConfig.connect(addresses);

        IMap<String, Infraction> infractionMap = hazelcastInstance.getMap("infractions");
        IMap<String, String> codeInfraction = hazelcastInstance.getMap("codes");

        DocumentUtils documentUtils = new DocumentUtils();

        writeTimeToFile(2, "Inicio de la lectura del archivo", timePath);
        documentUtils.readCSV(infractionMap, codeInfraction, cityProperty, inPath);
        writeTimeToFile(2, "Fin de la lectura del archivo", timePath);

        Set<String> validKeys = new HashSet<>(codeInfraction.keySet());
        hazelcastInstance.getList("validKeys").addAll(codeInfraction.keySet());

        JobTracker jobTracker = hazelcastInstance.getJobTracker("default");
        KeyValueSource<String, Infraction> source = KeyValueSource.fromMap(infractionMap);
        Job<String, Infraction> job = jobTracker.newJob(source);

        writeTimeToFile(2, "Inicio del trabajo map/reduce", timePath);
        ICompletableFuture<Map<String, List<String>>> future = job
                .mapper(new PopularInfractionsMapper())
                .reducer(new PopularInfractionsReducerFactory())
                .submit(new PopularInfractionsCollator(codeInfraction));

        Map<String, List<String>> result = future.get();
        writeTimeToFile(2, "Fin del trabajo map/reduce", timePath);

        DocumentUtils.writeQuery2CSV(outPath + "query2_results.csv", result);

        // Shutdown
        HazelcastClient.shutdownAll();
    }
}
