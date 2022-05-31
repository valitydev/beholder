package dev.vality.beholder.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class NetworkLog {

    private String resource;

    private Double start;

    private Double end;

}
