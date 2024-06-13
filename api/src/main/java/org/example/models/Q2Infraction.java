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
public class Q2Infraction implements DataSerializable {

    private String violationCode;
    private String communityAreaName;

    public Q2Infraction() {
    }

    @Override
    public void writeData(ObjectDataOutput objectDataOutput) throws IOException {
        objectDataOutput.writeUTF(violationCode);
        objectDataOutput.writeUTF(communityAreaName);
    }

    @Override
    public void readData(ObjectDataInput objectDataInput) throws IOException {
        violationCode = objectDataInput.readUTF();
        communityAreaName = objectDataInput.readUTF();
    }
}
