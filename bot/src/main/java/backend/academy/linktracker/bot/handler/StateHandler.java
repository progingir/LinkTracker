package backend.academy.linktracker.bot.handler;

import backend.academy.linktracker.bot.repository.StateRepository;
import backend.academy.linktracker.bot.state.UserState;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;

public interface StateHandler {
    UserState getHandledState();

    SendMessage handle(Update update, StateRepository.UserContext context);
}
