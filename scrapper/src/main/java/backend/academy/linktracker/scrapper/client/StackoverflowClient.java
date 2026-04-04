package backend.academy.linktracker.scrapper.client;

import backend.academy.linktracker.scrapper.properties.StackoverflowProperties;
import java.net.URI;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

@Component
public class StackoverflowClient implements LinkSourceClient {

    private static final Logger log = LoggerFactory.getLogger(StackoverflowClient.class);
    private static final String STACKOVERFLOW_HOST = "stackoverflow.com";
    private static final String STACKEXCHANGE_API_BASE_URL = "https://api.stackexchange.com/2.3";

    private final RestClient restClient;
    private final StackoverflowProperties properties;

    public StackoverflowClient(RestClient.Builder builder, StackoverflowProperties properties) {
        this.restClient = builder.baseUrl(STACKEXCHANGE_API_BASE_URL).build();
        this.properties = properties;
    }

    @Override
    public Optional<Instant> fetchUpdatedAt(URI uri) {
        if (!STACKOVERFLOW_HOST.equalsIgnoreCase(uri.getHost())) {
            return Optional.empty();
        }

        String[] parts = uri.getPath().split("/");
        if (parts.length < 3 || !"questions".equals(parts[1])) {
            return Optional.empty();
        }

        try {
            RestClient.RequestHeadersSpec<?> request = restClient.get().uri(uriBuilder -> uriBuilder
                    .path("/questions/{id}")
                    .queryParam("site", "stackoverflow")
                    .queryParam("key", properties.getKey())
                    .build(parts[2]));
            if (properties.getAccessToken() != null
                    && !properties.getAccessToken().isBlank()) {
                request.header(HttpHeaders.AUTHORIZATION, "Bearer " + properties.getAccessToken());
            }
            QuestionsResponse response = request.retrieve().body(QuestionsResponse.class);
            if (response == null || response.items() == null || response.items().isEmpty()) {
                return Optional.empty();
            }
            return Optional.ofNullable(response.items().getFirst().lastActivityDate())
                    .map(Instant::ofEpochSecond);
        } catch (HttpClientErrorException exception) {
            log.atWarn()
                    .addKeyValue("uri", uri)
                    .addKeyValue("status", exception.getStatusCode().value())
                    .setCause(exception)
                    .log("stackoverflow_fetch_failed");
            return Optional.empty();
        } catch (RuntimeException exception) {
            log.atWarn().addKeyValue("uri", uri).setCause(exception).log("stackoverflow_fetch_failed");
            return Optional.empty();
        }
    }

    private record QuestionsResponse(List<QuestionResponse> items) {}

    private record QuestionResponse(
            @com.fasterxml.jackson.annotation.JsonProperty("last_activity_date")
            Long lastActivityDate) {}
}
