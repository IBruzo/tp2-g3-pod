package org.example.client;

import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.client.config.ClientNetworkConfig;
import com.hazelcast.config.GroupConfig;
import com.hazelcast.core.HazelcastInstance;

public class HazelConfig {

    public static HazelcastInstance connect(String[] addresses){
        // Client Config
        ClientConfig clientConfig = new ClientConfig();
        // Group Config
        GroupConfig groupConfig = new GroupConfig().setName("g0").setPassword("g0-pass");
        clientConfig.setGroupConfig(groupConfig);
        // Client Network Config
        ClientNetworkConfig clientNetworkConfig = new ClientNetworkConfig();

        clientNetworkConfig.addAddress(addresses);
        clientConfig.setNetworkConfig(clientNetworkConfig);
        return HazelcastClient.newHazelcastClient(clientConfig);

    }
}
