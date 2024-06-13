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
public class Q3Infraction implements DataSerializable {

    private String unitDescription;
    private Double fineAmount;

    public Q3Infraction() {
    }

    @Override
    public void writeData(ObjectDataOutput objectDataOutput) throws IOException {
        objectDataOutput.writeUTF(unitDescription);
        objectDataOutput.writeDouble(fineAmount);
    }

    @Override
    public void readData(ObjectDataInput objectDataInput) throws IOException {
        unitDescription = objectDataInput.readUTF();
        fineAmount = objectDataInput.readDouble();
    }
}
