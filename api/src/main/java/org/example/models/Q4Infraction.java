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
public class Q4Infraction implements DataSerializable {

    private String licensePlateNumber;
    private String communityAreaName;
    private LocalDate infractionDate;

    public Q4Infraction() {
    }

    @Override
    public void writeData(ObjectDataOutput objectDataOutput) throws IOException {
        objectDataOutput.writeUTF(licensePlateNumber);
        objectDataOutput.writeUTF(communityAreaName);
        objectDataOutput.writeLong(infractionDate.toEpochDay());
    }

    @Override
    public void readData(ObjectDataInput objectDataInput) throws IOException {
        licensePlateNumber = objectDataInput.readUTF();
        communityAreaName = objectDataInput.readUTF();
        infractionDate = LocalDate.ofEpochDay(objectDataInput.readLong());
    }
}
