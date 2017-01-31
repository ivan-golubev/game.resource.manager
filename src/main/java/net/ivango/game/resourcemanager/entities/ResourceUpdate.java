package net.ivango.game.resourcemanager.entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class ResourceUpdate {
    public enum UpdateType {
        CREATED,
        UPDATED,
        DELETED
    }

    private UpdateType type;
    private String userName;
    private int resourceId;
    private String game;
}
