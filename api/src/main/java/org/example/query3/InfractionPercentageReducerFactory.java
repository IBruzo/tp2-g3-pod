package org.example.query3;

import com.hazelcast.mapreduce.Reducer;
import com.hazelcast.mapreduce.ReducerFactory;

public class InfractionPercentageReducerFactory  implements ReducerFactory<String, Double, Double> {
    @Override
    public Reducer<Double, Double> newReducer(String s) {
        return new InfractionPercentageReducer();
    }

    private class InfractionPercentageReducer extends Reducer<Double, Double>{

        private double totalFines;


        @Override
        public void beginReduce() {

            totalFines = 0;
        }

        @Override
        public void reduce(Double fineAmount) {

            totalFines += fineAmount;
        }

        @Override
        public Double finalizeReduce() {

            return totalFines;
        }
    }
}
