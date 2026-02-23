package backend.academy.linktracker.bot.command;

import java.util.Map;

import static backend.academy.linktracker.bot.command.Replies.*;

public class CommandDispatcher {

    private final Map<String, String> commands = Map.of(
        "/start", START,
        "/help", HELP
    );

    public String dispatch(String messageText) {
        String command = CommandParser.extract(messageText);
        return commands.getOrDefault(command, UNKNOWN);
    }
}
