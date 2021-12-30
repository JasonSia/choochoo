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

    public String getTrainNextDestination() {
        for (Node node : routeAssigned) {
            if (node.getDistance() > 0) {
                return node.getName();
            }
        }
        return currentLocation;
    }

    public String getTrainPreviousLocation() {
        for (int i = 0; i < routeAssigned.size(); i++) {
            if (routeAssigned.get(i).getDistance() > 0) {
                return routeAssigned.get(i - 1).getName();
            }
        }
        return currentLocation;
    }

    public void moveTrainByOneUnit() {
        for (Node node : routeAssigned) {
            if (node.getDistance() > 0) {
                node.setDistance(node.getDistance() - 1);
                if (node.getDistance() == 0) {
                    setCurrentLocation(getTrainPreviousLocation());
                }
                break;
            }
        }
    }

    public int getTimeToReachDestination() {
        int totalDistanceLeft = 0;
        for (Node node : routeAssigned) {
            totalDistanceLeft = totalDistanceLeft + node.getDistance();
        }
        return totalDistanceLeft;
    }
}
