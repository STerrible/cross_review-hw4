package backend.academy.linktracker.bot.client;

import backend.academy.linktracker.bot.model.AddLinkRequest;
import backend.academy.linktracker.bot.model.LinkResponse;
import backend.academy.linktracker.bot.model.ListLinksResponse;
import backend.academy.linktracker.bot.model.RemoveLinkRequest;
import backend.academy.linktracker.bot.properties.ScrapperProperties;
import java.net.URI;
import java.util.List;
import java.util.Objects;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

@Component
public class ScrapperClientImpl implements ScrapperClient {

    private final RestClient restClient;

    public ScrapperClientImpl(RestClient.Builder restClientBuilder, ScrapperProperties properties) {
        this.restClient = restClientBuilder.baseUrl(properties.getBaseUrl()).build();
    }

    @Override
    public void registerChat(long chatId) {
        restClient.post().uri("/tg-chat/{id}", chatId).retrieve().toBodilessEntity();
    }

    @Override
    public LinkResponse addLink(long chatId, AddLinkRequest request) {
        RestClient.RequestBodySpec addRequest = restClient.post().uri("/links");
        addRequest.header("Tg-Chat-Id", String.valueOf(chatId));
        addRequest.contentType(MediaType.APPLICATION_JSON);
        addRequest.body(request);
        return Objects.requireNonNull(addRequest.retrieve().body(LinkResponse.class));
    }

    @Override
    public LinkResponse removeLink(long chatId, URI link) {
        RestClient.RequestBodySpec removeRequest =
                restClient.method(HttpMethod.DELETE).uri("/links");
        removeRequest.header("Tg-Chat-Id", String.valueOf(chatId));
        removeRequest.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        removeRequest.body(new RemoveLinkRequest(link));
        return Objects.requireNonNull(removeRequest.retrieve().body(LinkResponse.class));
    }

    @Override
    public ListLinksResponse getLinks(long chatId) {
        try {
            RestClient.RequestHeadersSpec<?> listRequest = restClient.get().uri("/links");
            listRequest.header("Tg-Chat-Id", String.valueOf(chatId));
            return Objects.requireNonNullElse(
                    listRequest.retrieve().body(ListLinksResponse.class), new ListLinksResponse(List.of(), 0));
        } catch (HttpClientErrorException.NotFound exception) {
            return new ListLinksResponse(List.of(), 0);
        }
    }
}
