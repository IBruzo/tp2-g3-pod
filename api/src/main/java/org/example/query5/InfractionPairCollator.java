package org.example.query5;

import com.hazelcast.core.IMap;
import com.hazelcast.mapreduce.Collator;

import java.util.*;

public class InfractionPairCollator implements Collator<Map.Entry<String, Double>, Map<String, Double>> {
    IMap<String, String> codeInfraction;

    public InfractionPairCollator(IMap<String, String> codeInfraction) {
        this.codeInfraction = codeInfraction;
    }

    @Override
    public Map<String, Double> collate(Iterable<Map.Entry<String, Double>> iterable) {
        // Converts the infraction codes to their names
        Map<String, Double> result = new HashMap<>();
        for (Map.Entry<String, Double> entry : iterable) {
            result.put(codeInfraction.get(entry.getKey()), entry.getValue());
        }

        return result;
    }
}