import dijkstra.Dijkstra;
import dijkstra.Graph;
import dijkstra.Node;
import models.Context;
import models.Route;
import models.Station;

import java.util.stream.Collectors;

public class Main {

  public static void main(String args[]) {
    System.out.println("initializing choo choo");
    Context ctx = InitializeSystem.readInput(args);
    // set all mail packages to stations
    generatePackagesInRespectiveStations(ctx);

    // generate routing map
    generateRoutingMap(ctx);

    //to get shortest path from A to anywhere.
    Graph shortestPathGraph = Dijkstra.calculateShortestPathFromSource(ctx.getGraph(), ctx.getNodes().get("A"));
    
    // loop through stations
    // find closest trains to stations to start transporting with greedy algorithm
    // station controlling trains to come
    // similar to grab on demand service

    System.out.println(ctx);
  }

  private static void generateRoutingMap(Context ctx) {
    for (Station station : ctx.getStations()) {
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

  private static void generatePackagesInRespectiveStations(Context ctx) {
    for (Station station : ctx.getStations()) {
      station.setMailPackages(
          ctx.getMailPackages().stream()
              .filter(p -> p.getSource().equalsIgnoreCase(station.getName()))
              .collect(Collectors.toList()));
    }
  }
}
