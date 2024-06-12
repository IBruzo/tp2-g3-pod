package org.example.query5;

import com.hazelcast.mapreduce.Combiner;
import com.hazelcast.mapreduce.CombinerFactory;
import org.example.models.Pair;


public class InfractionPairCombinerFactory implements CombinerFactory<String, Double, Pair<Integer, Double>> {

    @Override
    public Combiner<Double, Pair<Integer, Double>> newCombiner(String key) {
        return new InfractionPairCombiner();
    }

    private class InfractionPairCombiner extends Combiner<Double, Pair<Integer, Double>> {
        private int count;
        private double totalSum;

        @Override
        public void beginCombine() {
            count = 0;
            totalSum = 0;
        }

        @Override
        public void combine(Double fineAmount) {
            count++;
            totalSum += fineAmount;
        }

        @Override
        public Pair<Integer, Double> finalizeChunk() {
            return new Pair<>(count, totalSum);
        }
    }
}
