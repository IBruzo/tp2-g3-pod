package org.example.client;

import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.client.config.ClientNetworkConfig;
import com.hazelcast.config.GroupConfig;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.example.models.InfractionChicago;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class Client {
    private static final Logger logger = LoggerFactory.getLogger(Client.class);
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private static final String CSV_FILE = "infractionsCHI.csv";

    public static void main(String[] args) {
        logger.info("hz-config Client Starting ...");
        // Client Config
        ClientConfig clientConfig = new ClientConfig();
        // Group Config
        GroupConfig groupConfig = new GroupConfig().setName("g0").setPassword("g0-pass");
        clientConfig.setGroupConfig(groupConfig);
        // Client Network Config
        ClientNetworkConfig clientNetworkConfig = new ClientNetworkConfig();
        String[] addresses = { "192.168.1.7:5701" };
        clientNetworkConfig.addAddress(addresses);
        clientConfig.setNetworkConfig(clientNetworkConfig);
        HazelcastInstance hazelcastInstance = HazelcastClient.newHazelcastClient(clientConfig);

        // String mapName = "testMap";
        // IMap<Integer, String> testMapFromMember = hazelcastInstance.getMap(mapName);
        // testMapFromMember.set(1, "test1");
        // IMap<Integer, String> testMap = hazelcastInstance.getMap(mapName);
        // System.out.println(testMap.get(1));

        IMap<String, InfractionChicago> infractionMap = hazelcastInstance.getMap("infractionsChicago");

        try (BufferedReader br = new BufferedReader(new FileReader(CSV_FILE))) {
            String line;
            AtomicInteger idCounter = new AtomicInteger();
            while ((line = br.readLine()) != null) {
                String[] values = line.split(";");
                Date date = parseDate(values[0] + " " + values[1]);
                String licensePlateNumber = values[2];
                String violationCode = values[3];
                String unitDescription = values[4];
                String communityAreaName = values[5];

                InfractionChicago infraction = new InfractionChicago(date, licensePlateNumber, violationCode,
                        unitDescription, communityAreaName);
                String key = "infraction-" + idCounter.incrementAndGet();

                infractionMap.put(key, infraction);
            }
        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }

        // Shutdown
        HazelcastClient.shutdownAll();
    }

    private static Date parseDate(String dateString) throws ParseException {
        return dateFormat.parse(dateString);
    }
}
