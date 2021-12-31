import dijkstra.Dijkstra;
import dijkstra.Graph;
import dijkstra.Node;
import models.Context;
import models.MailPackage;
import models.Route;
import models.Train;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

public class Main {

  private static int currentTime = 0;

  public static void main(String args[]) {
    System.out.println("initializing choo choo");
    Context ctx = InitializeSystem.readInput(args);

    InitializeSystem.generatePackagesInRespectiveStations(ctx);
    InitializeSystem.placeTrainInStations(ctx);
    InitializeSystem.getMapForRouting(ctx);
    deliverPackages(ctx);
  }

  private static void deliverPackages(Context ctx) {
    int currentTime = 0;
    for (MailPackage mailPackage : ctx.getAllUndeliveredMailPackages()) {
      Train firstTrainThatCanCarryPackage =
          ctx.getNonMovingTrains().stream()
              .filter(p -> p.getCapacity() >= mailPackage.getWeight())
              .findFirst()
              .get();

      Graph map = InitializeSystem.getMapForRouting(ctx);
      Graph pathForTrainToAllStation =
          Dijkstra.calculateShortestPathFromSource(
              map, map.getNodesByName(firstTrainThatCanCarryPackage.getCurrentLocation()));

      Node pathForTrainToMailDestination =
          pathForTrainToAllStation.getNodesByName(mailPackage.getSource());
      LinkedList<Route> routeForTrain =
          convertNodeToRouteForTrain(ctx, pathForTrainToMailDestination);
      firstTrainThatCanCarryPackage.setRouteAssigned(routeForTrain);
      moveTrainToDeliverPackage(firstTrainThatCanCarryPackage);
    }
  }

  private static void moveTrainToDeliverPackage(Train train) {
    while (train.getTimeToReachDestination() > 0) {

      Optional<Route> routeBeforeMoving = train.getCurrentRoute();
      train.moveTrainByOneUnitTime();
      Optional<Route> routeAfterMoving = train.getCurrentRoute();
      if (train.getTimeToReachDestination() > 0) {
        //still moving dont do anything
      } else if (train.getTimeToReachDestination() == 0) {
        //load package
        //unload package
        //add routetotrain
      }
      if (routeBeforeMoving.isPresent()
          && routeAfterMoving.isPresent()
          && !routeAfterMoving
              .get()
              .getRouteName()
              .equalsIgnoreCase(routeBeforeMoving.get().getRouteName())) {
        logMovement(
            currentTime,
            train.getCurrentLocation(),
            train,
            Collections.emptyList(),
            Collections.emptyList(),
            routeBeforeMoving.get().getStationA(),
            routeBeforeMoving.get().getStationB(),
            routeBeforeMoving.get().getRouteName(),
            train.getCurrentRoute().get().getTime());
      }else if (routeBeforeMoving.isPresent() && routeAfterMoving.isEmpty()){
        logMovement(
                currentTime,
                train.getCurrentLocation(),
                train,
                Collections.emptyList(),
                Collections.emptyList(),
                routeBeforeMoving.get().getStationA(),
                routeBeforeMoving.get().getStationB(),
                routeBeforeMoving.get().getRouteName(),
                100000);
      }
      if (train.getTimeToReachDestination() == 0) {
        train.setRouteAssigned(Collections.emptyList());
      }
      currentTime = currentTime + 1;
    }
  }

  private static void logMovement(
      int currentTime,
      String node,
      Train train,
      List<MailPackage> mailPackageLoaded,
      List<MailPackage> mailPackagedDropped,
      String from,
      String to,
      String route,
      int timeLeft) {
    String sb =
        "@"
            + (currentTime)
            + ", "
            + "n = "
            + node
            + ", q = "
            + train.getName()
            + ", load= "
            + mailPackageLoaded.toString()
            + ", drop= "
            + mailPackagedDropped.toString()
            + ", "
            + "moving "
            + from
            + "->"
            + to
            + ":"
            + route
            + " arr "
            + (timeLeft + currentTime);

    System.out.println(sb);
  }

  private static LinkedList<Route> convertNodeToRouteForTrain(Context ctx, Node path) {
    LinkedList<Route> routeForTrain = new LinkedList<>();
    for (int i = 0; i < path.getShortestPath().size() - 1; i++) {
      String from = path.getShortestPath().get(i).getName();
      String to = path.getShortestPath().get(i + 1).getName();
      String routeNameToUse = ctx.determineRouteFromLocation(from, to);
      int routedistance = ctx.getDistanceOfRoute(routeNameToUse);
      Route route = new Route(routeNameToUse, from, to, routedistance);
      routeForTrain.add(route);
    }
    // add the last route
    String startingPointForLastNode = null;
    if (path.getShortestPath().size() == 1) {
      startingPointForLastNode = path.getShortestPath().get(0).getName();
    } else {
      startingPointForLastNode = routeForTrain.getLast().getStationB();
    }

    String routeNameToUse =
        ctx.determineRouteFromLocation(startingPointForLastNode, path.getName());
    int routedistance = ctx.getDistanceOfRoute(routeNameToUse);
    Route route =
        new Route(routeNameToUse, startingPointForLastNode, path.getName(), routedistance);
    routeForTrain.add(route);

    return routeForTrain;
  }
}
