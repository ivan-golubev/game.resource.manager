package net.ivango.game.resourcemanager.entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class Group {
    private int id;
    private String name, description, url;
    private double proportion;
}
