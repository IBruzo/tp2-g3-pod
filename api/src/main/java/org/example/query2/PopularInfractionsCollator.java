package org.example.query2;

import com.hazelcast.core.IMap;
import com.hazelcast.mapreduce.Collator;

import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class PopularInfractionsCollator implements Collator<Map.Entry<String, Map<String, Integer>>, Map<String, List<String>>> {
    private final IMap<String, String> codeInfraction;

    public PopularInfractionsCollator(IMap<String, String> codeInfraction) {
        this.codeInfraction = codeInfraction;
    }

    @Override
    public Map<String, List<String>> collate(Iterable<Map.Entry<String, Map<String, Integer>>> values) {
        Map<String, List<String>> result = new TreeMap<>();

        for (Map.Entry<String, Map<String, Integer>> entry : values) {
            String county = entry.getKey();
            Map<String, Integer> infractionCounts = entry.getValue();

            List<String> topInfractions = infractionCounts.entrySet().stream()
                    .sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue()))
                    .limit(3)
                    .map(e -> codeInfraction.getOrDefault(e.getKey(), "-"))
                    .collect(Collectors.toList());

            while (topInfractions.size() < 3) {
                topInfractions.add("-");
            }

            result.put(county, topInfractions);
        }

        return result;
    }
}
