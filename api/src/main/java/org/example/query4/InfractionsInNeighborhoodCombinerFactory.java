package org.example.query4;

import com.hazelcast.mapreduce.Combiner;
import com.hazelcast.mapreduce.CombinerFactory;
import org.example.models.Pair;

import java.util.HashMap;
import java.util.Map;

public class InfractionsInNeighborhoodCombinerFactory implements CombinerFactory<String, Pair<String, Integer>, Map<String, Integer>> {

    @Override
    public Combiner<Pair<String, Integer>, Map<String, Integer>> newCombiner(String key) {
        return new InfractionsInNeighborhoobCombiner();
    }

    private class InfractionsInNeighborhoobCombiner extends Combiner<Pair<String, Integer>, Map<String, Integer>> {
        private Map<String, Integer> localAggregation = new HashMap<>();

        @Override
        public void combine(Pair<String, Integer> value) {
            localAggregation.merge(value.getFirst(), value.getSecond(), Integer::sum);
        }

        @Override
        public Map<String, Integer> finalizeChunk() {
            Map<String, Integer> chunkResult = new HashMap<>(localAggregation);
            localAggregation.clear();
            return chunkResult;
        }
    }

}



