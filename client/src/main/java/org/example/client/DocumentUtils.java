package org.example.client;

import com.hazelcast.core.IMap;
import org.example.models.Infraction;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

public class DocumentUtils {

    private static final String CSV_FILE = "tickets";
    private static final String CSV_CODES= "infractions";

  public void readCSV(IMap<String, Infraction> infractionMap,IMap<String, String> codeInfraction,String cityCode,String inPath){

      try (BufferedReader br = new BufferedReader(new FileReader(inPath + CSV_FILE + cityCode + ".csv"))) {
          String line = br.readLine();
          if (line == null){
              System.out.println("Empty CSV");
              return;
          }


          String[] header = line.split(";");

          int[] indexes = {0,0,0,0,0,0}; // date plate violation agency amount community
          for(int i =0; i<=5 ;i++ ){
              header[i] =  header[i].toLowerCase();
              if(header[i].contains("date")){
                  indexes[0]=i;
              }else if(header[i].contains("plate")){
                  indexes[1]=i;
              }else if(header[i].contains("code")){
                  indexes[2]=i;
              } else if (header[i].equals("unit_description") || header[i].equals("issuing agency")) {
                  indexes[3]=i;
              } else if (header[i].contains("fine")) {
                  indexes[4]=i;
              } else if (header[i].equals("community_area_name") || header[i].equals("county name")) {
                  System.out.println(header[i]);
                  indexes[5]=i;
              }
          }



          AtomicInteger idCounter = new AtomicInteger();
          while ((line = br.readLine()) != null && idCounter.get() < 300) {
              String[] values = line.split(";");
              System.out.println(values[indexes[0]]);
              LocalDate date = parseDate(values[indexes[0]]); //arreglar? values[0] es asi: 2020-01-01 00:00:00
              String licensePlateNumber = values[indexes[1]];
              String violationCode = values[indexes[2]];
              String unitDescription = values[indexes[3]];
              String fineAmount = values[indexes[4]];
              String communityAreaName = values[indexes[5]];

              Infraction infraction = new Infraction(date, licensePlateNumber, violationCode,
                      unitDescription, communityAreaName,Double.parseDouble(fineAmount));
              String key = "infraction-" + idCounter.incrementAndGet();

              infractionMap.put(key, infraction);
          }
      } catch (IOException | ParseException e) {
          e.printStackTrace();
      }

      try (BufferedReader br = new BufferedReader(new FileReader(inPath + CSV_CODES + cityCode + ".csv"))) {
          String line = br.readLine();
          if (line == null){
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

}
