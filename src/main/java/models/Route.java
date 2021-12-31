package models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
public class Route {

    private String routeName;
    private String stationA;
    private String stationB;
    private int time;
}
