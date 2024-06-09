package org.example.query5;

import com.hazelcast.mapreduce.Context;
import com.hazelcast.mapreduce.Mapper;


@SuppressWarnings("deprecation")
public class BracketInfractionMapper implements Mapper<String, Double, Integer, String> {
    @Override
    public void map(String key, Double value, Context<Integer, String> context) {
        int number = value == null ? 0 : value.intValue();;
        if(number >= 100) {
            int group = (number / 100) * 100;
            context.emit(group, key);
        }
    }
}