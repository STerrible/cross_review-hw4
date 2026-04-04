package backend.academy.linktracker.bot.client;

import backend.academy.linktracker.bot.model.AddLinkRequest;
import backend.academy.linktracker.bot.model.LinkResponse;
import backend.academy.linktracker.bot.model.ListLinksResponse;
import java.net.URI;

public interface ScrapperClient {
    void registerChat(long chatId);

    LinkResponse addLink(long chatId, AddLinkRequest request);

    LinkResponse removeLink(long chatId, URI link);

    ListLinksResponse getLinks(long chatId);
}
