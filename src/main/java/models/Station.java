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
    private List<Train> trainsInStation = new ArrayList();

    public Station (String name){
        this.name = name;
    }

    public List<MailPackage> getUndeliveredPackage(){
        List<MailPackage> undeliveredPackage = new ArrayList<>();
        for (MailPackage mp: mailPackages){
            if(mp.getStatus() == MailPackage.TO_DELIVER){
                undeliveredPackage.add(mp);
            }
        }
        return undeliveredPackage;
    }

    public boolean isAllPackageInStationDelivered(){
        for (MailPackage mp: mailPackages){
            if(mp.getStatus() != MailPackage.DELIVERED){
              return false;
            }
        }
        return true;
    }

}
