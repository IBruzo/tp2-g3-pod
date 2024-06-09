package org.example.query2;

import com.hazelcast.core.IMap;
import com.hazelcast.mapreduce.Collator;

import java.util.*;

public class PopularInfractionsCollator implements Collator<Map.Entry<String, Map<String, Integer>>, Map<String, List<String>>> {
    IMap<String, String> codeInfraction;

    public PopularInfractionsCollator(IMap<String, String> codeInfraction) {
        this.codeInfraction = codeInfraction;
    }

    @Override
    public Map<String, List<String>> collate(Iterable<Map.Entry<String, Map<String, Integer>>> reducedResults) {
        Map<String, List<String>> topInfractionsPerCounty = new TreeMap<>();

        for (Map.Entry<String, Map<String, Integer>> entry : reducedResults) {
            String county = entry.getKey();
            Map<String, Integer> infractionCounts = entry.getValue();

            PriorityQueue<Map.Entry<String, Integer>> topInfractions = new PriorityQueue<>(
                    Comparator.comparingInt(Map.Entry::getValue)
            );

            for (Map.Entry<String, Integer> infractionEntry : infractionCounts.entrySet()) {
                topInfractions.offer(infractionEntry);
                if (topInfractions.size() > 3) {
                    topInfractions.poll();
                }
            }

            List<String> top3Infractions = new ArrayList<>();
            while (!topInfractions.isEmpty()) {
                top3Infractions.add(codeInfraction.get(topInfractions.poll().getKey()));
            }

            Collections.reverse(top3Infractions);

            while (top3Infractions.size() < 3) {
                top3Infractions.add("-");
            }

            topInfractionsPerCounty.put(county, top3Infractions);
        }

        return topInfractionsPerCounty;
    }
}
