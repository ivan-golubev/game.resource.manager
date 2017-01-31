package net.ivango.game.resourcemanager.entities;


import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class GameResourceCollection {

    private Map<Integer, GameResource> resources = new ConcurrentHashMap<>();
    private AtomicInteger counter = new AtomicInteger(0);

    public int addResource(GameResource res, String ownerName) {
        res.setOwner(ownerName);
        res.setId(counter.addAndGet(1));
        res.setCreated(System.currentTimeMillis());
        resources.put(res.getId(), res);
        return res.getId();
    }

    public boolean updateResource(GameResource res, int resourceId, String userName) {
        GameResource existingRes = resources.get(resourceId);
        if (existingRes != null && !existingRes.getOwner().equals(userName)) {
            throw new SecurityException("User is not authorized to update a resource he/she does not own");
        }

        /* just to make sure the provided json contains valid info */
        res.setId(resourceId);
        res.setOwner(userName);
        if (existingRes != null) { res.setCreated(existingRes.getCreated()); }
        res.setUpdated(System.currentTimeMillis());

        return resources.replace(res.getId(), res) != null;
    }

    public Optional<GameResource> getResource(int id) {
        GameResource res = resources.get(id);
        return res != null ? Optional.of(res) : Optional.empty();
    }

    public List<GameResource> getResources() { return new ArrayList<>(resources.values()); }

    public boolean deleteResource(int id, String userName) {
        GameResource existingRes = resources.get(id);
        if (existingRes != null && !existingRes.getOwner().equals(userName)) {
            throw new SecurityException("User is not authorized to delete a resource he/she does not own");
        }
        return resources.remove(id) != null;
    }
}
