package backend.academy.linktracker.bot.service;

import backend.academy.linktracker.bot.command.CommandDispatcher;
import backend.academy.linktracker.bot.telegram.TelegramClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class UpdateService {

    private static final Logger log = LoggerFactory.getLogger(UpdateService.class);

    private final TelegramClient telegramClient;
    private final CommandDispatcher dispatcher;

    public UpdateService(TelegramClient telegramClient) {
        this.telegramClient = telegramClient;
        this.dispatcher = new CommandDispatcher();
    }

    public void handle(long chatId, String text) {
        log.info("message_received chatId={} text={}", chatId, text);

        String reply = dispatcher.dispatch(text);
        telegramClient.sendMessage(chatId, reply);

        log.info("message_sent chatId={}", chatId);
    }
}
