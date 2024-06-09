package org.example.query4;

import com.hazelcast.mapreduce.Collator;
import org.example.models.Pair;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@SuppressWarnings("deprecation")
public class InfractionsInNeighborhoodCollator implements Collator<Map.Entry<Pair<String, String>, Integer>, List<String>> {
    @Override
    public List<String> collate(Iterable<Map.Entry<Pair<String, String>, Integer>> values) {
        System.out.println("Collating");
        Map<String, Map.Entry<String, Integer>> neighborhoodMaxInfractions = new HashMap<>();

        for (Map.Entry<Pair<String, String>, Integer> entry : values) {
            Pair<String, String> pair = entry.getKey();
            String neighborhood = pair.getFirst();
            String licensePlate = pair.getSecond();
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
