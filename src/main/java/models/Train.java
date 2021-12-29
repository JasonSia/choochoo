package models;

import dijkstra.Node;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Collections;
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
    private String currentLocation;
    private String destination;
    private List<Node> routeAssigned = Collections.emptyList();
    private List<MailPackage> mailPackages = new ArrayList<>();

    public Train(String name, int capacity, String startingPoint){
        this.name = name;
        this.capacity = capacity;
        this.startingPoint = startingPoint;
        this.currentLocation = startingPoint;
        this.destination = startingPoint;
    }
}
