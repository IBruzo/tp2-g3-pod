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
import org.example.models.Pair;
import org.example.query5.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import static org.example.client.DocumentUtils.writeTimeToFile;

public class Query5 {
    private static final Logger logger = LoggerFactory.getLogger(Query5.class);

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

        writeTimeToFile(5, "Inicio de la lectura del archivo", timePath);
        documentUtils.readCSV(infractionMap, codeInfraction, cityProperty, inPath);
        writeTimeToFile(5, "Fin de la lectura del archivo", timePath);

        Set<String> validKeys = new HashSet<>(codeInfraction.keySet());
        hazelcastInstance.getList("validKeys").addAll(codeInfraction.keySet());

        JobTracker infractionFinejobTracker = hazelcastInstance.getJobTracker("default");
        KeyValueSource<String, Infraction> infractionFineSource = KeyValueSource.fromMap(infractionMap);
        Job<String, Infraction> infractionFineJob = infractionFinejobTracker.newJob(infractionFineSource);

        writeTimeToFile(5, "Inicio del trabajo map/reduce", timePath);
        ICompletableFuture<Map<String, Double>> infractionFineFuture = infractionFineJob
                .mapper(new InfractionPairMapper())
                .reducer(new InfractionPairReducerFactory())
                .submit(new InfractionPairCollator(codeInfraction));

        Map<String, Double> infractionFineResult = infractionFineFuture.get();

        // Prepare second map/reduce
        IMap<String, Double> infractionFineIMap = hazelcastInstance.getMap("infractionFineResult");
        infractionFineIMap.putAll(infractionFineResult);

        JobTracker bracketInfractionJobTracker = hazelcastInstance.getJobTracker("default");
        KeyValueSource<String, Double> bracketInfractionSource = KeyValueSource.fromMap(infractionFineIMap);
        Job<String, Double> bracketInfractionJob = bracketInfractionJobTracker.newJob(bracketInfractionSource);

        // Second map/reduce
        ICompletableFuture<Map<Integer, List<Pair<String, String>>>> bracketInfractionFuture = bracketInfractionJob
                .mapper(new BracketInfractionMapper())
                .submit(new BracketInfractionCollator());

        Map<Integer, List<Pair<String, String>>> bracketInfractionResult = bracketInfractionFuture.get();

        writeTimeToFile(5, "Fin del trabajo map/reduce", timePath);

        DocumentUtils.writeQuery5CSV(outPath + "query5_results.csv", bracketInfractionResult);

        // Shutdown
        HazelcastClient.shutdownAll();
    }

}