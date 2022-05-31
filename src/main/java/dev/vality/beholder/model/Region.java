package dev.vality.beholder.model;

import lombok.Data;

@Data
public class Region {

    private String code;

    private String country;

    @Override
    public String toString() {
        return country + "[" + code + "]";
    }
}
