package org.example.query1;

import com.hazelcast.mapreduce.Context;
import com.hazelcast.mapreduce.Mapper;
import org.example.models.InfractionChicago;

import java.util.Set;

public class TicketsPerInfractionMapper implements Mapper<String, InfractionChicago, String, Integer> {
    private final Set<String> codeMap;

    public TicketsPerInfractionMapper(Set<String> codeMap) {
        this.codeMap = codeMap;
    }

    @Override
    public void map(String key, InfractionChicago value, Context<String, Integer> context) {
        if(codeMap.contains(value.getViolationCode()))
            context.emit(value.getViolationCode(), 1);
    }
}
