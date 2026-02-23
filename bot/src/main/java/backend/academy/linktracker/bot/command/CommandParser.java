package backend.academy.linktracker.bot.command;

public final class CommandParser {

    private CommandParser() {}

    public static String extract(String text) {
        if (text == null || !text.startsWith("/")) {
            return "";
        }

        String first = text.trim().split("\\s+")[0];
        return first.split("@")[0];
    }
}
