package org.example.query2;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.HazelcastInstanceAware;
import com.hazelcast.mapreduce.Context;
import com.hazelcast.mapreduce.Mapper;
import org.example.models.Infraction;
import org.example.models.Pair;

import java.util.HashSet;
import java.util.Set;

public class PopularInfractionsMapper implements Mapper<String, Infraction, String, Pair<String, Integer>>, HazelcastInstanceAware {
    private transient Set<String> codeMap;

    @Override
    public void setHazelcastInstance(HazelcastInstance hazelcastInstance) {
        // Initialize codeMap using the Hazelcast instance
        this.codeMap = new HashSet<>(hazelcastInstance.getList("validKeys"));
    }

    @Override
    public void map(String key, Infraction value, Context<String, Pair<String, Integer>> context) {
        // Infraction Code validation
        if (codeMap.contains(value.getViolationCode()))
            context.emit(value.getCommunityAreaName(), new Pair<>(value.getViolationCode(),1));
    }
}