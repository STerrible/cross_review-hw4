package backend.academy.linktracker.bot;

import static org.junit.jupiter.api.Assertions.assertEquals;

import backend.academy.linktracker.bot.client.ScrapperClient;
import backend.academy.linktracker.bot.command.CommandDispatcher;
import backend.academy.linktracker.bot.model.AddLinkRequest;
import backend.academy.linktracker.bot.model.LinkResponse;
import backend.academy.linktracker.bot.model.ListLinksResponse;
import backend.academy.linktracker.bot.service.LinkService;
import java.net.URI;
import java.util.List;
import org.junit.jupiter.api.Test;

class CommandDispatcherTest {

    private static final long CHAT_ID = 100L;

    @Test
    void unknownCommandReturnsUnknownReply() {
        ScrapperClient scrapperClient = new StubScrapperClient();
        CommandDispatcher dispatcher = new CommandDispatcher(scrapperClient, new LinkService(scrapperClient));
        assertEquals(
                "Неизвестная команда. Воспользуйтесь /help, чтобы посмотреть список доступных команд.",
                dispatcher.dispatch(CHAT_ID, "/abracadabra").getParameters().get("text"));
    }

    @Test
    void listCommandReturnsEmptyMessage() {
        ScrapperClient scrapperClient = new StubScrapperClient();
        CommandDispatcher dispatcher = new CommandDispatcher(scrapperClient, new LinkService(scrapperClient));
        assertEquals(
                "Список отслеживаемых ссылок пуст.",
                dispatcher.dispatch(CHAT_ID, "/list").getParameters().get("text"));
    }

    private static class StubScrapperClient implements ScrapperClient {

        @Override
        public void registerChat(long chatId) {}

        @Override
        public LinkResponse addLink(long chatId, AddLinkRequest request) {
            return new LinkResponse(1L, request.link(), request.tags(), request.filters());
        }

        @Override
        public LinkResponse removeLink(long chatId, URI link) {
            return new LinkResponse(1L, link, List.of(), List.of());
        }

        @Override
        public ListLinksResponse getLinks(long chatId) {
            return new ListLinksResponse(List.of(), 0);
        }
    }
}
