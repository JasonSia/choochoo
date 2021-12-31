package models;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Setter
@Getter
public class Station {

    @Setter(AccessLevel.NONE)
    private String name;
    private List<Route> adjacentRoute = new ArrayList();
    private List<MailPackage> mailPackages = new ArrayList();
    private List<Train> trainsInStation = new ArrayList();

    public Station (String name){
        this.name = name;
    }


    public void removePackage(String packageName){
        this.mailPackages = mailPackages.stream().filter(p -> !p.getName().equalsIgnoreCase(packageName)).collect(Collectors.toList());
    }


}
