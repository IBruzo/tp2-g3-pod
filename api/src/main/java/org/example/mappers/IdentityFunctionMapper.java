package org.example.mappers;

import java.util.StringTokenizer;

import org.example.models.InfractionChicago;

import com.hazelcast.mapreduce.Context;
import com.hazelcast.mapreduce.Mapper;

public class IdentityFunctionMapper implements Mapper<String, InfractionChicago, String, InfractionChicago> {

    @Override
    public void map(String key, InfractionChicago value, Context<String, InfractionChicago> context) {
        context.emit(key, value);
    }
}
