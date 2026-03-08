package backend.academy.linktracker.bot.service.state;

import java.net.URI;

public record ChatState(TrackState state, URI pendingLink) {

    public static ChatState idle() {
        return new ChatState(TrackState.IDLE, null);
    }
}
