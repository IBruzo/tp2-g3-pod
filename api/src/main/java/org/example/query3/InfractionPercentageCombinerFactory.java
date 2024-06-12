package org.example.query3;

import com.hazelcast.mapreduce.Combiner;
import com.hazelcast.mapreduce.CombinerFactory;
import com.hazelcast.mapreduce.Reducer;

public class InfractionPercentageCombinerFactory implements CombinerFactory<String, Double, Double> {
    @Override
    public Combiner<Double, Double> newCombiner(String s) {
        return new InfractionPercentageCombiner();
    }

    private class InfractionPercentageCombiner extends Combiner<Double, Double> {

        private double totalFines;


        @Override
        public void beginCombine() {
            totalFines = 0;
        }

        @Override
        public void combine(Double value) {
            totalFines += value;
        }

        @Override
        public Double finalizeChunk() {
            return totalFines;
        }
    }
}
