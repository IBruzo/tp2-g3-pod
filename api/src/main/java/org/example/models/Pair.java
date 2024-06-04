package org.example.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@Getter
@AllArgsConstructor
@ToString
public class Pair<S, S1> {
    private String first;
    private String second;
}
