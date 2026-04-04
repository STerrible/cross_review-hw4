package backend.academy.linktracker.scrapper.controller;

import backend.academy.linktracker.scrapper.model.AddLinkRequest;
import backend.academy.linktracker.scrapper.model.LinkResponse;
import backend.academy.linktracker.scrapper.model.ListLinksResponse;
import backend.academy.linktracker.scrapper.model.RemoveLinkRequest;
import backend.academy.linktracker.scrapper.service.ScrapperService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/links")
@RequiredArgsConstructor
public class LinksController {

    private final ScrapperService scrapperService;

    @GetMapping
    public ResponseEntity<ListLinksResponse> list(@RequestHeader(ApiHeaders.TG_CHAT_ID) long chatId) {
        return ResponseEntity.ok(scrapperService.listLinks(chatId));
    }

    @PostMapping
    public ResponseEntity<LinkResponse> add(
            @RequestHeader(ApiHeaders.TG_CHAT_ID) long chatId, @Valid @RequestBody AddLinkRequest request) {
        return ResponseEntity.ok(scrapperService.addLink(chatId, request));
    }

    @DeleteMapping
    public ResponseEntity<LinkResponse> remove(
            @RequestHeader(ApiHeaders.TG_CHAT_ID) long chatId, @Valid @RequestBody RemoveLinkRequest request) {
        return ResponseEntity.ok(scrapperService.removeLink(chatId, request.link()));
    }
}
