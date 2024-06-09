package org.example.query4;

import com.hazelcast.mapreduce.Reducer;
import com.hazelcast.mapreduce.ReducerFactory;
import org.example.models.Pair;

@SuppressWarnings("deprecation")
public class InfractionsInNeighborhoodReducerFactory implements ReducerFactory<Pair<String,String>, Integer, Integer> {
    @Override
    public Reducer<Integer, Integer> newReducer(Pair<String,String> key) {
        return new InfractionsInNeighborhoodReducer();
    }

    private class InfractionsInNeighborhoodReducer extends Reducer<Integer, Integer> {
        private int sum;

        @Override
        public void beginReduce() {
            System.out.println("Reducer created");
            sum = 0;
        }

        @Override
        public void reduce(Integer value) {
            sum += value;
        }

        @Override
        public Integer finalizeReduce() {
            return sum;
        }
    }
}
