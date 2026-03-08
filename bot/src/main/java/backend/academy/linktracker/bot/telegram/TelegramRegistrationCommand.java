package backend.academy.linktracker.bot.telegram;

import com.pengrad.telegrambot.model.BotCommand;
import java.util.Arrays;

public enum TelegramRegistrationCommand {
    START("start", "Начать работу"),
    HELP("help", "Список команд"),
    TRACK("track", "Добавить ссылку в отслеживание"),
    UNTRACK("untrack", "Удалить ссылку из отслеживания"),
    LIST("list", "Показать список ссылок"),
    CANCEL("cancel", "Отменить диалог");

    private final String command;
    private final String description;

    TelegramRegistrationCommand(String command, String description) {
        this.command = command;
        this.description = description;
    }

    public BotCommand toApiCommand() {
        return new BotCommand(command, description);
    }

    public static BotCommand[] toApiCommands() {
        return Arrays.stream(values())
                .map(TelegramRegistrationCommand::toApiCommand)
                .toArray(BotCommand[]::new);
    }
}
