import dijkstra.Dijkstra;
import dijkstra.Graph;
import dijkstra.Node;
import models.Context;
import models.MailPackage;
import models.Route;
import models.Train;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

public class Main {

  private static int currentTime = 0;

  public static void main(String args[]) {
    Context ctx = InitializeSystem.readInput(args);

    InitializeSystem.generatePackagesInRespectiveStations(ctx);
    InitializeSystem.placeTrainInStations(ctx);
    InitializeSystem.getMapForRouting(ctx);
    deliverPackages(ctx);
  }

  private static void deliverPackages(Context ctx) {
    int currentTime = 0;
    while (ctx.getAllUndeliveredMailPackages().stream().findFirst().isPresent()) {
      MailPackage mailPackage = ctx.getAllUndeliveredMailPackages().stream().findFirst().get();
      Optional<Train> firstTrainThatCanCarryPackageFromNonMovingTrain =
          ctx.getNonMovingTrains().stream()
              .filter(p -> p.getCapacity() >= mailPackage.getWeight())
              .findFirst();

      if(firstTrainThatCanCarryPackageFromNonMovingTrain.isPresent()){
        Train firstTrainThatCanCarryPackage = firstTrainThatCanCarryPackageFromNonMovingTrain.get();
        Graph map = InitializeSystem.getMapForRouting(ctx);
        Graph pathForTrainToAllStation =
                Dijkstra.calculateShortestPathFromSource(
                        map, map.getNodesByName(firstTrainThatCanCarryPackage.getCurrentLocation()));

        Node pathForTrainToMailSource =
                pathForTrainToAllStation.getNodesByName(mailPackage.getSource());
        LinkedList<Route> routeForTrain =
                convertNodeToRouteForTrain(ctx, pathForTrainToMailSource);
        firstTrainThatCanCarryPackage.setRouteAssigned(routeForTrain);
        moveTrainToDeliverPackage(ctx, firstTrainThatCanCarryPackage, mailPackage);
      }
    }
  }

  private static void moveTrainToDeliverPackage(Context ctx, Train train, MailPackage packageToDeliver) {
    while (train.getTimeToReachDestination() >= 0 && packageToDeliver.getStatus() != MailPackage.DELIVERED) {
      Optional<Route> routeBeforeMoving = train.getCurrentRoute();
      train.moveTrainByOneUnitTime();
      Optional<Route> routeAfterMoving = train.getCurrentRoute();
      List<MailPackage> loaded = new ArrayList<>();
      List<MailPackage> unloaded = new ArrayList<>();
      if (train.getTimeToReachDestination() > 0 ) {
        //placeholder
      } else if (train.getTimeToReachDestination() == 0) {
        //unload package
        if(train.getRouteAssigned().get(train.getRouteAssigned().size()-1).getStationB().equalsIgnoreCase(packageToDeliver.getDestination())){
          //unload train

          //remove from train
          train.getMailPackages().remove(packageToDeliver);
          //add to station
          ctx.getStations().get(packageToDeliver.getDestination()).getMailPackages().add(packageToDeliver);
          packageToDeliver.setStatus(MailPackage.DELIVERED);
          unloaded.add(packageToDeliver);
        }else if (train.getRouteAssigned().get(train.getRouteAssigned().size()-1).getStationB().equalsIgnoreCase(packageToDeliver.getSource())){
          //load train

          //add to train
          train.getMailPackages().add(packageToDeliver);
          //remove from station
          ctx.getStations().get(packageToDeliver.getSource()).removePackage(packageToDeliver.getName());

          loaded.add(packageToDeliver);
        }
        if (packageToDeliver.getStatus() != MailPackage.DELIVERED){
          Graph map = InitializeSystem.getMapForRouting(ctx);
          Graph pathForTrainToAllStation =
                  Dijkstra.calculateShortestPathFromSource(
                          map, map.getNodesByName(train.getCurrentLocation()));
          Node pathForTrainToMailDestination =
                  pathForTrainToAllStation.getNodesByName(packageToDeliver.getDestination());
          LinkedList<Route> routeForTrain =
                  convertNodeToRouteForTrain(ctx, pathForTrainToMailDestination);

          String routeToUse = ctx.determineRouteFromLocation(train.getCurrentLocation(), packageToDeliver.getSource());
          Route route = new Route(routeToUse, train.getCurrentLocation(), packageToDeliver.getSource(),ctx.getDistanceOfRoute(routeToUse));
          train.getRouteAssigned().add(route);
          train.getRouteAssigned().addAll(routeForTrain);
          if(packageToDeliver.getStatus() == MailPackage.TO_DELIVER){
            packageToDeliver.setStatus(MailPackage.DELIVERING);
          }
        }

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
            loaded,
            unloaded,
            routeBeforeMoving.get().getStationA(),
            routeBeforeMoving.get().getStationB(),
            routeBeforeMoving.get().getRouteName(),
            train.getCurrentRoute().get().getTime());
      }else if (routeBeforeMoving.isPresent() && routeAfterMoving.isEmpty()){
        logMovement(
                currentTime,
                train.getCurrentLocation(),
                train,
                loaded,
                unloaded,
                routeBeforeMoving.get().getStationA(),
                routeBeforeMoving.get().getStationB(),
                routeBeforeMoving.get().getRouteName(),
                0);
      }
      if (packageToDeliver.getStatus() == MailPackage.DELIVERED) {
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
