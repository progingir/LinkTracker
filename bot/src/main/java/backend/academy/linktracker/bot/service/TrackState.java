package backend.academy.linktracker.bot.service;

public enum TrackState implements UserState {
    WAITING_FOR_LINK,
    WAITING_FOR_TAGS,
    WAITING_FOR_FILTERS
}
