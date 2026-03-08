package backend.academy.linktracker.scrapper.client;

import backend.academy.linktracker.scrapper.model.LinkUpdateRequest;

public interface BotClient {
    void sendUpdate(LinkUpdateRequest update);
}
