package backend.academy.linktracker.bot.telegram;

import backend.academy.linktracker.bot.service.UpdateService;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Update;
import jakarta.annotation.PostConstruct;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class TelegramUpdatesListener implements UpdatesListener {

    private static final Logger log = LoggerFactory.getLogger(TelegramUpdatesListener.class);

    private final TelegramBot bot;
    private final UpdateService updateService;

    public TelegramUpdatesListener(TelegramBot bot, UpdateService updateService) {
        this.bot = bot;
        this.updateService = updateService;
    }

    @PostConstruct
    public void start() {
        bot.setUpdatesListener(this);
        log.info("telegram_updates_listener_started");
    }

    @Override
    public int process(List<Update> updates) {
        for (Update update : updates) {
            if (update.message() == null || update.message().text() == null) {
                continue;
            }
            long chatId = update.message().chat().id();
            String text = update.message().text();

            updateService.handle(chatId, text);
        }
        return UpdatesListener.CONFIRMED_UPDATES_ALL;
    }
}
