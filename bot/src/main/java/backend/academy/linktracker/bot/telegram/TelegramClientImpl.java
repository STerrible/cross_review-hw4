package backend.academy.linktracker.bot.telegram;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.request.SetMyCommands;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class TelegramClientImpl implements TelegramClient {

    private static final Logger log = LoggerFactory.getLogger(TelegramClientImpl.class);

    private final TelegramBot bot;

    public TelegramClientImpl(TelegramBot bot) {
        this.bot = bot;
    }

    @Override
    public void sendMessage(long chatId, String text) {
        SendMessage sendMessage = new SendMessage(chatId, text);
        try {
            var response = bot.execute(sendMessage);
            if (!response.isOk()) {
                log.warn("telegram_send_failed");
                return;
            }
            log.info("telegram_message_sent");
        } catch (RuntimeException exception) {
            log.atWarn().setCause(exception).log("telegram_send_failed");
        }
    }

    @Override
    public void registerCommands() {
        try {
            var response = bot.execute(new SetMyCommands(TelegramRegistrationCommand.toApiCommands()));
            if (!response.isOk()) {
                log.warn("telegram_set_commands_failed");
                return;
            }
            log.info("telegram_commands_registered");
        } catch (RuntimeException exception) {
            log.atWarn().setCause(exception).log("telegram_set_commands_failed");
        }
    }
}
