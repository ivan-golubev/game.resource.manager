package net.ivango.game.resourcemanager.rest;

import net.ivango.game.resourcemanager.entities.GameResource;
import net.ivango.game.resourcemanager.entities.ResourceId;
import net.ivango.game.resourcemanager.entities.ResourceUpdate;
import net.ivango.game.resourcemanager.services.ResourceManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/resources")
public class ResManagerController {

    @Autowired
    private ResourceManager resourceManager;
    @Autowired
    private SimpMessagingTemplate template;

    /**
     * Creates a new resource.
     * Specified id is ignored, server assigns a new id to this resource.
     * Specified owner is ignored, server fills the field automatically.
     * @return id of the created resource
     * */
    @PostMapping(produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public @ResponseBody ResourceId createResource(@RequestParam(value = "game") String game,
                                                   @RequestBody GameResource gameResource,
                                                   Principal principal) {

        ResourceId resourceId = new ResourceId(
                resourceManager.create(game, gameResource, principal)
        );
        template.convertAndSend(
                "/topic/resources",
                new ResourceUpdate(
                        ResourceUpdate.UpdateType.CREATED,
                        principal.getName(),
                        resourceId.getResourceId(),
                        game
                )
        );
        return resourceId;
    }

    /**
     * Get a list of resources for the specified game
     * @return empty list if game does not exist
     * */
    @GetMapping(produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public @ResponseBody List<GameResource> getResources(@RequestParam(value = "game") String game) {
        return resourceManager.get(game);
    }

    /**
     * Get a resource by id and game
     * @return NOT_FOUND status if the resource has been found
     * */
    @GetMapping(path = "{id}", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<?> getResource(@RequestParam(value = "game") String game,
                                         @PathVariable("id") int resourceId) {

        Optional<GameResource> resourceOp = resourceManager.get(game, resourceId);
        if ( resourceOp.isPresent() ) {
            return new ResponseEntity<>(resourceOp.get(), HttpStatus.OK);
        } else {
            return new ResponseEntity<String>(HttpStatus.NOT_FOUND);
        }
    }

    /**
     * Updates the existing resource.
     * @return OK status if updated,
     *         NOT_FOUND status if not found,
     *         FORBIDDEN status if user is not allowed to perform this operation.
     * */
    @PutMapping(path = "{id}")
    public ResponseEntity<?> updateResource(@RequestParam(value = "game") String game,
                                            @RequestBody GameResource gameResource,
                                            @PathVariable("id") int resourceId,
                                            Principal principal) {
        try {
            boolean success = resourceManager.update(game, gameResource, resourceId, principal);
            if (success) {
                template.convertAndSend(
                        "/topic/resources",
                        new ResourceUpdate(
                                ResourceUpdate.UpdateType.UPDATED,
                                principal.getName(),
                                resourceId,
                                game
                        )
                );
            }
            return new ResponseEntity<String>(success ? HttpStatus.OK : HttpStatus.NOT_FOUND);
        } catch (SecurityException se) {
            return new ResponseEntity<String>(HttpStatus.FORBIDDEN);
        }
    }

    /**
     * Deletes the existing resource if it exists
     * @return OK status if deleted or NOT_FOUND status otherwise
     * */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<?> deleteResource(@RequestParam(value = "game") String game,
                                            @PathVariable("id") int resourceId,
                                            Principal principal) {
        try {
            boolean success = resourceManager.delete(game, resourceId, principal);
            if (success) {
                template.convertAndSend(
                        "/topic/resources",
                        new ResourceUpdate(
                                ResourceUpdate.UpdateType.DELETED,
                                principal.getName(),
                                resourceId,
                                game
                        )
                );
            }
            return new ResponseEntity<String>(success ? HttpStatus.OK : HttpStatus.NOT_FOUND);
        } catch (SecurityException se) {
            return new ResponseEntity<String>(HttpStatus.FORBIDDEN);
        }
    }

}