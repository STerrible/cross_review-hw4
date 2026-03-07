package backend.academy.linktracker.bot.telegram;

import com.pengrad.telegrambot.request.SendMessage;

public interface TelegramClient {
    void sendMessage(long chatId, String text);

    default void sendMessage(SendMessage sendMessage) {
        Object chatId = sendMessage.getParameters().get("chat_id");
        Object text = sendMessage.getParameters().get("text");
        sendMessage(((Number) chatId).longValue(), String.valueOf(text));
    }

    void registerCommands();
}
