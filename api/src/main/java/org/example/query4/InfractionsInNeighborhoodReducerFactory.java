package org.example.query4;

import com.hazelcast.mapreduce.Reducer;
import com.hazelcast.mapreduce.ReducerFactory;
import org.example.models.Pair;

import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("deprecation")
public class InfractionsInNeighborhoodReducerFactory implements ReducerFactory<String, Map<String, Integer>, Map<String,Integer>> {
    @Override
    public Reducer<Map<String, Integer>, Map<String,Integer>> newReducer(String key) {
        return new InfractionsInNeighborhoodReducer();
    }

    private class InfractionsInNeighborhoodReducer extends Reducer<Map<String, Integer>, Map<String,Integer>> {
        private Map<String,Integer> result = new HashMap<>();

        @Override
        public void beginReduce() {
            result = new HashMap<>();
        }

        @Override
        public void reduce(Map<String, Integer> value) {
            value.forEach((k, v) -> result.merge(k, v, Integer::sum));
        }

        @Override
        public Map<String, Integer> finalizeReduce() {
            return result;
        }
    }
}
