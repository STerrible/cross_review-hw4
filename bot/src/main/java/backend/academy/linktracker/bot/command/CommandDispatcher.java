package backend.academy.linktracker.bot.command;

import static backend.academy.linktracker.bot.command.Replies.HELP;
import static backend.academy.linktracker.bot.command.Replies.START;
import static backend.academy.linktracker.bot.command.Replies.UNKNOWN;

import java.util.Map;

public class CommandDispatcher {

    private final Map<String, String> commands = Map.of("/start", START, "/help", HELP);

    public String dispatch(String messageText) {
        String command = CommandParser.extract(messageText);
        return commands.getOrDefault(command, UNKNOWN);
    }
}
