package org.example.query4;

import com.hazelcast.mapreduce.Collator;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@SuppressWarnings("deprecation")
public class InfractionsInNeighborhoodCollator implements Collator<Map.Entry<String, Integer>, List<String>> {
    @Override
    public List<String> collate(Iterable<Map.Entry<String, Integer>> values) {
        Map<String, Map.Entry<String, Integer>> neighborhoodMaxInfractions = new HashMap<>();

        for (Map.Entry<String, Integer> entry : values) {
            String[] parts = entry.getKey().split(";");
            String neighborhood = parts[0];
            String licensePlate = parts[1];
            Integer count = entry.getValue();

            Map.Entry<String, Integer> newEntry = Map.entry(licensePlate, count);

            Map.Entry<String, Integer> currentMaxEntry = neighborhoodMaxInfractions.get(neighborhood);
            if (currentMaxEntry == null || count > currentMaxEntry.getValue()) {
                neighborhoodMaxInfractions.put(neighborhood, newEntry);
            }
        }

        return neighborhoodMaxInfractions.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(e -> String.format("%s;%s;%d", e.getKey(), e.getValue().getKey(), e.getValue().getValue()))
                .collect(Collectors.toList());
    }
}
