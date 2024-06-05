package org.example.query2;

import com.hazelcast.mapreduce.Context;
import com.hazelcast.mapreduce.Mapper;
import org.example.models.Infraction;
import org.example.models.Pair;

import java.util.Set;

public class PopularInfractionsMapper implements Mapper<String, Infraction, String, Pair<String, Integer>> {
    private final Set<String> codeMap;

    public PopularInfractionsMapper(Set<String> codeMap) {
        this.codeMap = codeMap;
    }

    @Override
    public void map(String key, Infraction value, Context<String, Pair<String, Integer>> context) {
        // Infraction Code validation
        if (codeMap.contains(value.getViolationCode()))
            context.emit(value.getCommunityAreaName(), new Pair<>(value.getViolationCode(),1));
    }
}