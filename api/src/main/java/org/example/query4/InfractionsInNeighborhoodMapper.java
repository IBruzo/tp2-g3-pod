package org.example.query4;

import com.hazelcast.mapreduce.Context;
import com.hazelcast.mapreduce.Mapper;
import org.example.models.Infraction;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@SuppressWarnings("deprecation")
public class InfractionsInNeighborhoodMapper implements Mapper<String, Infraction, String, Integer> {
    private final LocalDate from;
    private final LocalDate to;

    public InfractionsInNeighborhoodMapper(String from, String to) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        this.from = LocalDate.parse(from, formatter);
        this.to = LocalDate.parse(to, formatter);
    }

    @Override
    public void map(String key, Infraction infraction, Context<String, Integer> context) {
        LocalDate infractionDate = infraction.getInfractionDate();
        if (!infractionDate.isBefore(from) && !infractionDate.isAfter(to)) {
            String compositeKey = infraction.getCommunityAreaName() + ";" + infraction.getLicensePlateNumber();
            context.emit(compositeKey, 1);
        }
    }
}