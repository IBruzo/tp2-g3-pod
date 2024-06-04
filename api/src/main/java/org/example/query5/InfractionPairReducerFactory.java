package org.example.query5;

import com.hazelcast.mapreduce.Reducer;
import com.hazelcast.mapreduce.ReducerFactory;

public class InfractionPairReducerFactory implements ReducerFactory<String, Double, Double> {

    @Override
    public Reducer<Double, Double> newReducer(String key) {
        return new InfractionPairReducer();
    }


    private class InfractionPairReducer extends Reducer<Double, Double> {
        private int count;
        private double totalSum;

        @Override
        public void beginReduce() {
            count = 0;
            totalSum = 0;
        }

        @Override
        public void reduce(Double fineAmount) {
            count++;
            totalSum += fineAmount;
        }

        @Override
        public Double finalizeReduce() {
            return totalSum/count;
        }
    }
}