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


import org.example.models.InfractionChicago;
import org.example.query1.TicketsPerInfractionCollator;
import org.example.query1.TicketsPerInfractionMapper;
import org.example.query1.TicketsPerInfractionReducers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;

public class Query1 {
    private static final Logger logger = LoggerFactory.getLogger(Query1.class);
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private static final String CSV_FILE_CHI = "ticketsCHI.csv";
    private static final String CSV_CODES_CHI = "infractionsCHI.csv";
    private static final String DEFAULT_ADDRESS = "127.0.0.1:5701";
    private static final String DEFAULT_CITY = "CHI";
    private static final String DEFAULT_DIRECTORY =  "C:\\Users\\OEM\\Desktop\\facult\\POD\\tp2-g3-pod\\client\\src\\main\\resources\\"; //"/afs/it.itba.edu.ar/pub/pod/";
    private static final String DEFAULT_WRITE_DIRECTORY = "C:\\Users\\OEM\\Desktop\\facult\\POD\\tp2-g3-pod\\client\\src\\main\\resources\\"; //"/afs/it.itba.edu.ar/pub/pod-write/";

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        logger.info("hz-config Client Starting ...");


        String addressProperty = System.getProperty("addresses",DEFAULT_ADDRESS);
        String[] addresses = addressProperty.split(";");
        String cityProperty = System.getProperty("city",DEFAULT_CITY);
        String inPath = System.getProperty("inPath",DEFAULT_DIRECTORY); //directory
        String outPath = System.getProperty("outPath",DEFAULT_WRITE_DIRECTORY); //directory

        System.out.println(addressProperty);
        System.out.println(Arrays.toString(addresses));
        System.out.println(cityProperty);
        System.out.println(inPath);
        System.out.println(outPath);



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

        IMap<String, InfractionChicago> infractionMap = hazelcastInstance.getMap("infractionsChicago");
        IMap<String, String> codeInfraction = hazelcastInstance.getMap("codesChicago");

        System.out.println("Infraction Map (should be empty) : " + infractionMap);

        try (BufferedReader br = new BufferedReader(new FileReader(inPath + CSV_FILE_CHI))) {
            String line = br.readLine();
            if (line == null){
                System.out.println("Empty CSV");
                return;
            }
            AtomicInteger idCounter = new AtomicInteger();
            while ((line = br.readLine()) != null && idCounter.get() < 300) {
                String[] values = line.split(";");
                Date date = parseDate(values[0]); //arreglar? values[0] es asi: 2020-01-01 00:00:00
                String licensePlateNumber = values[1];
                String violationCode = values[2];
                String unitDescription = values[3];
                String fineAmount = values[4];
                String communityAreaName = values[5];

                InfractionChicago infraction = new InfractionChicago(date, licensePlateNumber, violationCode,
                        unitDescription, communityAreaName,Integer.parseInt(fineAmount));
                String key = "infraction-" + idCounter.incrementAndGet();

                infractionMap.put(key, infraction);
            }
        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }

        try (BufferedReader br = new BufferedReader(new FileReader(inPath + CSV_CODES_CHI))) {
            String line = br.readLine();
            if (line == null){
                System.out.println("Empty CSV");
                return;
            }
            while ((line = br.readLine()) != null) {
                String[] values = line.split(";");
                if (values.length == 2) {
                    String code = values[0];
                    String description = values[1];
                    codeInfraction.put(code, description);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        Set<String> validKeys = new HashSet<>(codeInfraction.keySet());

        JobTracker jobTracker = hazelcastInstance.getJobTracker("default");
        KeyValueSource<String, InfractionChicago> source = KeyValueSource.fromMap(infractionMap);
        Job<String, InfractionChicago> job = jobTracker.newJob(source);

        ICompletableFuture<Map<String, Integer>> future = job
                .mapper(new TicketsPerInfractionMapper(validKeys))
                .reducer(new TicketsPerInfractionReducers.TicketsPerInfractionReducerFactory())
                .submit(new TicketsPerInfractionCollator());

        Map<String, Integer> result = future.get();
        System.out.println(result);

        // Shutdown
        HazelcastClient.shutdownAll();
    }

    private static Date parseDate(String dateString) throws ParseException {
        return dateFormat.parse(dateString);
    }
}
