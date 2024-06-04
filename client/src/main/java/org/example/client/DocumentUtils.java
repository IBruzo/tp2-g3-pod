package org.example.client;

import com.hazelcast.core.IList;
import com.hazelcast.core.IMap;
import lombok.Cleanup;
import org.example.models.Infraction;
import org.example.models.Pair;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class DocumentUtils {

    private static final String CSV_FILE = "tickets";
    private static final String CSV_CODES = "infractions";

    public void readCSV(IList<Infraction> infractionList, IMap<String, String> codeInfraction, String cityCode,
            String inPath) {

        try (BufferedReader br = new BufferedReader(new FileReader(inPath + CSV_FILE + cityCode + ".csv"))) {
            String line = br.readLine();
            if (line == null) {
                System.out.println("Empty CSV");
                return;
            }

            String[] header = line.split(";");

            int[] indexes = { 0, 0, 0, 0, 0, 0 }; // ORDEN DE LOS INDICES CON LOS CAMPOS: date plate violation agency
                                                  // amount community
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

            int csvLines = 0;
            while ((line = br.readLine()) != null && csvLines < 1000) {
                String[] values = line.split(";");
                LocalDate date = parseDate(values[indexes[0]]); // arreglar? values[0] es asi: 2020-01-01 00:00:00
                String licensePlateNumber = values[indexes[1]];
                String violationCode = values[indexes[2]];
                String unitDescription = values[indexes[3]];
                String fineAmount = values[indexes[4]];
                String communityAreaName = values[indexes[5]];

                Infraction infraction = new Infraction(date, licensePlateNumber, violationCode, unitDescription,
                        communityAreaName, Double.parseDouble(fineAmount));

                infractionList.add(infraction);
                csvLines++;
            }
        } catch (IOException | ParseException e) {
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

    public static void writeQuery2CSV(String path, Map<Integer, List<Pair<String, String>>> data) throws IOException {
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

}
