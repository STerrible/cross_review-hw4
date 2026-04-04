package backend.academy.linktracker.scrapper.client;

import backend.academy.linktracker.scrapper.properties.GithubProperties;
import java.net.URI;
import java.time.Instant;
import java.util.Arrays;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

@Component
public class GithubClient implements LinkSourceClient {

    private static final Logger log = LoggerFactory.getLogger(GithubClient.class);
    private static final String GITHUB_HOST = "github.com";
    private static final String GITHUB_API_BASE_URL = "https://api.github.com";
    private static final String GITHUB_API_VERSION = "2022-11-28";
    private static final String USER_AGENT = "link-tracker-scrapper";

    private final RestClient restClient;

    public GithubClient(RestClient.Builder builder, GithubProperties properties) {
        this.restClient = builder.baseUrl(GITHUB_API_BASE_URL)
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + properties.getToken())
                .defaultHeader(HttpHeaders.ACCEPT, "application/vnd.github+json")
                .defaultHeader("X-GitHub-Api-Version", GITHUB_API_VERSION)
                .defaultHeader(HttpHeaders.USER_AGENT, USER_AGENT)
                .build();
    }

    @Override
    public Optional<Instant> fetchUpdatedAt(URI uri) {
        if (!GITHUB_HOST.equalsIgnoreCase(uri.getHost())) {
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
        } catch (HttpClientErrorException exception) {
            log.atWarn()
                    .addKeyValue("uri", uri)
                    .addKeyValue("status", exception.getStatusCode().value())
                    .setCause(exception)
                    .log("github_fetch_failed");
            return Optional.empty();
        } catch (RuntimeException exception) {
            log.atWarn().addKeyValue("uri", uri).setCause(exception).log("github_fetch_failed");
            return Optional.empty();
        }
    }

    private record RepoResponse(
            @com.fasterxml.jackson.annotation.JsonProperty("updated_at")
            Instant updatedAt) {}
}
