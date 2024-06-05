package org.example.query3;

import com.hazelcast.mapreduce.Collator;
import org.example.models.Pair;


import java.util.*;

public class InfractionPercentageCollator implements Collator<Map.Entry<String, Double>,  List<Pair<String,Double>>> {

    private static final String TOTAL_FINES = "total_fines";

    @Override
    public  List<Pair<String,Double>> collate(Iterable<Map.Entry<String, Double>> iterable) {
        List<Pair<String,Double>> result = new ArrayList<>();
        double total = 0;

        for(Map.Entry<String, Double> entry : iterable) {
            if(entry.getKey().equals(TOTAL_FINES)) {
                total = entry.getValue();
            }
        }

        for(Map.Entry<String, Double> entry : iterable) {
            if (!entry.getKey().equals(TOTAL_FINES)) {
            result.add(new Pair<>(entry.getKey(),( entry.getValue() / total)*100));
            }
        }

        result.sort((e1, e2) -> e2.getSecond().compareTo(e1.getSecond()));

        return result;
    }
}

