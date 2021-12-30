package models;

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

    public Context(Map<String, Station> stations,List<Route> routes,List<MailPackage> mailPackages, List<Train> trains){
        this.stations = stations;
        this.routes = routes;
        this.mailPackages = mailPackages;
        this.trains = trains;
    }

    public int getDistanceOfRoute(String routeName){
        for (Route route: getRoutes()){
            if (route.getRouteName().equalsIgnoreCase(routeName)){
                return route.getTime();
            }
        }
        return 0;
    }

    public String determineRouteFromLocation(
            String currentLocation, String destination) {
        for (Route route : routes) {
            if ((route.getStationA().equalsIgnoreCase(currentLocation)
                    || route.getStationB().equalsIgnoreCase(destination)
                    && ((route.getStationA().equalsIgnoreCase(destination))
                    || route.getStationB().equalsIgnoreCase(currentLocation)))) {
                return route.getRouteName();
            }
        }
        return "invalid route";
    }
}
