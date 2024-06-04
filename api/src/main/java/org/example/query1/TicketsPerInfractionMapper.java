package org.example.query1;

import com.hazelcast.mapreduce.Context;
import com.hazelcast.mapreduce.Mapper;
import org.example.models.Infraction;

import java.util.HashSet;
import java.util.Set;

public class TicketsPerInfractionMapper implements Mapper<String, Infraction, String, Integer> {
    private final Set<String> codeMap;

    public TicketsPerInfractionMapper(Set<String> codeMap) {
        this.codeMap = codeMap;
    }

    @Override
    public void map(String key, Infraction value, Context<String, Integer> context) {
        if(codeMap.contains(value.getViolationCode()))
            context.emit(value.getViolationCode(), 1);
    }
}
