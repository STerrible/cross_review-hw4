package backend.academy.linktracker.scrapper;

import static org.junit.jupiter.api.Assertions.assertEquals;

import backend.academy.linktracker.scrapper.client.BotClient;
import backend.academy.linktracker.scrapper.client.LinkSourceClient;
import backend.academy.linktracker.scrapper.model.LinkUpdateRequest;
import backend.academy.linktracker.scrapper.repository.InMemorySubscriptionRepository;
import backend.academy.linktracker.scrapper.scheduler.LinkUpdateScheduler;
import java.net.URI;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;

class LinkUpdateSchedulerTest {

    @Test
    void schedulerSendsSingleUpdateForSameLinkTrackedByMultipleChats() {
        InMemorySubscriptionRepository repository = new InMemorySubscriptionRepository();
        URI link = URI.create("https://github.com/user/repo");

        repository.registerChat(1L);
        repository.registerChat(2L);
        repository.addLink(1L, link, List.of(), List.of());
        repository.addLink(2L, link, List.of(), List.of());

        StubLinkSourceClient sourceClient = new StubLinkSourceClient(Instant.parse("2024-01-01T00:00:00Z"));
        RecordingBotClient botClient = new RecordingBotClient();
        LinkUpdateScheduler scheduler = new LinkUpdateScheduler(repository, List.of(sourceClient), botClient);

        scheduler.checkUpdates();
        scheduler.checkUpdates();

        assertEquals(1, botClient.calls.get());
        assertEquals(List.of(1L, 2L), botClient.lastUpdate.tgChatIds());
    }

    private static final class StubLinkSourceClient implements LinkSourceClient {

        private final Instant updatedAt;

        private StubLinkSourceClient(Instant updatedAt) {
            this.updatedAt = updatedAt;
        }

        @Override
        public Optional<Instant> fetchUpdatedAt(URI uri) {
            return Optional.of(updatedAt);
        }
    }

    private static final class RecordingBotClient implements BotClient {

        private final AtomicInteger calls = new AtomicInteger();
        private LinkUpdateRequest lastUpdate;

        @Override
        public void sendUpdate(LinkUpdateRequest update) {
            calls.incrementAndGet();
            lastUpdate = update;
        }
    }
}
