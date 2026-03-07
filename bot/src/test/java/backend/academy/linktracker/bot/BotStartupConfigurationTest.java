package backend.academy.linktracker.bot;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import backend.academy.linktracker.bot.configuration.BotStartupConfiguration;
import backend.academy.linktracker.bot.telegram.TelegramClient;
import org.junit.jupiter.api.Test;
import org.springframework.boot.DefaultApplicationArguments;

class BotStartupConfigurationTest {

    @Test
    void registerCommandsSwallowsRuntimeException() {
        TelegramClient telegramClient = mock(TelegramClient.class);
        doThrow(new RuntimeException("boom")).when(telegramClient).registerCommands();

        BotStartupConfiguration configuration = new BotStartupConfiguration(telegramClient);

        assertDoesNotThrow(() -> configuration.registerCommands().run(new DefaultApplicationArguments(new String[0])));
        verify(telegramClient).registerCommands();
    }
}
