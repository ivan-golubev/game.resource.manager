package net.ivango.game.resourcemanager.services;

import net.ivango.game.resourcemanager.entities.ResourceUpdate;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

@Controller
public class ResourceUpdatesController {

    @MessageMapping("/sendupdate")
    @SendTo("/topic/resources")
    public ResourceUpdate onResourceUpdate(ResourceUpdate r) { return r; }

}
