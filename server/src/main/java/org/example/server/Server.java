package org.example.server;

import com.hazelcast.config.*;
import com.hazelcast.core.Hazelcast;
import io.grpc.ServerBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.ObjectInputFilter;
import java.util.Collections;

public class Server {
    private static Logger logger = LoggerFactory.getLogger(Server.class);

    private static final String DEFAULT_INTERFACE = "127.0.0.*";

    public static void main(String[] args) throws InterruptedException, IOException {
        logger.info(" Server Starting ...");

        String interfaceProperty = System.getProperty("interface",DEFAULT_INTERFACE);

        // Config
        Config config = new Config();
        // Group Config
        GroupConfig groupConfig = new GroupConfig().setName("g0").setPassword("g0-pass");
        config.setGroupConfig(groupConfig);
        // Network Config
        MulticastConfig multicastConfig = new MulticastConfig();
        JoinConfig joinConfig = new JoinConfig().setMulticastConfig(multicastConfig);
        InterfacesConfig interfacesConfig = new InterfacesConfig()
                .setInterfaces(Collections.singletonList(interfaceProperty)).setEnabled(true);
        NetworkConfig networkConfig = new NetworkConfig().setInterfaces(interfacesConfig).setJoin(joinConfig);
        config.setNetworkConfig(networkConfig);

        /*
         * // Management Center Config
         * ManagementCenterConfig managementCenterConfig = new ManagementCenterConfig()
         * .setUrl("http://localhost:8080/mancenter/")
         * .setEnabled(true);
         * config.setManagementCenterConfig(managementCenterConfig);
         */
        // Start cluster

        Hazelcast.newHazelcastInstance(config);

    }
}
