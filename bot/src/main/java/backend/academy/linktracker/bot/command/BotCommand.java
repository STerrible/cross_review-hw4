package backend.academy.linktracker.bot.command;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public enum BotCommand {
    START("/start", "Начать работу"),
    HELP("/help", "Список команд"),
    TRACK("/track", "Добавить ссылку в отслеживание"),
    UNTRACK("/untrack", "Удалить ссылку из отслеживания"),
    LIST("/list", "Показать список ссылок"),
    CANCEL("/cancel", "Отменить диалог"),
    UNKNOWN("", "");

    private static final Map<String, BotCommand> COMMANDS = Arrays.stream(values())
            .filter(command -> !command.commandText.isBlank())
            .collect(Collectors.toMap(BotCommand::commandText, Function.identity()));

    private final String commandText;
    private final String description;

    BotCommand(String commandText, String description) {
        this.commandText = commandText;
        this.description = description;
    }

    public static BotCommand fromText(String commandText) {
        return COMMANDS.getOrDefault(commandText, UNKNOWN);
    }

    public String commandText() {
        return commandText;
    }

    public com.pengrad.telegrambot.model.BotCommand toApiCommand() {
        return new com.pengrad.telegrambot.model.BotCommand(commandText.substring(1), description);
    }

    public static com.pengrad.telegrambot.model.BotCommand[] toApiCommands() {
        return Arrays.stream(values())
                .filter(command -> !command.commandText.isBlank())
                .map(BotCommand::toApiCommand)
                .toArray(com.pengrad.telegrambot.model.BotCommand[]::new);
    }
}
