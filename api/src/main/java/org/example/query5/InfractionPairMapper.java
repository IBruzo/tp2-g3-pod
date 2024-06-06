package org.example.query5;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.HazelcastInstanceAware;
import com.hazelcast.mapreduce.Context;
import com.hazelcast.mapreduce.Mapper;
import org.example.models.Infraction;

import java.util.HashSet;
import java.util.Set;

@SuppressWarnings("deprecation")
public class InfractionPairMapper implements Mapper<String, Infraction, String, Double>, HazelcastInstanceAware {
    private transient Set<String> codeMap;

    @Override
    public void setHazelcastInstance(HazelcastInstance hazelcastInstance) {
        // Initialize codeMap using the Hazelcast instance
        this.codeMap = new HashSet<>(hazelcastInstance.getList("validKeys"));
    }

    @Override
    public void map(String key, Infraction value, Context<String, Double> context) {
        // Infraction Code validation
        if (codeMap.contains(value.getViolationCode()))
            context.emit(value.getViolationCode(), value.getFineAmount());
    }
}
