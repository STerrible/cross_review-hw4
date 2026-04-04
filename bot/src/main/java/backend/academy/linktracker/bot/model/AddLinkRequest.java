package backend.academy.linktracker.bot.model;

import java.net.URI;
import java.util.List;

public record AddLinkRequest(URI link, List<String> tags, List<String> filters) {}
