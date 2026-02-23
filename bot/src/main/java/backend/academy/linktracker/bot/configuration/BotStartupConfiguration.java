package backend.academy.linktracker.bot.configuration;

import backend.academy.linktracker.bot.telegram.TelegramClient;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class BotStartupConfiguration {

    private final TelegramClient telegramClient;

    @Bean
    @ConditionalOnProperty(
        prefix = "app.telegram",
        name = "register-commands",
        havingValue = "true",
        matchIfMissing = true
    )
    public ApplicationRunner registerCommands() {
        return args -> telegramClient.registerCommands();
    }
}
