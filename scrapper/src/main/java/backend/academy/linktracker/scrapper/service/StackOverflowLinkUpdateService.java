package backend.academy.linktracker.scrapper.service;

import backend.academy.linktracker.scrapper.client.StackOverflowClient;
import backend.academy.linktracker.scrapper.dto.StackOverflowAnswerResponse;
import backend.academy.linktracker.scrapper.dto.StackOverflowCommentResponse;
import backend.academy.linktracker.scrapper.dto.StackOverflowResponse;
import backend.academy.linktracker.scrapper.dto.UpdateResult;
import backend.academy.linktracker.scrapper.util.TextUtil;
import java.net.URI;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class StackOverflowLinkUpdateService implements LinkUpdateService {
    private final StackOverflowClient client;
    private final LinkParser parser;

    @Override
    public boolean supports(URI url) {
        return parser.parseStackOverflow(url) != null;
    }

    @Override
    public List<UpdateResult> fetchUpdates(URI url, OffsetDateTime lastKnownUpdate) {
        Long questionId = parser.parseStackOverflow(url);
        List<UpdateResult> results = new ArrayList<>();

        List<StackOverflowAnswerResponse.Answer> newAnswers = new ArrayList<>();
        StackOverflowAnswerResponse answerResponse = client.fetchAnswers(questionId, lastKnownUpdate);
        if (answerResponse != null && answerResponse.items() != null) {
            for (var answer : answerResponse.items()) {
                if (lastKnownUpdate == null || answer.creationDate().isAfter(lastKnownUpdate)) {
                    newAnswers.add(answer);
                }
            }
        }

        List<StackOverflowCommentResponse.Comment> newComments = new ArrayList<>();
        StackOverflowCommentResponse commentResponse = client.fetchComments(questionId, lastKnownUpdate);
        if (commentResponse != null && commentResponse.items() != null) {
            for (var comment : commentResponse.items()) {
                if (lastKnownUpdate == null || comment.creationDate().isAfter(lastKnownUpdate)) {
                    newComments.add(comment);
                }
            }
        }

        if (!newAnswers.isEmpty() || !newComments.isEmpty()) {
            String questionTitle = client.fetchQuestion(questionId)
                    .map(StackOverflowResponse.Item::title)
                    .orElse("Неизвестная тема");

            for (var answer : newAnswers) {
                results.add(new UpdateResult(
                        answer.creationDate(),
                        formatDescription(
                                questionTitle,
                                "Новый ответ",
                                answer.owner().displayName(),
                                answer.body(),
                                answer.creationDate())));
            }

            for (var comment : newComments) {
                results.add(new UpdateResult(
                        comment.creationDate(),
                        formatDescription(
                                questionTitle,
                                "Новый комментарий",
                                comment.owner().displayName(),
                                comment.body(),
                                comment.creationDate())));
            }
        }

        return results;
    }

    private String formatDescription(String title, String type, String author, String body, OffsetDateTime time) {
        String safeTitle = TextUtil.escapeMarkdown(title);
        String safeAuthor = TextUtil.escapeMarkdown(author);
        String safePreview = TextUtil.escapeMarkdown(TextUtil.truncate(body, 200));

        return String.format(
                "🔔 **Обновление на StackOverflow!**\n" + "📌 **Тема:** %s\n"
                        + "Тип: %s\n"
                        + "👤 Автор: %s\n"
                        + "⏱ Время: %s\n\n"
                        + "📄 Превью:\n%s",
                safeTitle, type, safeAuthor, time, safePreview);
    }
}
