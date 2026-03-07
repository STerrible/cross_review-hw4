package backend.academy.linktracker.bot;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

import backend.academy.linktracker.bot.command.CommandDispatcher;
import backend.academy.linktracker.bot.service.UpdateService;
import backend.academy.linktracker.bot.telegram.TelegramClient;
import org.junit.jupiter.api.Test;

class UpdateServiceTest {

    @Test
    void handleSendsDispatchedReplyToTelegramClient() {
        RecordingTelegramClient telegramClient = new RecordingTelegramClient();
        UpdateService service = new UpdateService(telegramClient, new CommandDispatcher());

        service.handle(100L, "/help");

        assertEquals(100L, telegramClient.lastChatId);
        assertEquals("/start — начать работу\n/help — список доступных команд", telegramClient.lastText);
    }

    @Test
    void handleSwallowsRuntimeExceptionFromTelegramClient() {
        TelegramClient telegramClient = new ThrowingTelegramClient();
        UpdateService service = new UpdateService(telegramClient, new CommandDispatcher());

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
}
