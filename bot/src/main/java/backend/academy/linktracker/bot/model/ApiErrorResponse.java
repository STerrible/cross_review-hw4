package backend.academy.linktracker.bot.model;

import java.util.List;

public record ApiErrorResponse(
        String description, String code, String exceptionName, String exceptionMessage, List<String> stacktrace) {}
