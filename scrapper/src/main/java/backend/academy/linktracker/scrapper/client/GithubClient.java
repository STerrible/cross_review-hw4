package backend.academy.linktracker.scrapper.client;

import backend.academy.linktracker.scrapper.properties.GithubProperties;
import java.net.URI;
import java.time.Instant;
import java.util.Arrays;
import java.util.Optional;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class GithubClient implements LinkSourceClient {

    private final RestClient restClient;

    public GithubClient(RestClient.Builder builder, GithubProperties properties) {
        this.restClient = builder.baseUrl("https://api.github.com")
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + properties.getToken())
                .defaultHeader(HttpHeaders.ACCEPT, "application/vnd.github+json")
                .defaultHeader("X-GitHub-Api-Version", "2022-11-28")
                .defaultHeader(HttpHeaders.USER_AGENT, "link-tracker-scrapper")
                .build();
    }

    @Override
    public Optional<Instant> fetchUpdatedAt(URI uri) {
        if (!"github.com".equalsIgnoreCase(uri.getHost())) {
            return Optional.empty();
        }

        String[] segments = Arrays.stream(uri.getPath().split("/"))
                .filter(part -> !part.isBlank())
                .toArray(String[]::new);
        if (segments.length < 2) {
            return Optional.empty();
        }

        try {
            RepoResponse response = restClient
                    .get()
                    .uri("/repos/{owner}/{repo}", segments[0], segments[1])
                    .retrieve()
                    .body(RepoResponse.class);
            return response == null ? Optional.empty() : Optional.ofNullable(response.updatedAt());
        } catch (RuntimeException exception) {
            return Optional.empty();
        }
    }

    private record RepoResponse(
            @com.fasterxml.jackson.annotation.JsonProperty("updated_at")
            Instant updatedAt) {}
}
