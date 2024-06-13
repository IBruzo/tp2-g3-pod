package org.example.query1;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.HazelcastInstanceAware;
import com.hazelcast.mapreduce.Context;
import com.hazelcast.mapreduce.Mapper;
import org.example.models.Q1Infraction;

import java.util.HashSet;
import java.util.Set;


public class TicketsPerInfractionMapper implements Mapper<String, Q1Infraction, String, Integer>, HazelcastInstanceAware {
    private transient Set<String> codeMap;

    @Override
    public void map(String key, Q1Infraction value, Context<String, Integer> context) {
        if (codeMap != null && codeMap.contains(value.getViolationCode())) {
            context.emit(value.getViolationCode(), 1);
        }
    }

    @Override
    public void setHazelcastInstance(HazelcastInstance hazelcastInstance) {
        // Initialize codeMap using the Hazelcast instance
        this.codeMap = new HashSet<>(hazelcastInstance.getList("validKeys"));
    }
}
