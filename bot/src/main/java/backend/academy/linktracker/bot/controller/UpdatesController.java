package backend.academy.linktracker.bot.controller;

import backend.academy.linktracker.bot.model.LinkUpdateRequest;
import backend.academy.linktracker.bot.service.UpdateService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/updates")
@RequiredArgsConstructor
public class UpdatesController {

    private final UpdateService updateService;

    @PostMapping
    public ResponseEntity<Void> postUpdate(@Valid @RequestBody LinkUpdateRequest request) {
        updateService.handleLinkUpdate(request);
        return ResponseEntity.ok().build();
    }
}
