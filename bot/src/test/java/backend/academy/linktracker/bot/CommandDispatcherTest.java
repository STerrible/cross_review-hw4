package backend.academy.linktracker.bot;

import static org.junit.jupiter.api.Assertions.assertEquals;

import backend.academy.linktracker.bot.command.BotCommand;
import backend.academy.linktracker.bot.command.CommandDispatcher;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class CommandDispatcherTest {

    private static final long CHAT_ID = 100L;

    private final CommandDispatcher dispatcher = new CommandDispatcher();

    @ParameterizedTest
    @CsvSource({"/start, START", "/help, HELP", "/abracadabra, UNKNOWN"})
    void dispatchReturnsExpectedReply(String inputCommand, BotCommand expectedCommand) {
        assertEquals(
                expectedCommand.reply(),
                dispatcher.dispatch(CHAT_ID, inputCommand).getParameters().get("text"));
    }
}
