package backend.academy.linktracker.bot.command;

import java.util.List;
import java.util.regex.Pattern;

public final class CommandParser {

    private static final Pattern WS_PATTERN = Pattern.compile("\\s+");

    private CommandParser() {}

    public static String extract(String text) {
        if (text == null || !text.startsWith("/")) {
            return "";
        }

        String first = WS_PATTERN.split(text.trim())[0];
        return first.split("@")[0];
    }

    public static List<String> arguments(String text) {
        if (text == null) {
            return List.of();
        }
        String trimmed = text.trim();
        if (trimmed.isEmpty()) {
            return List.of();
        }

        String[] tokens = WS_PATTERN.split(trimmed);
        if (!trimmed.startsWith("/") || tokens.length < 2) {
            return List.of();
        }

        return List.of(tokens).subList(1, tokens.length);
    }
}
