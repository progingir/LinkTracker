package backend.academy.linktracker.scrapper.exception;

import java.net.URI;

public class LinkAlreadyTrackedException extends RuntimeException {
    public LinkAlreadyTrackedException(URI url) {
        super("Ссылка " + url.toString() + " уже отслеживается в этом чате");
    }
}
