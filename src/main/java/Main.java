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

    //    // to get shortest path from A to anywhere.
    //    Graph shortestPathGraph =
    //            Dijkstra.calculateShortestPathFromSource(ctx.getGraph(), ctx.getNodes().get("A"));

    deliverPackages(ctx);
    // loop through stations
    // find closest trains to stations to start transporting with greedy algorithm
    // station controlling trains to come
    // similar to grab on demand service
  }

  private static void deliverPackages(Context ctx) {
    boolean allMailPackagesDelivered = false;
    int currentTime = 0;
    while (!allMailPackagesDelivered) {

      // move the train closer to their destination
      for (Train train : ctx.getTrains()) {
        if (!train.getCurrentLocation().equalsIgnoreCase(train.getDestination())
            && train.getTimeToReachDestination() > 1) {
          // still moving
          // todo convert to list path and deduct from path to determine pathing
          for (int i = 0; i < train.getRouteAssigned().size(); i++) {
            Node n = train.getRouteAssigned().get(i);
            if (n.getDistance() > 1) {
              n.setDistance(n.getDistance() - 1);
              if (i == 0) {
                String from = n.getName();
                String to = train.getRouteAssigned().get(i + 1).getName();
                String routeName = determineRouteFromLocation(ctx, from, to);
              } else {
                String from = train.getRouteAssigned().get(i - 1).getName();
                String to = n.getName();
                String routeName = determineRouteFromLocation(ctx, from, to);
              }
              break;
            }
          }

          // print log
        } else if (!train.getCurrentLocation().equalsIgnoreCase(train.getDestination())
            && train.getTimeToReachDestination() == 1) {
          // reached destination
          train.setCurrentLocation(train.getDestination());
          train.setTimeToReachDestination(0);
          train.setDestination(null);
          // get first x packages till full first

          Station currentStation = ctx.getStations().get(train.getCurrentLocation());
          List<MailPackage> mailPackageToDeliver =
              ctx.getStations().get(train.getCurrentLocation()).getMailPackages();
          String firstDestination =
              mailPackageToDeliver.stream().findFirst().get().getDestination();
          unloadTrain(firstDestination, train, currentStation);
          loadTrain(firstDestination, train, currentStation);

          // todo djisktra algorithm to find fastest path to destination
          Graph map1 = InitializeSystem.getMapForRouting(ctx);
          Graph pathAnalysisForTrain =
              Dijkstra.calculateShortestPathFromSource(
                  map1, map1.getNodesByName(currentStation.getName()));
          Optional<Node> pathForTrain =
              pathAnalysisForTrain.getNodes().stream()
                  .filter(p -> p.getName().equalsIgnoreCase(firstDestination))
                  .findFirst();
          if (pathForTrain.isEmpty()) {
            System.out.println("no destination found in map");
          } else {
            List<Node> path = pathForTrain.get().getShortestPath();
            train.setRouteAssigned(path);
            // assign path to train
          }
          // print log
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
        for (Train train : ctx.getTrains()) {
          // only get train if train is not moving
          if (train.getDestination() == null) {

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
            System.out.println(nearestTrain.getName());
            System.out.println(shortestDistanceForTrainToReach);
          }
        }

        if (nearestTrain != null) {
          nearestTrain.setDestination(station.getName());
          nearestTrain.setTimeToReachDestination(shortestDistanceForTrainToReach);
          nearestTrain.setRouteAssigned(pathToTake);
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
      Node node,
      Train train,
      List<MailPackage> mailPackageLoaded,
      List<MailPackage> mailPackagedDropped,
      Route route,
      int cost) {
    StringBuilder sb = new StringBuilder();
    sb.append("@");
    sb.append(currentTime);
    sb.append(", ");

    sb.append(train.getName());

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
    // loop through routes where both current and destination exist, return ctx

    for (Route route : ctx.getRoutes()) {
      if ((route.getStationA().equalsIgnoreCase(currentLocation)
          || route.getStationB().equalsIgnoreCase(destination)
              && ((route.getStationA().equalsIgnoreCase(destination))
                  || route.getStationB().equalsIgnoreCase(currentLocation)))) {
        return route.getRouteName();
      } else {
        return "invalid route";
      }
    }
    return "invalid route";
  }
}
