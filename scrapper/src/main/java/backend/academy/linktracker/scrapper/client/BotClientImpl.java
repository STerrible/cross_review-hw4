package backend.academy.linktracker.scrapper.client;

import backend.academy.linktracker.scrapper.model.LinkUpdateRequest;
import backend.academy.linktracker.scrapper.properties.BotClientProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Component
public class BotClientImpl implements BotClient {

    private static final Logger log = LoggerFactory.getLogger(BotClientImpl.class);
    private static final int CONNECT_TIMEOUT_MILLIS = 3_000;
    private static final int READ_TIMEOUT_MILLIS = 10_000;

    private final RestClient restClient;

    public BotClientImpl(RestClient.Builder builder, BotClientProperties properties) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(CONNECT_TIMEOUT_MILLIS);
        requestFactory.setReadTimeout(READ_TIMEOUT_MILLIS);
        this.restClient = builder.baseUrl(properties.getBaseUrl()).requestFactory(requestFactory).build();
    }

    @Override
    public void sendUpdate(LinkUpdateRequest update) {
        try {
            restClient
                .post()
                .uri("/updates")
                .contentType(MediaType.APPLICATION_JSON)
                .body(update)
                .retrieve()
                .toBodilessEntity();
        } catch (RuntimeException exception) {
            log.atWarn()
                .addKeyValue("linkId", update.id())
                .addKeyValue("chatsCount", update.tgChatIds() == null ? 0 : update.tgChatIds().size())
                .setCause(exception)
                .log("bot_update_send_failed");
        }
    }
}
