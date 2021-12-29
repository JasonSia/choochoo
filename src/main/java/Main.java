import dijkstra.Dijkstra;
import dijkstra.Graph;
import dijkstra.Node;
import models.Context;
import models.MailPackage;
import models.Route;
import models.Station;
import models.Train;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class Main {

  public static void main(String args[]) {
    System.out.println("initializing choo choo");
    Context ctx = InitializeSystem.readInput(args);

    InitializeSystem.generatePackagesInRespectiveStations(ctx);
    InitializeSystem.placeTrainInStations(ctx);
    InitializeSystem.getMapForRouting(ctx);

    deliverPackages(ctx);
  }

  private static void deliverPackages(Context ctx) {
    boolean allMailPackagesDelivered = false;
    int currentTime = 0;
    while (!allMailPackagesDelivered) {

      // move the train closer to their destination
      for (Train train : ctx.getTrains()) {
        if (train.getDestination() != null
            && !train.getCurrentLocation().equalsIgnoreCase(train.getDestination())
            && getTimeToReachDestination(train) > 1) {

          String route =
              determineRouteFromLocation(
                  ctx, train.getCurrentLocation(), getTrainNextDestination(train));
          logMovement(
              currentTime,
              train,
              Collections.emptyList(),
              Collections.emptyList(),
              getTrainPreviousLocation(train),
              getTrainNextDestination(train),
              route,
              getDistanceOfRoute(ctx, route));

          moveTrainByOneUnit(train);

        } else if (train.getDestination() != null
            && !train.getCurrentLocation().equalsIgnoreCase(train.getDestination())
            && getTimeToReachDestination(train) == 1) {
          // reached destination
          train.setCurrentLocation(train.getDestination());
          train.setRouteAssigned(Collections.emptyList());

          // get first x packages till full first
          Station currentStation = ctx.getStations().get(train.getCurrentLocation());
          List<MailPackage> mailPackageToDeliver =
              ctx.getStations().get(train.getCurrentLocation()).getMailPackages();
          String firstDestination =
              mailPackageToDeliver.stream().findFirst().get().getDestination();
          unloadTrain(firstDestination, train, currentStation);
          loadTrain(firstDestination, train, currentStation);

          // todo djisktra algorithm to find fastest path to destination
          Graph map = InitializeSystem.getMapForRouting(ctx);
          Graph pathAnalysisForTrain =
              Dijkstra.calculateShortestPathFromSource(
                  map, map.getNodesByName(currentStation.getName()));
          Optional<Node> pathForTrain =
              pathAnalysisForTrain.getNodes().stream()
                  .filter(p -> p.getName().equalsIgnoreCase(firstDestination))
                  .findFirst();
          if (pathForTrain.isEmpty()) {
            System.out.println("no destination found in map");
          } else {
            List<Node> path = pathForTrain.get().getShortestPath();
            train.setRouteAssigned(path);
            train.setDestination(firstDestination);
          }
          // print log
          moveTrainByOneUnit(train);
        } else {
          // not moving
        }
      }

      // find packages in other stations that needs to be delivered

      for (Station station : getStationsWithPackages(ctx)) {
        // all packages in this station
        List<MailPackage> mailPackagesInStation = station.getMailPackages();

        // find best train to get package to send
        int shortestDistanceForTrainToReach = Integer.MAX_VALUE;
        Train nearestTrain = null;
        List<Node> pathToTake = Collections.emptyList();

        // todo if multiple train of same destination need to compare capacity (another knapsack)
        // todo bug: multiple train might be called if one is already otw
        for (Train train : ctx.getTrains()) {
          // only get train if train is not moving
          if (train.getDestination().equalsIgnoreCase(train.getCurrentLocation())) {
            Graph map1 = InitializeSystem.getMapForRouting(ctx);
            Graph pathForTrain =
                Dijkstra.calculateShortestPathFromSource(
                    map1, map1.getNodesByName(train.getCurrentLocation()));

            for (Node node : pathForTrain.getNodes()) {
              if (!train.getCurrentLocation().equalsIgnoreCase(node.getName())
                  && node.getName().equalsIgnoreCase(station.getName())) {
                if (node.getDistance() <= shortestDistanceForTrainToReach) {
                  shortestDistanceForTrainToReach = node.getDistance();
                  nearestTrain = train;
                  Optional<Node> destinationNode =
                      pathForTrain.getNodes().stream()
                          .filter(p -> p.getName().equalsIgnoreCase(station.getName()))
                          .findFirst();
                  pathToTake = destinationNode.get().getShortestPath();
                }
              }
            }
          }
        }

        if (nearestTrain != null) {
          nearestTrain.setRouteAssigned(pathToTake);
          nearestTrain.setDestination(station.getName());
        }

        // to optimise if to drop packages for other trains to pick it up if there is other train in
        // the same platform
        // station.getTrainsInStation().stream().map(Train::getMailPackages).collect(Collectors.toList());

      }

      if (hasAllMailPackagesDelivered(ctx)) {
        allMailPackagesDelivered = true;
      }
      currentTime = currentTime + 1;
    }
  }

  private static void logMovement(
      int currentTime,
      Train train,
      List<MailPackage> mailPackageLoaded,
      List<MailPackage> mailPackagedDropped,
      String from,
      String to,
      String route,
      int timeLeft) {
    StringBuilder sb = new StringBuilder();
    sb.append("@");
    sb.append(currentTime-1);
    sb.append(", ");
    sb.append("n ");
    sb.append(", q = ");
    sb.append(train.getName());
    sb.append(", load= ");
    sb.append(mailPackageLoaded.toString());
    sb.append(", drop= ");
    sb.append(mailPackagedDropped.toString());
    sb.append(", ");
    sb.append("moving ");
    sb.append(from);
    sb.append("->");
    sb.append(to);
    sb.append(":");
    sb.append(route);
    sb.append(" arr ");
    sb.append(timeLeft);

    System.out.println(sb.toString());
  }

  private static boolean hasAllMailPackagesDelivered(Context ctx) {
    return getStationsWithPackages(ctx).isEmpty() && getTrainsWithPackages(ctx).isEmpty();
  }

  private static List<Station> getStationsWithPackages(Context ctx) {
    List<Station> stationsWithPackages = new ArrayList<>();
    Map<String, Station> stations = ctx.getStations();
    Iterator<Map.Entry<String, Station>> stationIterator = stations.entrySet().iterator();
    while (stationIterator.hasNext()) {
      Map.Entry<String, Station> stationEntry = stationIterator.next();
      Station station = stationEntry.getValue();
      for (MailPackage mp : station.getMailPackages()) {
        if (!mp.getDestination().equalsIgnoreCase(station.getName())) {
          stationsWithPackages.add(station);
        }
      }
    }
    return stationsWithPackages;
  }

  private static List<Train> getTrainsWithPackages(Context ctx) {
    return ctx.getTrains().stream()
        .filter(p -> p.getMailPackages().size() > 0)
        .collect(Collectors.toList());
  }

  private static void loadTrain(String destination, Train train, Station station) {
    // todo implement knapsack algo
    List<MailPackage> mailPackage =
        station.getMailPackages().stream()
            .filter(p -> p.getDestination().equalsIgnoreCase(destination))
            .sorted(Comparator.comparing(MailPackage::getWeight))
            .collect(Collectors.toList());

    int currentLoadOnTrain =
        train.getMailPackages().stream().mapToInt(MailPackage::getWeight).sum();

    while (currentLoadOnTrain < train.getCapacity()) {
      //todo for loop the package to add instead of getting first
      Optional<MailPackage> mailPackageToAdd = mailPackage.stream().findFirst();
      if (mailPackageToAdd.get().getWeight() + currentLoadOnTrain <= train.getCapacity()) {
        train.getMailPackages().add(mailPackageToAdd.get());
        station.getMailPackages().remove(mailPackageToAdd.get());
      }
      currentLoadOnTrain = train.getMailPackages().stream().mapToInt(MailPackage::getWeight).sum();
    }

    station.getMailPackages().removeAll(mailPackage);
  }

  private static void unloadTrain(String destination, Train train, Station station) {
    List<MailPackage> mailPackage =
        train.getMailPackages().stream()
            .filter(p -> p.getDestination().equalsIgnoreCase(destination))
            .collect(Collectors.toList());
    station.getMailPackages().addAll(mailPackage);
    train.getMailPackages().removeAll(mailPackage);
  }

  private static String determineRouteFromLocation(
      Context ctx, String currentLocation, String destination) {
    for (Route route : ctx.getRoutes()) {
      if ((route.getStationA().equalsIgnoreCase(currentLocation)
          || route.getStationB().equalsIgnoreCase(destination)
              && ((route.getStationA().equalsIgnoreCase(destination))
                  || route.getStationB().equalsIgnoreCase(currentLocation)))) {
        return route.getRouteName();
      }
    }
    return "invalid route";
  }

  private static int getTimeToReachDestination(Train train) {
    int totalDistanceLeft = 0;
    for (Node node : train.getRouteAssigned()) {
      totalDistanceLeft = totalDistanceLeft + node.getDistance();
    }
    return totalDistanceLeft;
  }

  private static void moveTrainByOneUnit(Train train) {
    for (Node node : train.getRouteAssigned()) {
      if (node.getDistance() > 0) {
        node.setDistance(node.getDistance() - 1);
        if (node.getDistance() == 0) {
          train.setCurrentLocation(getTrainPreviousLocation(train));
        }
        break;
      }
    }
  }

  private static String getTrainNextDestination(Train train) {
    for (Node node : train.getRouteAssigned()) {
      if (node.getDistance() > 0) {
        return node.getName();
      }
    }
    return train.getCurrentLocation();
  }

  private static String getTrainPreviousLocation(Train train) {
    for (int i = 0; i < train.getRouteAssigned().size(); i++) {
      if (train.getRouteAssigned().get(i).getDistance() > 0) {
        return train.getRouteAssigned().get(i - 1).getName();
      }
    }
    return train.getCurrentLocation();
  }

  private static int getDistanceOfRoute(Context ctx, String routeName){
    for (Route route: ctx.getRoutes()){
      if (route.getRouteName().equalsIgnoreCase(routeName)){
        return route.getTime();
      }
    }
    return 0;
  }
}
