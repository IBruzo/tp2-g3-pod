package org.example.query1;

import com.hazelcast.core.IMap;
import com.hazelcast.mapreduce.Collator;

import java.util.*;

@SuppressWarnings("deprecation")
public class TicketsPerInfractionCollator implements Collator<Map.Entry<String, Integer>, Map<String, Integer>> {
    IMap<String, String> codeInfraction;

    public TicketsPerInfractionCollator(IMap<String, String> codeInfraction) {
        this.codeInfraction = codeInfraction;
    }

    @Override
    public Map<String, Integer> collate(Iterable<Map.Entry<String, Integer>> iterable) {
        Map<String, Integer> filteredInfractions = new HashMap<>();
        for (Map.Entry<String, Integer> entry : iterable) {
            if (entry.getValue() > 0) {
                filteredInfractions.put(codeInfraction.get(entry.getKey()), entry.getValue());
            }
        }

        List<Map.Entry<String, Integer>> sortedFilteredInfractions = new ArrayList<>(filteredInfractions.entrySet());
        sortedFilteredInfractions.sort((e1, e2) -> {
            int quantityComparison = e2.getValue().compareTo(e1.getValue());
            if (quantityComparison == 0) {
                // if the infractions have the same amount of tickets, tie break using
                // alphabetical order
                return e1.getKey().compareTo(e2.getKey());
            }
            return quantityComparison;
        });

        Map<String, Integer> result = new LinkedHashMap<>();
        for (Map.Entry<String, Integer> entry : sortedFilteredInfractions) {
            result.put(entry.getKey(), entry.getValue());
        }
        return result;
    }
}