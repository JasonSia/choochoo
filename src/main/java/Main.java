import dijkstra.Dijkstra;
import dijkstra.Graph;
import dijkstra.Node;
import models.Context;
import models.MailPackage;
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
            && train.getTimeToReachDestination() > 1) {

          String route =
              ctx.determineRouteFromLocation(
                  train.getCurrentLocation(), train.getTrainNextDestination());
          logMovement(
              currentTime,
              train.getTrainPreviousLocation(),
              train,
              Collections.emptyList(),
              Collections.emptyList(),
              train.getTrainPreviousLocation(),
              train.getTrainNextDestination(),
              route,
              ctx.getDistanceOfRoute(route));

          train.moveTrainByOneUnit();

        } else if (train.getDestination() != null
            && !train.getCurrentLocation().equalsIgnoreCase(train.getDestination())
            && train.getTimeToReachDestination() == 1) {

          // reached destination
          train.setCurrentLocation(train.getDestination());
          train.setRouteAssigned(Collections.emptyList());

          // get first x packages till full first
          Station currentStation = ctx.getStations().get(train.getCurrentLocation());
          List<MailPackage> mailPackageToDeliver =
              ctx.getStations().get(train.getCurrentLocation()).getMailPackages();
          String firstDestination =
              mailPackageToDeliver.stream().findFirst().get().getDestination();
          List<MailPackage> unloadedPackage = unloadTrain(firstDestination, train, currentStation);
          List<MailPackage> loadedPackage = loadTrain(firstDestination, train, currentStation);

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
          String route =
                  ctx.determineRouteFromLocation(
                          train.getCurrentLocation(), train.getTrainNextDestination());
          logMovement(
                  currentTime,
                  train.getTrainPreviousLocation(),
                  train,
                  unloadedPackage,
                  loadedPackage,
                  train.getTrainPreviousLocation(),
                  train.getTrainNextDestination(),
                  route,
                  ctx.getDistanceOfRoute(route));

          train.moveTrainByOneUnit();
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
      String node,
      Train train,
      List<MailPackage> mailPackageLoaded,
      List<MailPackage> mailPackagedDropped,
      String from,
      String to,
      String route,
      int timeLeft) {
    String sb = "@" +
            (currentTime - 1) +
            ", " +
            "n =" +
            node +
            ", q = " +
            train.getName() +
            ", load= " +
            mailPackageLoaded.toString() +
            ", drop= " +
            mailPackagedDropped.toString() +
            ", " +
            "moving " +
            from +
            "->" +
            to +
            ":" +
            route +
            " arr " +
            timeLeft;

    System.out.println(sb);
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

  private static List<MailPackage> loadTrain(String destination, Train train, Station station) {
    // todo implement knapsack algo
    List<MailPackage> mailPackages =
        station.getMailPackages().stream()
            .filter(p -> p.getDestination().equalsIgnoreCase(destination))
            .sorted(Comparator.comparing(MailPackage::getWeight))
            .collect(Collectors.toList());

    int currentLoadOnTrain =
        train.getMailPackages().stream().mapToInt(MailPackage::getWeight).sum();
    int mailPackageIndex = 0;
    List<MailPackage> loadedPackage = new ArrayList<>();
    while (currentLoadOnTrain < train.getCapacity() && mailPackageIndex < mailPackages.size()) {
      // todo for loop the package to add instead of getting first
      MailPackage mailPackageToAdd = mailPackages.get(mailPackageIndex);
      if (mailPackageToAdd.getWeight() + currentLoadOnTrain <= train.getCapacity()) {
        train.getMailPackages().add(mailPackageToAdd);
        loadedPackage.add(mailPackageToAdd);
        mailPackageIndex++;
      }
      currentLoadOnTrain = train.getMailPackages().stream().mapToInt(MailPackage::getWeight).sum();
    }

    return loadedPackage;
  }

  private static List<MailPackage> unloadTrain(String destination, Train train, Station station) {
    List<MailPackage> mailPackage =
        train.getMailPackages().stream()
            .filter(p -> p.getDestination().equalsIgnoreCase(destination))
            .collect(Collectors.toList());
    station.getMailPackages().addAll(mailPackage);
    train.getMailPackages().removeAll(mailPackage);

    return mailPackage;
  }
}
