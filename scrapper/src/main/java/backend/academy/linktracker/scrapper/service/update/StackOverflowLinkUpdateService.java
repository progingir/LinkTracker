package backend.academy.linktracker.scrapper.service.update;

import backend.academy.linktracker.scrapper.client.StackOverflowClient;
import backend.academy.linktracker.scrapper.constant.StackOverflowConstants;
import backend.academy.linktracker.scrapper.dto.StackOverflowAnswerResponse;
import backend.academy.linktracker.scrapper.dto.StackOverflowCommentResponse;
import backend.academy.linktracker.scrapper.dto.StackOverflowResponse;
import backend.academy.linktracker.scrapper.dto.UpdateResult;
import backend.academy.linktracker.scrapper.service.parser.LinkParser;
import backend.academy.linktracker.scrapper.util.TextUtil;
import java.net.URI;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class StackOverflowLinkUpdateService implements LinkUpdateService {

    private final StackOverflowClient client;
    private final LinkParser parser;

    @Override
    public Optional<List<UpdateResult>> fetchUpdates(URI url, OffsetDateTime lastKnownUpdate) {
        Long questionId = parser.parseStackOverflow(url);
        if (questionId == null) {
            return Optional.empty();
        }

        List<UpdateResult> results = new ArrayList<>();

        var answerResponse = client.fetchAnswers(questionId, lastKnownUpdate);
        var commentResponse = client.fetchComments(questionId, lastKnownUpdate);

        if (isResponseEmpty(answerResponse) && isResponseEmpty(commentResponse)) {
            return Optional.of(results);
        }

        String questionTitle = client.fetchQuestion(questionId)
                .map(StackOverflowResponse.Item::title)
                .orElse(StackOverflowConstants.UNKNOWN_QUESTION_TITLE);

        processAnswers(results, answerResponse, questionTitle, lastKnownUpdate);
        processComments(results, commentResponse, questionTitle, lastKnownUpdate);

        return Optional.of(results);
    }

    private boolean isResponseEmpty(Object response) {
        if (response instanceof StackOverflowAnswerResponse r) {
            return r.items() == null || r.items().isEmpty();
        }
        if (response instanceof StackOverflowCommentResponse r) {
            return r.items() == null || r.items().isEmpty();
        }
        return true;
    }

    private void processAnswers(
            List<UpdateResult> results,
            StackOverflowAnswerResponse response,
            String questionTitle,
            OffsetDateTime lastKnownUpdate) {
        if (response == null || response.items() == null) {
            return;
        }

        for (var answer : response.items()) {
            if (lastKnownUpdate == null || answer.creationDate().isAfter(lastKnownUpdate)) {
                results.add(new UpdateResult(
                        answer.creationDate(),
                        formatDescription(
                                questionTitle,
                                StackOverflowConstants.ANSWER_TYPE,
                                answer.owner().displayName(),
                                answer.bodyMarkdown(),
                                answer.creationDate())));
            }
        }
    }

    private void processComments(
            List<UpdateResult> results,
            StackOverflowCommentResponse response,
            String questionTitle,
            OffsetDateTime lastKnownUpdate) {
        if (response == null || response.items() == null) {
            return;
        }

        for (var comment : response.items()) {
            if (lastKnownUpdate == null || comment.creationDate().isAfter(lastKnownUpdate)) {
                results.add(new UpdateResult(
                        comment.creationDate(),
                        formatDescription(
                                questionTitle,
                                StackOverflowConstants.COMMENT_TYPE,
                                comment.owner().displayName(),
                                comment.body(),
                                comment.creationDate())));
            }
        }
    }

    private String formatDescription(String title, String type, String author, String body, OffsetDateTime time) {
        String safeTitle = TextUtil.escapeMarkdown(title);
        String safeAuthor = TextUtil.escapeMarkdown(author);
        String safePreview = TextUtil.escapeMarkdown(TextUtil.truncate(body == null ? "" : body, 200));

        return String.format(
                StackOverflowConstants.DESCRIPTION_TEMPLATE, safeTitle, type, safeAuthor, time, safePreview);
    }
}
