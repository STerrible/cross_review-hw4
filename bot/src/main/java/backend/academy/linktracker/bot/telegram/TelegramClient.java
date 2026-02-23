package backend.academy.linktracker.bot.telegram;

public interface TelegramClient {
    void sendMessage(long chatId, String text);
    void registerCommands();
}
