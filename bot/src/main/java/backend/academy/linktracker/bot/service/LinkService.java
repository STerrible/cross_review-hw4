package backend.academy.linktracker.bot.service;

import backend.academy.linktracker.bot.client.ScrapperClient;
import backend.academy.linktracker.bot.model.AddLinkRequest;
import backend.academy.linktracker.bot.model.LinkResponse;
import java.net.URI;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;

@Service
public class LinkService {

    private final ScrapperClient scrapperClient;

    public LinkService(ScrapperClient scrapperClient) {
        this.scrapperClient = scrapperClient;
    }

    public AddLinkResult addLink(long chatId, URI link, List<String> tags) {
        try {
            scrapperClient.addLink(chatId, new AddLinkRequest(link, tags, List.of()));
            return AddLinkResult.SUCCESS;
        } catch (HttpClientErrorException.Conflict exception) {
            return AddLinkResult.ALREADY_TRACKED;
        } catch (HttpClientErrorException.NotFound exception) {
            return AddLinkResult.CHAT_NOT_REGISTERED;
        } catch (RuntimeException exception) {
            return AddLinkResult.SCRAPPER_UNAVAILABLE;
        }
    }

    public RemoveLinkResult removeLink(long chatId, URI link) {
        try {
            LinkResponse response = scrapperClient.removeLink(chatId, link);
            return new RemoveLinkResult(RemoveLinkStatus.SUCCESS, response);
        } catch (HttpClientErrorException exception) {
            return new RemoveLinkResult(RemoveLinkStatus.LINK_NOT_FOUND, null);
        } catch (RuntimeException exception) {
            return new RemoveLinkResult(RemoveLinkStatus.SCRAPPER_UNAVAILABLE, null);
        }
    }

    public ListLinksResult getLinks(long chatId) {
        try {
            return new ListLinksResult(ListLinksStatus.SUCCESS, scrapperClient.getLinks(chatId).links());
        } catch (HttpClientErrorException.NotFound exception) {
            return new ListLinksResult(ListLinksStatus.CHAT_NOT_REGISTERED, List.of());
        } catch (RuntimeException exception) {
            return new ListLinksResult(ListLinksStatus.SCRAPPER_UNAVAILABLE, List.of());
        }
    }

    public enum AddLinkResult {
        SUCCESS,
        ALREADY_TRACKED,
        CHAT_NOT_REGISTERED,
        SCRAPPER_UNAVAILABLE
    }

    public enum RemoveLinkStatus {
        SUCCESS,
        LINK_NOT_FOUND,
        SCRAPPER_UNAVAILABLE
    }

    public enum ListLinksStatus {
        SUCCESS,
        CHAT_NOT_REGISTERED,
        SCRAPPER_UNAVAILABLE
    }

    public record RemoveLinkResult(RemoveLinkStatus status, LinkResponse removedLink) {}

    public record ListLinksResult(ListLinksStatus status, List<LinkResponse> links) {}
}
