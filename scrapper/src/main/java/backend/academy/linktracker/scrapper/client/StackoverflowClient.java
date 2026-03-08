package backend.academy.linktracker.scrapper.client;

import backend.academy.linktracker.scrapper.properties.StackoverflowProperties;
import java.net.URI;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class StackoverflowClient implements LinkSourceClient {

    private final RestClient restClient;
    private final StackoverflowProperties properties;

    public StackoverflowClient(RestClient.Builder builder, StackoverflowProperties properties) {
        this.restClient = builder.baseUrl("https://api.stackexchange.com/2.3").build();
        this.properties = properties;
    }

    @Override
    public Optional<Instant> fetchUpdatedAt(URI uri) {
        if (!"stackoverflow.com".equalsIgnoreCase(uri.getHost())) {
            return Optional.empty();
        }

        String[] parts = uri.getPath().split("/");
        if (parts.length < 3 || !"questions".equals(parts[1])) {
            return Optional.empty();
        }

        try {
            QuestionsResponse response = restClient
                    .get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/questions/{id}")
                            .queryParam("site", "stackoverflow")
                            .queryParam("key", properties.getKey())
                            .queryParam("access_token", properties.getAccessToken())
                            .build(parts[2]))
                    .retrieve()
                    .body(QuestionsResponse.class);
            if (response == null || response.items() == null || response.items().isEmpty()) {
                return Optional.empty();
            }
            return Optional.ofNullable(response.items().getFirst().lastActivityDate())
                    .map(Instant::ofEpochSecond);
        } catch (RuntimeException exception) {
            return Optional.empty();
        }
    }

    private record QuestionsResponse(List<QuestionResponse> items) {}

    private record QuestionResponse(
            @com.fasterxml.jackson.annotation.JsonProperty("last_activity_date")
            Long lastActivityDate) {}
}
