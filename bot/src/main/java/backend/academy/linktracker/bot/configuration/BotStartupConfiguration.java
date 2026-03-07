package backend.academy.linktracker.bot.configuration;

import backend.academy.linktracker.bot.telegram.TelegramClient;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class BotStartupConfiguration {

    private static final Logger log = LoggerFactory.getLogger(BotStartupConfiguration.class);

    private final TelegramClient telegramClient;

    @Bean
    @ConditionalOnProperty(
        prefix = "app.telegram",
        name = "register-commands",
        havingValue = "true",
        matchIfMissing = true)
    public ApplicationRunner registerCommands() {
        return args -> {
            try {
                telegramClient.registerCommands();
            } catch (RuntimeException exception) {
                log.warn("telegram_register_commands_on_startup_failed", exception);
            }
        };
    }
}
