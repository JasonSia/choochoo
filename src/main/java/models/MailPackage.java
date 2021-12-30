package models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Setter
@Getter
public class MailPackage {

  public static final int TO_DELIVER = 0;
  public static final int DELIVERING = 1;
  public static final int DELIVERED = 2;

  private String name;
  private String source;
  private String destination;
  private int weight;
  private int status = TO_DELIVER;

  public String toString() {
    return name;
  }
}
