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
import org.example.models.Pair;
import org.example.query1.TicketsPerInfractionCollator;
import org.example.query1.TicketsPerInfractionMapper;
import org.example.query5.InfractionPairCollator;
import org.example.query5.InfractionPairMapper;
import org.example.query5.InfractionPairReducerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;

public class Query5 {
    private static final Logger logger = LoggerFactory.getLogger(Query5.class);

    private static final String DEFAULT_ADDRESS = "127.0.0.1:5701";
    private static final String DEFAULT_CITY = "CHI";
    private static final String DEFAULT_DIRECTORY = "/home/joaquin/Desktop/hazelcast/client/src/main/resources/";
    private static final String DEFAULT_WRITE_DIRECTORY = "/home/joaquin/Desktop/hazelcast/client/src/main/resources/";

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

        IList<Infraction> infractionList = hazelcastInstance.getList("infractions");
        IMap<String, String> codeInfraction = hazelcastInstance.getMap("codes");

        DocumentUtils documentUtils = new DocumentUtils();

        documentUtils.readCSV(infractionList, codeInfraction, cityProperty, inPath);

        Set<String> validKeys = new HashSet<>(codeInfraction.keySet());

        JobTracker jobTracker = hazelcastInstance.getJobTracker("default");
        KeyValueSource<String, Infraction> source = KeyValueSource.fromList(infractionList);
        Job<String, Infraction> job = jobTracker.newJob(source);

        ICompletableFuture<Map<Integer, List<Pair<String, String>>>> future = job
                .mapper(new InfractionPairMapper(validKeys))
                .reducer(new InfractionPairReducerFactory())
                .submit(new InfractionPairCollator(codeInfraction));

        Map<Integer, List<Pair<String, String>>> result = future.get();
        System.out.println(result);

        DocumentUtils.writeQuery2CSV(outPath + "query2_results.csv", result);

        // Shutdown
        HazelcastClient.shutdownAll();
    }

}