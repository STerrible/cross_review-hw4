package backend.academy.linktracker.bot.command;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public enum BotCommand {
    START("/start"),
    HELP("/help"),
    TRACK("/track"),
    UNTRACK("/untrack"),
    LIST("/list"),
    CANCEL("/cancel"),
    UNKNOWN("");

    private static final Map<String, BotCommand> COMMANDS = Arrays.stream(values())
            .filter(command -> !command.commandText.isBlank())
            .collect(Collectors.toMap(BotCommand::commandText, Function.identity()));

    private final String commandText;

    BotCommand(String commandText) {
        this.commandText = commandText;
    }

    public static BotCommand fromText(String commandText) {
        return COMMANDS.getOrDefault(commandText, UNKNOWN);
    }

    public String commandText() {
        return commandText;
    }
}
