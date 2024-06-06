package org.example.client;

import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.client.config.ClientNetworkConfig;
import com.hazelcast.config.GroupConfig;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.ICompletableFuture;
import com.hazelcast.core.IList;
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

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;

public class Query1 {
    private static final Logger logger = LoggerFactory.getLogger(Query1.class);

    private static final String DEFAULT_ADDRESS = "127.0.0.1:5701";
    private static final String DEFAULT_CITY = "CHI";
    private static final String DEFAULT_DIRECTORY = "/Users/felixlopezmenardi/Documents/pod/TPE-2/csv-tp2/";
    private static final String DEFAULT_WRITE_DIRECTORY = "/Users/felixlopezmenardi/Documents/pod/TPE-2/write/";

    @SuppressWarnings("deprecation")
    public static void main(String[] args) throws ExecutionException, InterruptedException, IOException {
        logger.info("hz-config Client Starting ...");

        String addressProperty = System.getProperty("addresses", DEFAULT_ADDRESS);
        String[] addresses = addressProperty.split(";");
        String cityProperty = System.getProperty("city", DEFAULT_CITY);
        String inPath = System.getProperty("inPath", DEFAULT_DIRECTORY); // directory
        String outPath = System.getProperty("outPath", DEFAULT_WRITE_DIRECTORY); // directory

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

        documentUtils.readCSV(infractionMap, codeInfraction, cityProperty, inPath);

        Set<String> validKeys = new HashSet<>(codeInfraction.keySet());
        hazelcastInstance.getList("validKeys").addAll(codeInfraction.keySet());

        JobTracker jobTracker = hazelcastInstance.getJobTracker("default");
        KeyValueSource<String, Infraction> source = KeyValueSource.fromMap(infractionMap); //.fromList(infractionList);
        Job<String, Infraction> job = jobTracker.newJob(source);

        ICompletableFuture<Map<String, Integer>> future = job
                .mapper(new TicketsPerInfractionMapper())
                .reducer(new TicketsPerInfractionReducerFactory())
                .submit(new TicketsPerInfractionCollator(codeInfraction));

        Map<String, Integer> result = future.get();
        //System.out.println(result);

        DocumentUtils.writeQuery1CSV(outPath + "query1_results.csv", result);

        // Shutdown
        HazelcastClient.shutdownAll();
    }

}
