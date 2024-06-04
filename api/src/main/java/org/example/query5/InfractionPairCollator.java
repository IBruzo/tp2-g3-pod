package org.example.query5;

import com.hazelcast.core.IMap;
import com.hazelcast.mapreduce.Collator;
import org.example.models.Pair;

import java.util.*;

public class InfractionPairCollator implements Collator<Map.Entry<String, Double>, Map<Integer, List<Pair<String, String>>>> {
    IMap<String, String> codeInfraction;

    public InfractionPairCollator(IMap<String, String> codeInfraction) {
        this.codeInfraction = codeInfraction;
    }

    @Override
    public Map<Integer, List<Pair<String, String>>> collate(Iterable<Map.Entry<String, Double>> iterable) {
        Map<Integer, List<String>> groupedInfractions = new HashMap<>();
        for(Map.Entry<String, Double> entry : iterable) {
            double number = entry.getValue();
            if(number >= 100) {
                int group = ((int) number / 100) * 100;
                groupedInfractions.computeIfAbsent(group, k -> new ArrayList<>());
                groupedInfractions.get(group).add(codeInfraction.get(entry.getKey()));
            }
        }

// Sort the groups in descending order
        List<Map.Entry<Integer, List<String>>> sortedGroups = new ArrayList<>(groupedInfractions.entrySet());
        sortedGroups.sort((e1, e2) -> e2.getKey().compareTo(e1.getKey()));

        // Create the result map
        Map<Integer, List<Pair<String, String>>> result = new LinkedHashMap<>();
        for (Map.Entry<Integer, List<String>> groupEntry : sortedGroups) {
            List<String> infractions = groupEntry.getValue();
            Collections.sort(infractions); // Sort infractions alphabetically within the group

            List<Pair<String, String>> pairs = new ArrayList<>();
            for (int i = 0; i < infractions.size(); i++) {
                for (int j = i + 1; j < infractions.size(); j++) {
                    pairs.add(new Pair<>(infractions.get(i), infractions.get(j)));
                }
            }
            if (!pairs.isEmpty()) {
                result.put(groupEntry.getKey(), pairs);
            }
        }
        return result;
    }
}