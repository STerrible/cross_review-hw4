package backend.academy.linktracker.scrapper.repository;

import backend.academy.linktracker.scrapper.model.LinkResponse;
import java.net.URI;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.stereotype.Repository;

@Repository
public class InMemorySubscriptionRepository {

    private final Set<Long> chats = ConcurrentHashMap.newKeySet();
    private final Map<Long, Map<URI, StoredLink>> byChat = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1);

    public void registerChat(long chatId) {
        chats.add(chatId);
        byChat.computeIfAbsent(chatId, ignored -> new ConcurrentHashMap<>());
    }

    public boolean deleteChat(long chatId) {
        boolean existed = chats.remove(chatId);
        byChat.remove(chatId);
        return existed;
    }

    public boolean chatExists(long chatId) {
        return chats.contains(chatId);
    }

    public LinkResponse addLink(long chatId, URI link, List<String> tags, List<String> filters) {
        Map<URI, StoredLink> links = byChat.computeIfAbsent(chatId, ignored -> new ConcurrentHashMap<>());
        StoredLink candidate =
                new StoredLink(idGenerator.getAndIncrement(), link, List.copyOf(tags), List.copyOf(filters));
        StoredLink previous = links.putIfAbsent(link, candidate);
        return (previous == null ? candidate : previous).toResponse();
    }

    public boolean hasLink(long chatId, URI link) {
        Map<URI, StoredLink> links = byChat.get(chatId);
        return links != null && links.containsKey(link);
    }

    public LinkResponse removeLink(long chatId, URI link) {
        Map<URI, StoredLink> links = byChat.get(chatId);
        if (links == null) {
            return null;
        }
        StoredLink removed = links.remove(link);
        return removed == null ? null : removed.toResponse();
    }

    public List<LinkResponse> links(long chatId) {
        Map<URI, StoredLink> links = byChat.get(chatId);
        if (links == null) {
            return List.of();
        }
        return links.values().stream().map(StoredLink::toResponse).toList();
    }

    public Set<URI> allTrackedUris() {
        Set<URI> uris = ConcurrentHashMap.newKeySet();
        byChat.values().forEach(map -> uris.addAll(map.keySet()));
        return uris;
    }

    public Collection<StoredLink> allLinks() {
        List<StoredLink> links = new ArrayList<>();
        byChat.values().forEach(map -> links.addAll(map.values()));
        return links;
    }

    public List<Long> chatsTracking(URI link) {
        List<Long> result = new ArrayList<>();
        byChat.forEach((chatId, links) -> {
            if (links.containsKey(link)) {
                result.add(chatId);
            }
        });
        return result;
    }

    public static final class StoredLink {
        private final long id;
        private final URI uri;
        private final List<String> tags;
        private final List<String> filters;
        private volatile Instant lastSeenUpdatedAt = Instant.EPOCH;

        private StoredLink(long id, URI uri, List<String> tags, List<String> filters) {
            this.id = id;
            this.uri = uri;
            this.tags = Collections.unmodifiableList(tags);
            this.filters = Collections.unmodifiableList(filters);
        }

        public URI uri() {
            return uri;
        }

        public long id() {
            return id;
        }

        public Instant lastSeenUpdatedAt() {
            return lastSeenUpdatedAt;
        }

        public void lastSeenUpdatedAt(Instant updatedAt) {
            this.lastSeenUpdatedAt = updatedAt;
        }

        public LinkResponse toResponse() {
            return new LinkResponse(id, uri, tags, filters);
        }
    }
}
