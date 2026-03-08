package backend.academy.linktracker.bot;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import backend.academy.linktracker.bot.model.ApiErrorResponse;
import backend.academy.linktracker.bot.model.LinkUpdateRequest;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.RestClient;
import org.wiremock.spring.EnableWireMock;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@EnableWireMock
class UpdatesControllerTest {

    @LocalServerPort
    int port;

    @Autowired
    RestClient.Builder restClientBuilder;

    @Test
    void validRequestReturnsOk() {
        RestClient client =
                restClientBuilder.baseUrl("http://localhost:" + port).build();

        ResponseEntity<Void> response = client.post()
                .uri("/updates")
                .contentType(MediaType.APPLICATION_JSON)
                .body(new LinkUpdateRequest(1L, "https://github.com/user/repo", "changed", List.of(1L)))
                .retrieve()
                .toBodilessEntity();

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void invalidRequestReturnsBadRequestWithContractErrorBody() {
        RestClient client =
                restClientBuilder.baseUrl("http://localhost:" + port).build();

        ResponseEntity<ApiErrorResponse> response = client.post()
                .uri("/updates")
                .contentType(MediaType.APPLICATION_JSON)
                .body("{}")
                .exchange((request, httpResponse) -> {
                    request.getHeaders();
                    return new ResponseEntity<>(
                            httpResponse.bodyTo(ApiErrorResponse.class),
                            httpResponse.getHeaders(),
                            HttpStatus.valueOf(httpResponse.getStatusCode().value()));
                });

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("400", response.getBody().code());
        assertFalse(response.getBody().stacktrace().isEmpty());
    }
}
