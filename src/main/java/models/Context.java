package models;

import dijkstra.Graph;
import dijkstra.Node;
import lombok.Getter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Getter
public class Context {
    Map<String, Station> stations = new HashMap<>();
    List<Route> routes = new ArrayList<>();
    List<MailPackage> mailPackages = new ArrayList<>();
    List<Train> trains = new ArrayList<>();
    Map<String, Node> nodes = new HashMap<>();
    Graph graph = new Graph();

    public Context(Map<String, Station> stations,List<Route> routes,List<MailPackage> mailPackages, List<Train> trains){
        this.stations = stations;
        this.routes = routes;
        this.mailPackages = mailPackages;
        this.trains = trains;
    }
}
