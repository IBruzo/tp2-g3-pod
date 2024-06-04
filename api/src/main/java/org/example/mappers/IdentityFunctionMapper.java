package org.example.mappers;

import org.example.models.Infraction;

import com.hazelcast.mapreduce.Context;
import com.hazelcast.mapreduce.Mapper;

public class IdentityFunctionMapper implements Mapper<String, Infraction, String, Infraction> {

    @Override
    public void map(String key, Infraction value, Context<String, Infraction> context) {
        context.emit(key, value);
    }
}
