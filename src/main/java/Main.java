import models.Context;
import models.Station;
import models.Train;

import java.util.ArrayList;
import java.util.List;

import static models.InitializeSystem.initalizeTrainSystem;

public class Main {

  private static List<Station> stations = new ArrayList<>();
  private static List<Train> trainList = new ArrayList<>();


  public static void main(String args[]) {
    System.out.println("initializing choo choo");
    Context ctx = initalizeTrainSystem(args);
    System.out.println(ctx);
  }


}
