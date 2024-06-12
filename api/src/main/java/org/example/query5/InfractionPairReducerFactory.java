package org.example.query5;

import com.hazelcast.mapreduce.Reducer;
import com.hazelcast.mapreduce.ReducerFactory;
import org.example.models.Pair;

public class InfractionPairReducerFactory implements ReducerFactory<String, Pair<Integer, Double>, Double> {

    @Override
    public Reducer<Pair<Integer, Double>, Double> newReducer(String key) {
        return new InfractionPairReducer();
    }


    private class InfractionPairReducer extends Reducer<Pair<Integer, Double>, Double> {
        private int count;
        private double totalSum;

        @Override
        public void beginReduce() {
            count = 0;
            totalSum = 0;
        }

        @Override
        public void reduce(Pair<Integer, Double> fineAmount) {
            count+= fineAmount.getFirst();
            totalSum += fineAmount.getSecond();
        }

        @Override
        public Double finalizeReduce() {
            return totalSum/count;
        }
    }
}