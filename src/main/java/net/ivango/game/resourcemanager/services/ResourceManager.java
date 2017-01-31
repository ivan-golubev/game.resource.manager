package net.ivango.game.resourcemanager.services;

import net.ivango.game.resourcemanager.entities.GameResource;
import net.ivango.game.resourcemanager.entities.GameResourceCollection;
import org.springframework.stereotype.Component;

import java.security.Principal;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ResourceManager {

    private Map<String, GameResourceCollection> resources = new ConcurrentHashMap<>();

    /**
     * Creates a resource and returns it's id
     * */
    public int create(String game, GameResource gameResource, Principal principal) {
        resources.putIfAbsent(game, new GameResourceCollection());
        return resources.get(game).addResource(gameResource, principal.getName());
    }

    /**
     * @return all resources for the specified game
     * */
    public List<GameResource> get(String game) {
        GameResourceCollection gameResources = resources.get(game);
        return gameResources != null ? gameResources.getResources() : Collections.emptyList();
    }

    /**
     * @return a resource with the specified id for the specified game
     * */
    public Optional<GameResource> get(String game, int resourceId) {
        GameResourceCollection gameResources = resources.get(game);
        return gameResources != null ? gameResources.getResource(resourceId) : Optional.empty();
    }

    /**
     * Updates the existing resource
     * @return true if operation was successful, false otherwise
     * */
    public boolean update(String game, GameResource gameResource, int resourceId, Principal principal) {
        return resources.containsKey(game) && resources.get(game).updateResource(gameResource, resourceId, principal.getName());
    }

    /**
     * Deletes this resource if exists within the scope of the specified game
     * @return true if operation was successful, false otherwise
     * */
    public boolean delete(String game, int resourceId, Principal principal) {
        return resources.containsKey(game) && resources.get(game).deleteResource(resourceId, principal.getName());
    }

}
