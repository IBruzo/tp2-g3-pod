package org.example.query2;

import com.hazelcast.mapreduce.Reducer;
import com.hazelcast.mapreduce.ReducerFactory;
import org.example.models.Pair;

import java.util.HashMap;
import java.util.Map;


public class PopularInfractionsReducerFactory implements ReducerFactory<String, Pair<String, Integer>, Map<String, Integer>> {

    @Override
    public Reducer<Pair<String, Integer>, Map<String, Integer>> newReducer(String key) {
        return new PopularInfractionsReducer();
    }


    private class PopularInfractionsReducer extends Reducer<Pair<String, Integer>, Map<String, Integer>> {
        private Map<String, Integer> result = new  HashMap<>();
        @Override
        public void beginReduce() {
            result = new HashMap<>();
        }

        @Override
        public void reduce(Pair<String, Integer> val) {
           if (result.containsKey(val.getFirst())) {
               result.put(val.getFirst(), result.get(val.getFirst()) + val.getSecond());
           }else{
               result.put(val.getFirst(), val.getSecond());
           }

        }

        @Override
        public Map<String, Integer> finalizeReduce() {
            return result;
        }
    }
}
