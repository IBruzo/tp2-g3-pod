package org.example.models;

import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.DataSerializable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.io.IOException;
import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
public class Infraction implements DataSerializable {

    private LocalDate infractionDate;
    private String licensePlateNumber;
    private String violationCode;
    private String unitDescription;
    private String communityAreaName;
    private double fineAmount;

    public Infraction() { }


    @Override
    public void writeData(ObjectDataOutput objectDataOutput) throws IOException {
        objectDataOutput.writeLong(infractionDate.toEpochDay());
        objectDataOutput.writeUTF(licensePlateNumber);
        objectDataOutput.writeUTF(violationCode);
        objectDataOutput.writeUTF(unitDescription);
        objectDataOutput.writeUTF(communityAreaName);
        objectDataOutput.writeDouble(fineAmount);
    }

    @Override
    public void readData(ObjectDataInput objectDataInput) throws IOException {
        infractionDate = LocalDate.ofEpochDay(objectDataInput.readLong());
        licensePlateNumber = objectDataInput.readUTF();
        violationCode = objectDataInput.readUTF();
        unitDescription = objectDataInput.readUTF();
        communityAreaName = objectDataInput.readUTF();
        fineAmount = objectDataInput.readDouble();
    }
}
