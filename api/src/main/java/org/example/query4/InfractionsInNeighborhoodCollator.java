package org.example.query4;

import com.hazelcast.mapreduce.Collator;
import org.example.models.Pair;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@SuppressWarnings("deprecation")
public class InfractionsInNeighborhoodCollator implements Collator<Map.Entry<String, Map<String, Integer>>, List<String>> {
    @Override
    public List<String> collate(Iterable<Map.Entry<String, Map<String, Integer>>> values) {
        return StreamSupport.stream(values.spliterator(), false)
                .map(entry -> {
                    String neighborhood = entry.getKey();
                    Map<String, Integer> licensePlateCounts = entry.getValue();
                    return licensePlateCounts.entrySet().stream()
                            .max(Map.Entry.comparingByValue())
                            .map(maxEntry -> String.format("%s;%s;%d", neighborhood, maxEntry.getKey(), maxEntry.getValue()))
                            .orElse(null);
                })
                .filter(result -> result != null)
                .sorted()
                .collect(Collectors.toList());
    }
}
