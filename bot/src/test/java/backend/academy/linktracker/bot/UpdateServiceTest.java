package backend.academy.linktracker.bot;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import backend.academy.linktracker.bot.client.ScrapperClient;
import backend.academy.linktracker.bot.command.CommandDispatcher;
import backend.academy.linktracker.bot.model.AddLinkRequest;
import backend.academy.linktracker.bot.model.LinkResponse;
import backend.academy.linktracker.bot.model.LinkUpdateRequest;
import backend.academy.linktracker.bot.model.ListLinksResponse;
import backend.academy.linktracker.bot.service.LinkService;
import backend.academy.linktracker.bot.service.UpdateService;
import backend.academy.linktracker.bot.telegram.TelegramClient;
import java.net.URI;
import java.util.List;
import org.junit.jupiter.api.Test;

class UpdateServiceTest {

    @Test
    void handleSendsDispatchedReplyToTelegramClient() {
        RecordingTelegramClient telegramClient = new RecordingTelegramClient();
        ScrapperClient scrapperClient = new StubScrapperClient();
        UpdateService service =
            new UpdateService(telegramClient, new CommandDispatcher(scrapperClient, new LinkService(scrapperClient)));

        service.handle(100L, "/help");

        assertEquals(100L, telegramClient.lastChatId);
        assertTrue(telegramClient.lastText.contains("/track"));
    }

    @Test
    void handleLinkUpdateSwallowsRuntimeExceptionFromTelegramClient() {
        TelegramClient telegramClient = new ThrowingTelegramClient();
        ScrapperClient scrapperClient = new StubScrapperClient();
        UpdateService service =
            new UpdateService(telegramClient, new CommandDispatcher(scrapperClient, new LinkService(scrapperClient)));

        assertDoesNotThrow(() -> service.handleLinkUpdate(
            new LinkUpdateRequest(1L, "https://github.com/user/repo", "changed", List.of(100L))));
    }

    @Test
    void handleSwallowsRuntimeExceptionFromTelegramClient() {
        TelegramClient telegramClient = new ThrowingTelegramClient();
        ScrapperClient scrapperClient = new StubScrapperClient();
        UpdateService service =
            new UpdateService(telegramClient, new CommandDispatcher(scrapperClient, new LinkService(scrapperClient)));

        assertDoesNotThrow(() -> service.handle(100L, "/help"));
    }

    private static final class RecordingTelegramClient implements TelegramClient {
        private long lastChatId;
        private String lastText;

        @Override
        public void sendMessage(long chatId, String text) {
            lastChatId = chatId;
            lastText = text;
        }

        @Override
        public void registerCommands() {}
    }

    private static final class ThrowingTelegramClient implements TelegramClient {

        @Override
        public void sendMessage(long chatId, String text) {
            throw new RuntimeException("send failed");
        }

        @Override
        public void registerCommands() {}
    }

    private static final class StubScrapperClient implements ScrapperClient {

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
