package org.example.query2;

import com.hazelcast.mapreduce.Reducer;
import com.hazelcast.mapreduce.ReducerFactory;
import org.example.models.Pair;

import java.util.ArrayList;
import java.util.List;


public class PopularInfractionsReducerFactory implements ReducerFactory<String, Pair<String, Integer>, List<Pair<String, Integer>>> {

    @Override
    public Reducer<Pair<String, Integer>, List<Pair<String, Integer>>> newReducer(String key) {
        return new PopularInfractionsReducer();
    }


    private class PopularInfractionsReducer extends Reducer<Pair<String, Integer>, List<Pair<String, Integer>>> {
        private List<Pair<String, Integer>> result = new ArrayList<Pair<String, Integer>>();
        @Override
        public void beginReduce() {
            result = new ArrayList<Pair<String, Integer>>();
        }

        @Override
        public void reduce(Pair<String, Integer> val) {
            System.out.println(val);
          for(Pair<String, Integer> pair : result){
              if(pair.getFirst().equals(val.getFirst())){
                  pair.setSecond(pair.getSecond()+1);
                  return;
              }
          }
          result.add(val);

        }

        @Override
        public List<Pair<String, Integer>> finalizeReduce() {
            result.sort((e1, e2) -> e2.getSecond().compareTo(e1.getSecond()));
            return result;
        }
    }
}
