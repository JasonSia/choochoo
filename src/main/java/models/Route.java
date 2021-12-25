package models;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class Route {

    private String routeName;
    private String stationA;
    private String stationB;
    private int time;
}
