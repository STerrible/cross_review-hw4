package backend.academy.linktracker.scrapper.client;

import java.net.URI;
import java.time.Instant;
import java.util.Optional;

public interface LinkSourceClient {
    Optional<Instant> fetchUpdatedAt(URI uri);
}
