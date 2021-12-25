package models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;


@AllArgsConstructor
@Getter
public class Context {
    List<Station> stations = new ArrayList();
    List<Route> routes = new ArrayList<>();
    List<MailPackage> mailPackages = new ArrayList<>();
    List<Train> trains = new ArrayList<>();
}
