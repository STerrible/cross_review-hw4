package backend.academy.linktracker.bot.command;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public enum BotCommand {
    START("/start", "Добро пожаловать! Используйте /help, чтобы посмотреть доступные команды."),
    HELP("/help", "/start — начать работу\n/help — список доступных команд"),
    UNKNOWN("", "Неизвестная команда. Воспользуйтесь /help, чтобы посмотреть список доступных команд.");

    private static final Map<String, BotCommand> COMMANDS = Arrays.stream(values())
            .filter(command -> !command.commandText.isBlank())
            .collect(Collectors.toMap(BotCommand::commandText, Function.identity()));

    private final String commandText;
    private final String reply;

    BotCommand(String commandText, String reply) {
        this.commandText = commandText;
        this.reply = reply;
    }

    public static BotCommand fromText(String commandText) {
        return COMMANDS.getOrDefault(commandText, UNKNOWN);
    }

    public String reply() {
        return reply;
    }

    public String commandText() {
        return commandText;
    }
}
