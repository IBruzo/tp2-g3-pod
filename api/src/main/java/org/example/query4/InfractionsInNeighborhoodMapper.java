package org.example.query4;

import com.hazelcast.mapreduce.Context;
import com.hazelcast.mapreduce.Mapper;
import org.example.models.Infraction;
import org.example.models.Pair;
import org.example.models.Q4Infraction;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class InfractionsInNeighborhoodMapper implements Mapper<String, Q4Infraction, String, Pair<String,Integer>> {
    private final LocalDate from;
    private final LocalDate to;

    public InfractionsInNeighborhoodMapper(String from, String to) {
        System.out.println("Mapper created");
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        this.from = LocalDate.parse(from, formatter);
        this.to = LocalDate.parse(to, formatter);
    }

    @Override
    public void map(String key, Q4Infraction infraction, Context<String, Pair<String,Integer>> context) {
        LocalDate infractionDate = infraction.getInfractionDate();
        if (infractionDate != null && !infractionDate.isBefore(from) && !infractionDate.isAfter(to)) {
            context.emit(infraction.getCommunityAreaName(), new Pair<>(infraction.getLicensePlateNumber(),1));
        }
    }
}