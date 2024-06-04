package org.example.models;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class Pair<S, S1> {
    private String first;
    private String second;
}
