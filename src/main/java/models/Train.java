package models;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
public class Train {

    @Setter(AccessLevel.NONE)
    private String name;
    @Setter(AccessLevel.NONE)
    private int capacity;
    @Setter(AccessLevel.NONE)
    private String startingPoint;
    private String currentLocation;//route/node
    private String destination;
    private int timeToReachDestination = 0;
    private List<MailPackage> mailPackages = new ArrayList<>();

    public Train(String name, int capacity, String startingPoint){
        this.name = name;
        this.capacity = capacity;
        this.startingPoint = startingPoint;
        this.currentLocation = startingPoint;
    }
}
