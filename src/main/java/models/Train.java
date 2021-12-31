package models;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

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
  private List<Route> routeAssigned = Collections.emptyList();
  private List<MailPackage> mailPackages = new ArrayList<>();

  public Train(String name, int capacity, String startingPoint) {
    this.name = name;
    this.capacity = capacity;
    this.startingPoint = startingPoint;
    this.currentLocation = startingPoint;
  }

  public int getTimeToReachDestination() {
    int totalTime = 0;
    for (Route route : routeAssigned) {
      totalTime = totalTime + route.getTime();
    }
    return totalTime;
  }

  public void moveTrainByOneUnitTime() {
    for (Route route : routeAssigned) {
      if (route.getTime() > 0) {
        currentLocation = route.getStationA();
        route.setTime(route.getTime() - 1);
        break;
      }
    }
  }

  public Optional<Route> getCurrentRoute() {
    for (Route route : routeAssigned) {
      if (route.getTime() > 0) {
        return Optional.of(route);
      }
    }
    return Optional.empty();
  }



      public String getNextDestination() {
          for (Route route : routeAssigned) {
              if (route.getTime() > 0) {
                  return route.getStationB();
              }
          }
          return null;
      }

}
