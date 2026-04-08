package backend.academy.linktracker.scrapper.client;

import backend.academy.linktracker.scrapper.properties.StackoverflowProperties;
import java.net.URI;
import java.time.Instant;
import java.util.Comparator;
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
    public Optional<LinkSourceUpdate> fetchUpdate(URI uri) {
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
            QuestionsResponse questionResponse = request.retrieve().body(QuestionsResponse.class);
            if (questionResponse == null
                    || questionResponse.items() == null
                    || questionResponse.items().isEmpty()) {
                return Optional.empty();
            }
            QuestionResponse question = questionResponse.items().getFirst();

            AnswersResponse answersResponse = restClient
                    .get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/questions/{id}/answers")
                            .queryParam("site", "stackoverflow")
                            .queryParam("sort", "creation")
                            .queryParam("order", "desc")
                            .queryParam("pagesize", 20)
                            .queryParam("filter", "withbody")
                            .queryParam("key", properties.getKey())
                            .build(parts[2]))
                    .retrieve()
                    .body(AnswersResponse.class);
            if (answersResponse == null || answersResponse.items() == null) {
                return Optional.empty();
            }
            return answersResponse.items().stream()
                    .filter(answer -> answer != null && answer.creationDate() != null)
                    .max(Comparator.comparing(AnswerResponse::creationDate))
                    .map(answer -> new LinkSourceUpdate(
                            Instant.ofEpochSecond(answer.creationDate()), formatDescription(question, answer)));
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
            String title,
            @com.fasterxml.jackson.annotation.JsonProperty("last_activity_date")
            Long lastActivityDate) {}

    private record AnswersResponse(List<AnswerResponse> items) {}

    private record AnswerResponse(
            @com.fasterxml.jackson.annotation.JsonProperty("creation_date")
            Long creationDate,
            @com.fasterxml.jackson.annotation.JsonProperty("body_markdown")
            String bodyMarkdown,
            OwnerResponse owner) {}

    private record OwnerResponse(
            @com.fasterxml.jackson.annotation.JsonProperty("display_name")
            String displayName) {}

    private String formatDescription(QuestionResponse question, AnswerResponse answer) {
        String title = question.title() == null ? "(без темы)" : question.title();
        String author =
                answer.owner() == null || answer.owner().displayName() == null ? "unknown" : answer.owner().displayName();
        String createdAt = answer.creationDate() == null
                ? "unknown-time"
                : Instant.ofEpochSecond(answer.creationDate()).toString();
        String preview = sanitizePreview(answer.bodyMarkdown(), 200);
        return "Ответ на вопрос: %s%nАвтор: %s%nСоздано: %s%nПревью: %s".formatted(title, author, createdAt, preview);
    }

    private String sanitizePreview(String source, int limit) {
        if (source == null || source.isBlank()) {
            return "(пусто)";
        }
        String normalized = source.replaceAll("\\s+", " ").trim();
        return normalized.length() <= limit ? normalized : normalized.substring(0, limit);
    }
}
