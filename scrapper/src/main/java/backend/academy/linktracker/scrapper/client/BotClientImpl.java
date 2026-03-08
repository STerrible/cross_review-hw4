package backend.academy.linktracker.scrapper.client;

import backend.academy.linktracker.scrapper.model.LinkUpdateRequest;
import backend.academy.linktracker.scrapper.properties.BotClientProperties;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class BotClientImpl implements BotClient {

    private final RestClient restClient;

    public BotClientImpl(RestClient.Builder builder, BotClientProperties properties) {
        this.restClient = builder.baseUrl(properties.getBaseUrl()).build();
    }

    @Override
    public void sendUpdate(LinkUpdateRequest update) {
        restClient
                .post()
                .uri("/updates")
                .contentType(MediaType.APPLICATION_JSON)
                .body(update)
                .retrieve()
                .toBodilessEntity();
    }
}
