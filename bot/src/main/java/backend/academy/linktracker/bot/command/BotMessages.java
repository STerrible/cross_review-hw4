package backend.academy.linktracker.bot.command;

public final class BotMessages {

    public static final String HELP_TEXT = """
        /start — начать работу
        /help — список доступных команд
        /track — добавить ссылку в отслеживание
        /untrack — удалить ссылку из отслеживания
        /list [tag] — показать отслеживаемые ссылки
        /cancel — отменить текущий диалог
        """;

    public static final String FALLBACK_PROCESSING_ERROR = "Не удалось обработать команду. Попробуйте ещё раз позже.";

    private BotMessages() {}
}
