package models;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
public class Station {

    @Setter(AccessLevel.NONE)
    private String name;
    private List<Route> adjacentRoute = new ArrayList();
    private List<MailPackage> mailPackages = new ArrayList();

    public Station (String name){
        this.name = name;
    }
}
