package net.ivango.game.resourcemanager.entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class GameResource {
    private int id;
    private String name, owner;
    private long created, updated;
    private List<Group> groups;
    private List<Map<String, String>> description;
    private String status, environment;
}
