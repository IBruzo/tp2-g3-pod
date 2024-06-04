package org.example.query1;

import com.hazelcast.mapreduce.KeyPredicate;

import java.io.Serializable;
import java.util.Set;

public class ValidInfractionKeyPredicate implements KeyPredicate<String>, Serializable {
    private final Set<String> codeMap;

    public ValidInfractionKeyPredicate(Set<String> codeMap) {
        this.codeMap = codeMap;
    }

    @Override
    public boolean evaluate(String key) {
        return codeMap.contains(key);
    }
}
