package backend.academy.linktracker.bot.client;

import backend.academy.linktracker.bot.model.AddLinkRequest;
import backend.academy.linktracker.bot.model.LinkResponse;
import backend.academy.linktracker.bot.model.ListLinksResponse;
import backend.academy.linktracker.bot.model.RemoveLinkRequest;
import backend.academy.linktracker.bot.properties.ScrapperProperties;
import java.net.URI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class ScrapperClientImpl implements ScrapperClient {

    private static final Logger log = LoggerFactory.getLogger(ScrapperClientImpl.class);
    private static final String TG_CHAT_ID_HEADER = "Tg-Chat-Id";

    private final RestClient restClient;

    public ScrapperClientImpl(RestClient.Builder restClientBuilder, ScrapperProperties properties) {
        this.restClient = restClientBuilder.baseUrl(properties.getBaseUrl()).build();
    }

    @Override
    public void registerChat(long chatId) {
        try {
            restClient.post().uri("/tg-chat/{id}", chatId).retrieve().toBodilessEntity();
        } catch (RuntimeException exception) {
            log.atWarn().addKeyValue("chatId", chatId).setCause(exception).log("scrapper_register_chat_failed");
            throw exception;
        }
    }

    @Override
    public LinkResponse addLink(long chatId, AddLinkRequest request) {
        RestClient.RequestBodySpec addRequest = requestWithChatHeader(HttpMethod.POST, "/links", chatId);
        addRequest.contentType(MediaType.APPLICATION_JSON);
        addRequest.body(request);
        return requireBody(addRequest.retrieve().body(LinkResponse.class), "addLink");
    }

    @Override
    public LinkResponse removeLink(long chatId, URI link) {
        RestClient.RequestBodySpec removeRequest = requestWithChatHeader(HttpMethod.DELETE, "/links", chatId);
        removeRequest.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        removeRequest.body(new RemoveLinkRequest(link));
        return requireBody(removeRequest.retrieve().body(LinkResponse.class), "removeLink");
    }

    @Override
    public ListLinksResponse getLinks(long chatId) {
        RestClient.RequestHeadersSpec<?> listRequest = requestHeadersWithChatHeader(HttpMethod.GET, "/links", chatId);
        return requireBody(listRequest.retrieve().body(ListLinksResponse.class), "getLinks");
    }

    private RestClient.RequestBodySpec requestWithChatHeader(HttpMethod method, String uri, long chatId) {
        RestClient.RequestBodySpec request = restClient.method(method).uri(uri);
        request.header(TG_CHAT_ID_HEADER, String.valueOf(chatId));
        return request;
    }

    private RestClient.RequestHeadersSpec<?> requestHeadersWithChatHeader(HttpMethod method, String uri, long chatId) {
        RestClient.RequestHeadersSpec<?> request = restClient.method(method).uri(uri);
        request.header(TG_CHAT_ID_HEADER, String.valueOf(chatId));
        return request;
    }

    private <T> T requireBody(T body, String operation) {
        if (body == null) {
            throw new IllegalStateException("Empty response body from scrapper for operation: " + operation);
        }
        return body;
    }
}
