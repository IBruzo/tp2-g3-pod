package org.example.client;

import com.hazelcast.core.IMap;
import lombok.Cleanup;
import org.example.models.Infraction;
import org.example.models.Pair;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.IOException;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class DocumentUtils {

    private static final String CSV_FILE = "tickets";
    private static final String CSV_CODES = "infractions";

    public void readCSV(IMap<String, Infraction> infractionMap, IMap<String, String> codeInfraction, String cityCode,
            String inPath) {

        try (BufferedReader br = new BufferedReader(new FileReader(inPath + CSV_FILE + cityCode + ".csv"))) {
            String line = br.readLine();
            if (line == null) {
                System.out.println("Empty CSV");
                return;
            }

            String[] header = line.split(";");

            int[] indexes = { 0, 0, 0, 0, 0, 0 }; // ORDEN DE LOS INDICES CON LOS CAMPOS: date plate violation agency

            for (int i = 0; i <= 5; i++) {
                header[i] = header[i].toLowerCase();
                if (header[i].contains("date")) {
                    indexes[0] = i;
                } else if (header[i].contains("plate")) {
                    indexes[1] = i;
                } else if (header[i].contains("code")) {
                    indexes[2] = i;
                } else if (header[i].equals("unit_description") || header[i].equals("issuing agency")) {
                    indexes[3] = i;
                } else if (header[i].contains("fine")) {
                    indexes[4] = i;
                } else if (header[i].equals("community_area_name") || header[i].equals("county name")) {
                    indexes[5] = i;
                }
            }

            infractionMap.putAll(parseInfractionsFile(inPath + CSV_FILE + cityCode + ".csv",indexes));
//         infractionList.addAll(  parseInfractionsFile(inPath + CSV_FILE + cityCode + ".csv",indexes));

        } catch (IOException e) {
            e.printStackTrace();
        }

        try (BufferedReader br = new BufferedReader(new FileReader(inPath + CSV_CODES + cityCode + ".csv"))) {
            String line = br.readLine();
            if (line == null) {
                System.out.println("Empty CSV");
                return;
            }
            while ((line = br.readLine()) != null) {
                String[] values = line.split(";");
                if (values.length == 2) {
                    String code = values[0];
                    String description = values[1];
                    codeInfraction.put(code, description);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Map<String, Infraction> parseInfractionsFile(String path, int[] indexes) throws IOException {
        Map<String, Infraction> infractionMap = new HashMap<>();
        AtomicInteger csvLines = new AtomicInteger(0);

        try (var lines = Files.lines(Path.of(path))) {
            infractionMap = lines
                    .skip(1) // Skip the header
                    .limit(1000) // comentar para dejar que fluya
                    .map(line -> {
                        String[] values = line.split(";");
                        LocalDate date = null; // Adjust index as necessary
                        try {
                            date = parseDate(values[indexes[0]]);
                        } catch (ParseException e) {
                            throw new RuntimeException(e);
                        }
                        String licensePlateNumber = values[indexes[1]];
                        String violationCode = values[indexes[2]];
                        String unitDescription = values[indexes[3]];
                        String fineAmount = values[indexes[4]];
                        String communityAreaName = values[indexes[5]];

                        Infraction infraction = new Infraction(date, licensePlateNumber, violationCode, unitDescription,
                                communityAreaName, Double.parseDouble(fineAmount));

                        String key = "infraction-" + csvLines.getAndIncrement();
                        return Map.entry(key, infraction);
                    })
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        }
        return infractionMap;
    }

    private static LocalDate parseDate(String dateString) throws ParseException {
        if (dateString.contains(":")) {
            dateString = dateString.substring(0, 10);
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        try {
            return LocalDate.parse(dateString, formatter);
        } catch (DateTimeParseException e) {
            System.err.println("Invalid date format: " + dateString);
            return null;
        }
    }

    public static void writeQuery1CSV(String path, Map<String, Integer> data) throws IOException {
        @Cleanup
        BufferedWriter writer = Files.newBufferedWriter(
                Path.of(path),
                StandardOpenOption.CREATE,
                StandardOpenOption.WRITE);

        writer.write("Infraction;Tickets\n");
        for (Map.Entry<String, Integer> entry : data.entrySet()) {
            writer.write(entry.getKey() + ";" + entry.getValue() + "\n");
        }
    }

    public static void writeQuery2CSV(String path, Map<String, List<String>> data) throws IOException {
        @Cleanup
        BufferedWriter writer = Files.newBufferedWriter(
                Path.of(path),
                StandardOpenOption.CREATE,
                StandardOpenOption.WRITE);

        writer.write("County;InfractionTop1;InfractionTop2;InfractionTop3\n");
        for (Map.Entry<String, List<String>> entry : data.entrySet()) {
            StringBuilder line = new StringBuilder();
            line.append(entry.getKey()).append(";");
            for (String infraction : entry.getValue()) {
               line.append(infraction).append(";");
            }
            line.deleteCharAt(line.length() - 1);
            line.append("\n");
            writer.write(line.toString());
        }
    }

    public static void writeQuery5CSV(String path, Map<Integer, List<Pair<String, String>>> data) throws IOException {
        @Cleanup
        BufferedWriter writer = Files.newBufferedWriter(
                Path.of(path),
                StandardOpenOption.CREATE,
                StandardOpenOption.WRITE);

        writer.write("Group;Infraction A;Infraction B\n");
        for (Map.Entry<Integer, List<Pair<String, String>>> entry : data.entrySet()) {
            for (Pair<String, String> pair : entry.getValue()) {
                writer.write(entry.getKey() + ";" + pair.getFirst() + ";" + pair.getSecond() + "\n");
            }
        }
    }

    public static void writeQuery3CSV(String path, List<Pair<String,Double>> data) throws IOException {
        @Cleanup
        BufferedWriter writer = Files.newBufferedWriter(
                Path.of(path),
                StandardOpenOption.CREATE,
                StandardOpenOption.WRITE);
        DecimalFormat df = new DecimalFormat();
        df.setMaximumFractionDigits(2);
        df.setRoundingMode(RoundingMode.DOWN);

        writer.write("Issuing Agency;Percentage\n");

        for (Pair<String, Double> entry : data) {
            writer.write(entry.getFirst() + ";" + df.format( entry.getSecond()) + "%" + "\n");
        }
    }

    public static void writeQuery4CSV(String path, List<String> data) throws IOException {
        @Cleanup
        BufferedWriter writer = Files.newBufferedWriter(
                Path.of(path),
                StandardOpenOption.CREATE,
                StandardOpenOption.WRITE);

        writer.write("Neighborhood;License Plate;Tickets\n");
        for (String entry : data) {
            writer.write(entry + "\n");
        }
    }

    public static void writeTimeToFile(int queryNumber, String message, String path) throws IOException {
        String fileName = path + "time" + queryNumber + ".txt";
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss:SSSS");
        String formattedTime = LocalDateTime.now().format(formatter);
        try (var writer = Files.newBufferedWriter(Path.of(fileName), StandardOpenOption.CREATE, StandardOpenOption.APPEND)) {
            writer.write( formattedTime + " - " + message + "\n");
        }
    }
}
