package backend.academy.linktracker.bot.telegram;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.BotCommand;
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
        var resp = bot.execute(new SendMessage(chatId, text));
        if (!resp.isOk()) {
            log.error("telegram_send_failed description={}", resp.description());
        } else {
            log.info("telegram_message_sent chatId={}", chatId);
        }
    }

    @Override
    public void registerCommands() {
        var resp = bot.execute(new SetMyCommands(
            new BotCommand("start", "Начать работу"),
            new BotCommand("help", "Список команд")
        ));

        if (!resp.isOk()) {
            log.error("telegram_set_commands_failed description={}", resp.description());
        } else {
            log.info("telegram_commands_registered");
        }
    }
}
