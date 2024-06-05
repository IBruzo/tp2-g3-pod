package org.example.query3;

import com.hazelcast.mapreduce.Context;
import com.hazelcast.mapreduce.Mapper;
import org.example.models.Infraction;

import java.util.Set;

public class InfractionPercentageMapper implements Mapper<String, Infraction, String, Double> {

    private static final String TOTAL_FINES = "total_fines";


    @Override
    public void map(String s, Infraction infraction, Context<String, Double> context) {

                context.emit(infraction.getUnitDescription(),infraction.getFineAmount());
                context.emit(TOTAL_FINES,infraction.getFineAmount());
    }
}
