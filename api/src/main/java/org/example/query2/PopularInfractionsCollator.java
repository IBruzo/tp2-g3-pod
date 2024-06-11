package org.example.query2;

import com.hazelcast.core.IMap;
import com.hazelcast.mapreduce.Collator;

import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class PopularInfractionsCollator implements Collator<Map.Entry<String, Map<String, Integer>>, List<String>> {
    private final IMap<String, String> codeInfraction;

    public PopularInfractionsCollator(IMap<String, String> codeInfraction) {
        this.codeInfraction = codeInfraction;
    }

    @Override
    public List<String> collate(Iterable<Map.Entry<String, Map<String, Integer>>> values) {
        return StreamSupport.stream(values.spliterator(), false)
                .map(entry -> {
                    String county = entry.getKey();
                    Map<String, Integer> typesMap = entry.getValue();

                    List<String> top3Types = typesMap.entrySet().stream()
                            .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                            .limit(3)
                            .map(e -> codeInfraction.getOrDefault(e.getKey(), "-"))
                            .collect(Collectors.toList());

                    while (top3Types.size() < 3) {
                        top3Types.add("-");
                    }

                    return String.format("%s;%s;%s;%s", county, top3Types.get(0), top3Types.get(1), top3Types.get(2));
                })
                .filter(result -> result != null)
                .sorted()
                .collect(Collectors.toList());
    }
}
