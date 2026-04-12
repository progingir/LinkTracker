package backend.academy.linktracker.scrapper.service;

import backend.academy.linktracker.scrapper.dto.LinkUpdate;

public interface UpdateSender {
    void sendUpdate(LinkUpdate update);
}
