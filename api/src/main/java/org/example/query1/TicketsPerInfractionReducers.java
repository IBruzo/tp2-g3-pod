package org.example.query1;

import com.hazelcast.mapreduce.Reducer;
import com.hazelcast.mapreduce.ReducerFactory;

public class TicketsPerInfractionReducers {
    public static class TicketsPerInfractionReducerFactory implements ReducerFactory<String, Integer, Integer> {
        @Override
        public Reducer<Integer, Integer> newReducer(String key) {
            return new TicketsPerInfractionReducer();
        }
    }

    public static class TicketsPerInfractionReducer extends Reducer<Integer, Integer> {
        private int count;

        @Override
        public void beginReduce() {
            count = 0;
        }

        @Override
        public void reduce(Integer value) {
            count += value;
        }

        @Override
        public Integer finalizeReduce() {
            return count;
        }
    }
}
