package org.example.query5;

import com.hazelcast.core.IMap;
import com.hazelcast.mapreduce.Collator;
import org.example.models.Pair;

import java.util.*;

public class BracketInfractionCollator implements Collator<Map.Entry<Integer, List<String>>, Map<Integer, List<Pair<String, String>>>> {

    @Override
    public Map<Integer, List<Pair<String, String>>> collate(Iterable<Map.Entry<Integer, List<String>>> iterable) {
        // Create a list to hold all entries
        List<Map.Entry<Integer, List<String>>> entryList = new ArrayList<>();
        iterable.forEach(entryList::add);

        // Sort the entries by their keys in descending order
        entryList.sort((e1, e2) -> e2.getKey().compareTo(e1.getKey()));

        // Create the result map with the keys in descending order
        Map<Integer, List<Pair<String, String>>> result = new LinkedHashMap<>();

        for (Map.Entry<Integer, List<String>> entry : entryList) {
            // Sort the infractions alphabetically within the group
            List<String> infractions = entry.getValue();
            Collections.sort(infractions);

            // Create the pairs of infractions
            List<Pair<String, String>> pairs = new ArrayList<>();
            for (int i = 0; i < infractions.size(); i++) {
                for (int j = i + 1; j < infractions.size(); j++) {
                    pairs.add(new Pair<>(infractions.get(i), infractions.get(j)));
                }
            }
            if (!pairs.isEmpty()) {
                result.put(entry.getKey(), pairs);
            }
        }

        return result;
    }
}