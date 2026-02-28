package backend.academy.linktracker.bot;

import static org.junit.jupiter.api.Assertions.assertEquals;

import backend.academy.linktracker.bot.command.CommandDispatcher;
import backend.academy.linktracker.bot.command.Replies;
import org.junit.jupiter.api.Test;

class CommandDispatcherTest {

    private final CommandDispatcher dispatcher = new CommandDispatcher();

    @Test
    void startCommandReturnsWelcomeMessage() {
        assertEquals(Replies.START, dispatcher.dispatch("/start"));
    }

    @Test
    void helpCommandReturnsHelpMessage() {
        assertEquals(Replies.HELP, dispatcher.dispatch("/help"));
    }

    @Test
    void unknownCommandReturnsUnknownMessage() {
        assertEquals(Replies.UNKNOWN, dispatcher.dispatch("/abracadabra"));
    }
}
