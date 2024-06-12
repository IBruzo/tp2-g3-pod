package org.example.query1;

import com.hazelcast.mapreduce.Combiner;
import com.hazelcast.mapreduce.CombinerFactory;

public class TicketsPerInfractionCombinerFactory implements CombinerFactory<String, Integer, Integer> {

    @Override
    public Combiner<Integer, Integer> newCombiner(String key) {
        return new TicketsPerInfractionCombiner();
    }

    private class TicketsPerInfractionCombiner extends Combiner<Integer, Integer> {
        private int count;

        @Override
        public void beginCombine() {
            count = 0;
        }

        @Override
        public void combine(Integer value) {
            count += value;
        }

        @Override
        public Integer finalizeChunk() {
            return count;
        }
    }
}
