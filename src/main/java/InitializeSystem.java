import dijkstra.Node;
import models.Context;
import models.MailPackage;
import models.Route;
import models.Station;
import models.Train;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class InitializeSystem {

    private static final int POINTER_OFFSET = 1;

    private static final int STATION_DATA_NAME_INDEX = 0;

    private static final int ROUTE_DATA_NAME_INDEX = 0;
    private static final int ROUTE_DATA_STATION_A_INDEX = 1;
    private static final int ROUTE_DATA_STATION_B_INDEX = 2;
    private static final int ROUTE_DATA_TIME_INDEX = 3;

    private static final int PACKAGE_DATA_NAME_INDEX = 0;
    private static final int PACKAGE_DATA_SOURCE_INDEX = 1;
    private static final int PACKAGE_DATA_DESTINATION_INDEX = 2;
    private static final int PACKAGE_DATA_WEIGHT_INDEX = 3;

    private static final int TRAIN_DATA_NAME_INDEX = 0;
    private static final int TRAIN_DATA_STARTING_LOCATION_INDEX = 1;
    private static final int TRAIN_DATA_CAPACITY_INDEX = 2;

    private static int argumentPointer = 0;

    protected static Context readInput(String args[]) {

        int numberOfStations = Integer.parseInt(args[argumentPointer]);
        int nextArgumentPointer = argumentPointer + numberOfStations + POINTER_OFFSET;
        // initializeStations
        Map<String, Station> stations = initializeStations(Arrays.copyOfRange(args, argumentPointer + POINTER_OFFSET, nextArgumentPointer));

        argumentPointer = nextArgumentPointer;
        int numberOfRoutes = Integer.parseInt(args[argumentPointer]);
        nextArgumentPointer = argumentPointer + numberOfRoutes + POINTER_OFFSET;
        // initializeRoute
        List<Route> routes =
                initializeRoutes(
                        Arrays.copyOfRange(args, argumentPointer + POINTER_OFFSET, nextArgumentPointer));

        argumentPointer = nextArgumentPointer;
        int numberOfPackages = Integer.parseInt(args[argumentPointer]);
        nextArgumentPointer = argumentPointer + numberOfPackages + POINTER_OFFSET;
        // initializePackages
        List<MailPackage> mailPackages =
                initalizePackages(
                        Arrays.copyOfRange(args, argumentPointer + POINTER_OFFSET, nextArgumentPointer));

        argumentPointer = nextArgumentPointer;
        // int numberOfTrains = Integer.parseInt(args[argumentPointer]);
        List<Train> trains = initalizeTrains(Arrays.copyOfRange(args, argumentPointer + POINTER_OFFSET, args.length));
        Context ctx = new Context(stations, routes, mailPackages, trains);
        System.out.println("finish initializing system");
        return ctx;

    }

    protected static void generatePackagesInRespectiveStations(Context ctx) {
        Map<String, Station> stations = ctx.getStations();
        Iterator<Map.Entry<String, Station>> stationIterator = stations.entrySet().iterator();
        while (stationIterator.hasNext() ) {
            Map.Entry<String, Station> stationEntry = stationIterator
                    .next();
            stationEntry.getValue().setMailPackages(
              ctx.getMailPackages().stream()
                  .filter(p -> p.getSource().equalsIgnoreCase(stationEntry.getKey()))
                  .collect(Collectors.toList()));
        }
    }

    protected static void generateRoutingMap(Context ctx) {
        Map<String, Station> stations = ctx.getStations();
        Iterator<Map.Entry<String, Station>> stationIterator = stations.entrySet().iterator();
        while (stationIterator.hasNext() ) {
            Map.Entry<String, Station> stationEntry = stationIterator.next();
            Station station = stationEntry.getValue();
            Node node = ctx.getNodes().get(station.getName());
            if (node == null){
                node = new Node(station.getName());
                ctx.getNodes().put(station.getName(), node);
            }
            for (Route route : ctx.getRoutes()) {
                if (!route.getStationA().equalsIgnoreCase(route.getStationB())) {
                    if (route.getStationA().equalsIgnoreCase(station.getName())){
                        Node nodeB = ctx.getNodes().get(route.getStationB());
                        if (nodeB == null){
                            nodeB = new Node(route.getStationB());
                            ctx.getNodes().put(route.getStationB(), nodeB);
                        }
                        node.addDestination(nodeB, route.getTime());
                    }else if (route.getStationB().equalsIgnoreCase(station.getName())){
                        Node nodeA = ctx.getNodes().get(route.getStationA());
                        if (nodeA == null){
                            nodeA = new Node(route.getStationA());
                            ctx.getNodes().put(route.getStationA(), nodeA);
                        }
                        node.addDestination(nodeA, route.getTime());
                    }
                }
            }
            ctx.getGraph().addNode(node);
        }
    }

    private static Map<String, Station> initializeStations(String[] stations) {
        Map<String, Station> stationMap = new HashMap<>();
        for (String stationInput: stations){
            String []stationInputArray = stationInput.split(",");
            Station station = new Station(stationInputArray[STATION_DATA_NAME_INDEX]);
            stationMap.put(station.getName(), station);
        }
        return stationMap;
    }

    private static List<Route> initializeRoutes(String routes[]) {
        List<Route> routeList = new ArrayList<>();
        for (String routeInput : routes) {
            String[] routeInputArray = routeInput.split(",");
            Route route =
                    new Route(
                            routeInputArray[ROUTE_DATA_NAME_INDEX],
                            routeInputArray[ROUTE_DATA_STATION_A_INDEX],
                            routeInputArray[ROUTE_DATA_STATION_B_INDEX],
                            Integer.parseInt(routeInputArray[ROUTE_DATA_TIME_INDEX]));
            routeList.add(route);
        }
        return routeList;
    }

    private static List<MailPackage> initalizePackages(String mailPackages[]) {
        List<MailPackage> mailPackageList = new ArrayList<>();
        for (String packageInput : mailPackages) {
            String[] packageInputArray = packageInput.split(",");
            MailPackage mailPackage =
                    new MailPackage(
                            packageInputArray[PACKAGE_DATA_NAME_INDEX],
                            packageInputArray[PACKAGE_DATA_SOURCE_INDEX],
                            packageInputArray[PACKAGE_DATA_DESTINATION_INDEX],
                            Integer.parseInt(packageInputArray[PACKAGE_DATA_WEIGHT_INDEX]));
            mailPackageList.add(mailPackage);
        }
        return mailPackageList;
    }

    private static List<Train> initalizeTrains(String trains[]) {
        List<Train> trainList = new ArrayList<>();
        for (String trainInput : trains) {
            String[] trainInputArray = trainInput.split(",");
            Train train =
                    new Train(
                            trainInputArray[TRAIN_DATA_NAME_INDEX],
                            Integer.parseInt(trainInputArray[TRAIN_DATA_CAPACITY_INDEX]),
                            trainInputArray[TRAIN_DATA_STARTING_LOCATION_INDEX]);
            trainList.add(train);
        }
        return trainList;
    }

    protected static void placeTrainInStations(Context ctx) {
        for (Train train: ctx.getTrains()){
            ctx.getStations().get(train.getStartingPoint()).getTrainsInStation().add(train);
        }
    }
}
