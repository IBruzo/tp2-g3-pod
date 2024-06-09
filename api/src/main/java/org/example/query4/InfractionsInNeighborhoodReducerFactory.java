package org.example.query4;

import com.hazelcast.mapreduce.Reducer;
import com.hazelcast.mapreduce.ReducerFactory;
import org.example.models.Pair;

import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("deprecation")
public class InfractionsInNeighborhoodReducerFactory implements ReducerFactory<String, Pair<String,Integer>, Map<String,Integer>> {
    @Override
    public Reducer<Pair<String,Integer>, Map<String,Integer>> newReducer(String key) {
        return new InfractionsInNeighborhoodReducer();
    }

    private class InfractionsInNeighborhoodReducer extends Reducer<Pair<String,Integer>, Map<String,Integer>> {
        private Map<String,Integer> result = new HashMap<>();

        @Override
        public void beginReduce() {
            result = new HashMap<>();
        }

        @Override
        public void reduce(Pair<String,Integer> value) {
            if (result.containsKey(value.getFirst())) {
                result.put(value.getFirst(), result.get(value.getFirst()) + value.getSecond());
            } else {
                result.put(value.getFirst(), value.getSecond());
            }
        }

        @Override
        public Map<String, Integer> finalizeReduce() {
            return result;
        }
    }
}
