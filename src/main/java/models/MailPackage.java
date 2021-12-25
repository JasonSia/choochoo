package models;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class MailPackage {

    private String name;
    private String source;
    private String destination;
    private int weight;
}
