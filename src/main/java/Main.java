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
        if (train.getFinalDestination() != null
            && !train.getCurrentLocation().equalsIgnoreCase(train.getFinalDestination())
            && train.getTimeToReachDestination() > 1) {
          trainContinueToMove(ctx, currentTime, train);

        } else if (train.getFinalDestination() != null
            && !train.getCurrentLocation().equalsIgnoreCase(train.getFinalDestination())
            && train.getTimeToReachDestination() == 1) {
          trainReachedDestination(ctx, currentTime, train);
        }
      }

      // find packages in other stations that needs to be delivered

      for (Station station : getStationsWithPackages(ctx)) {
        // all packages in this station
        List<MailPackage> mailPackagesInStation = station.getMailPackages();

        // find best train to get package to send
        if (!mailPackagesInStation.isEmpty()) {
          callForTrain(ctx, station);
        }
      }

      if (hasAllMailPackagesDelivered(ctx)) {
        allMailPackagesDelivered = true;
      }
      currentTime = currentTime + 1;
    }
  }

  private static void callForTrain(Context ctx, Station station) {
    int shortestDistanceForTrainToReach = Integer.MAX_VALUE;
    Train nearestTrain = null;
    List<Node> pathToTake = null;
    Node shortestPathToTargetStation = null;
    // todo if multiple train of same destination need to compare capacity (another knapsack)

    // find nearest train from station
    for (Train train : ctx.getTrains()) {
      // only get train if train is not moving
      if (train.getRouteAssigned().isEmpty()) {
        Graph map1 = InitializeSystem.getMapForRouting(ctx);
        Graph pathForTrainToAllStation =
            Dijkstra.calculateShortestPathFromSource(
                map1, map1.getNodesByName(train.getCurrentLocation()));


        for (Node pathForTrainToStation : pathForTrainToAllStation.getNodes()) {
          if (pathForTrainToStation.getName().equalsIgnoreCase(station.getName())) {
            shortestPathToTargetStation = pathForTrainToStation;
          }
        }

        if (shortestPathToTargetStation.getDistance() < shortestDistanceForTrainToReach) {
          shortestDistanceForTrainToReach = shortestPathToTargetStation.getDistance();
          nearestTrain = train;
        }

      }

      // to optimise if to drop packages for other trains to pick it up if there is other train in
      // the same platform
      // station.getTrainsInStation().stream().map(Train::getMailPackages).collect(Collectors.toList());
    }

    if (nearestTrain != null) {
      pathToTake = repurposeNodeDistance(ctx, shortestPathToTargetStation);
      nearestTrain.setRouteAssigned(pathToTake);
      nearestTrain.setFinalDestination(station.getName());
    }
  }

  private static void trainContinueToMove(Context ctx, int currentTime, Train train) {
    String route =
        ctx.determineRouteFromLocation(train.getCurrentLocation(), train.getTrainNextDestination());
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
  }

  private static void trainReachedDestination(Context ctx, int currentTime, Train train) {
    // reaching destination in next step
    train.setCurrentLocation(train.getFinalDestination());
    train.setRouteAssigned(Collections.emptyList());

    // get first x packages till full first
    Station currentStation = ctx.getStations().get(train.getCurrentLocation());

    List<MailPackage> packageToDeliver = currentStation.getUndeliveredPackage();

    List<MailPackage> unloadedPackage = Collections.emptyList();
    unloadedPackage = unloadTrain(train.getFinalDestination(), train, currentStation);
    List<MailPackage> loadedPackage = Collections.emptyList();

    if (!packageToDeliver.isEmpty()) {
      // there is something the train needs help to ship and train
      String firstDestination = packageToDeliver.stream().findFirst().get().getDestination();

      loadedPackage = loadTrain(firstDestination, train, currentStation);

      Graph map = InitializeSystem.getMapForRouting(ctx);
      Graph getShortestPathToAllStations =
          Dijkstra.calculateShortestPathFromSource(
              map, map.getNodesByName(currentStation.getName()));

      Node shortestPathToTargetStation = null;
      for (Node pathForTrainToStation : getShortestPathToAllStations.getNodes()) {
        if (pathForTrainToStation.getName().equalsIgnoreCase(firstDestination)) {
          shortestPathToTargetStation = pathForTrainToStation;
        }
      }

      if (shortestPathToTargetStation == null) {
        System.out.println("no destination found in map");
      } else {
        List<Node> path = shortestPathToTargetStation.getShortestPath();
        train.setRouteAssigned(path);
        train.setFinalDestination(firstDestination);
      }
    }

    String route =
        ctx.determineRouteFromLocation(train.getCurrentLocation(), train.getTrainNextDestination());
    logMovement(
        currentTime,
        train.getTrainPreviousLocation(),
        train,
        loadedPackage,
        unloadedPackage,
        train.getTrainPreviousLocation(),
        train.getTrainNextDestination(),
        route,
        ctx.getDistanceOfRoute(route));

    train.moveTrainByOneUnit();
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
            + (currentTime - 1)
            + ", "
            + "n ="
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
            + (timeLeft + currentTime-1);

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
        if (!mp.getDestination().equalsIgnoreCase(station.getName()) && mp.getStatus()==MailPackage.TO_DELIVER) {
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

    // sort packages of location destination
    List<MailPackage> mailPackages =
        station.getMailPackages().stream()
            .filter(
                p ->
                    p.getDestination().equalsIgnoreCase(destination)
                        )
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
        mailPackageToAdd.setStatus(MailPackage.DELIVERING);
        train.getMailPackages().add(mailPackageToAdd);
        loadedPackage.add(mailPackageToAdd);
        mailPackageIndex++;
      }
      currentLoadOnTrain = train.getMailPackages().stream().mapToInt(MailPackage::getWeight).sum();
    }

    return loadedPackage;
  }

  private static List<MailPackage> unloadTrain(String destination, Train train, Station station) {
    List<MailPackage> mailPackages =
        train.getMailPackages().stream()
            .filter(p -> p.getDestination().equalsIgnoreCase(destination))
            .collect(Collectors.toList());

    for (MailPackage mp: mailPackages){
      mp.setStatus(MailPackage.DELIVERED);
    }
    station.getMailPackages().addAll(mailPackages);
    train.getMailPackages().removeAll(mailPackages);

    return mailPackages;
  }

  private static List<Node> repurposeNodeDistance(Context ctx, Node pathToTake){
    if (pathToTake.getShortestPath().size() == 1){
      pathToTake.getShortestPath().get(0).setDistance(pathToTake.getDistance());
    }else{
      Node finalNode = new Node();
      finalNode.setName(pathToTake.getName());//no details required as it will be retrieve from route
      finalNode.setDistance(0);
      pathToTake.getShortestPath().add(finalNode);
      for(int i = 0; i < pathToTake.getShortestPath().size() -1 ; i ++){
        String route = ctx.determineRouteFromLocation(pathToTake.getShortestPath().get(i).getName(), pathToTake.getShortestPath().get(i+1).getName());
        pathToTake.getShortestPath().get(i).setDistance(ctx.getDistanceOfRoute(route));
      }
    }
    return pathToTake.getShortestPath().subList(0, pathToTake.getShortestPath().size()-1);
  }

}
