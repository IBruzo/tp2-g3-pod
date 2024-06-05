package org.example.query2;

import com.hazelcast.core.IMap;
import com.hazelcast.mapreduce.Collator;
import org.example.models.Pair;

import java.util.*;

public class PopularInfractionsCollator  implements Collator<Map.Entry<String, List<Pair<String,Integer>>>, Map<String , List<String>>> {
    IMap<String, String> codeInfraction;

    public PopularInfractionsCollator(IMap<String, String> codeInfraction) {
        this.codeInfraction = codeInfraction;
    }

    @Override
    public Map<String, List<String>> collate(Iterable<Map.Entry<String, List<Pair<String, Integer>>>> iterable) {
        Map<String , List<String>> resp = new TreeMap<>();

       for(Map.Entry<String, List<Pair<String, Integer>>> entry : iterable) {
           List<Pair<String, Integer>> pairs = entry.getValue();
           List<String> top3 = new ArrayList<>();

           for(int i = 0 ; i < 3 ; i++) {

               if(pairs.size() <= i) {
                   top3.add("-");
               }else{
                   top3.add(codeInfraction.get( pairs.get(i).getFirst()));
               }
           }
           resp.put(entry.getKey(), top3);
       }

       return resp;
    }
}

