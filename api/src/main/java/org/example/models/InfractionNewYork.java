package org.example.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;

@Getter
@Setter
@AllArgsConstructor
public class InfractionNewYork implements Serializable {
    // Plate;Issue Date;InfractionCode;
    // Fine Amount;County Name;Issuing Agency
    // GXH1273;2022-08-25;36;50.0;Kings;DEPARTMENT OF TRANSPORTATION

    private String plate;
    private Date issueDate;
    private String infractionCode;
    private int fineAmount;
    private String countyName;
    private String issuingAgency;

}