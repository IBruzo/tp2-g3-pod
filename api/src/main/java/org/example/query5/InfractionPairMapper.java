package org.example.query5;

import com.hazelcast.mapreduce.Context;
import com.hazelcast.mapreduce.Mapper;
import org.example.models.Infraction;

import java.util.Set;

public class InfractionPairMapper implements Mapper<String, Infraction, String, Double> {
    private final Set<String> codeMap;

    public InfractionPairMapper(Set<String> codeMap) {
        this.codeMap = codeMap;
    }

    @Override
    public void map(String key, Infraction value, Context<String, Double> context) {
        // Infraction Code validation
        if(codeMap.contains(value.getViolationCode()))
            context.emit(value.getViolationCode(), value.getFineAmount());
    }
}
