package backend.academy.linktracker.bot.command;

import com.pengrad.telegrambot.request.SendMessage;
import org.springframework.stereotype.Component;

@Component
public class CommandDispatcher {

    public SendMessage dispatch(long chatId, String messageText) {
        String commandText = CommandParser.extract(messageText);
        String reply = BotCommand.fromText(commandText).reply();
        return new SendMessage(chatId, reply);
    }
}
