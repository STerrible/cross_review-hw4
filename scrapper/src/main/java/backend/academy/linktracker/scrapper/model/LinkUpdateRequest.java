package backend.academy.linktracker.scrapper.model;

import java.util.List;

public record LinkUpdateRequest(Long id, String url, String description, List<Long> tgChatIds) {}
