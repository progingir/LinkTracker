package backend.academy.linktracker.scrapper.service.parser;

import backend.academy.linktracker.scrapper.constant.GithubConstants;
import backend.academy.linktracker.scrapper.constant.StackOverflowConstants;
import java.net.URI;
import java.util.regex.Matcher;
import org.springframework.stereotype.Service;

@Service
public class LinkParser {

    public record GithubInfo(String owner, String repo) {}

    public GithubInfo parseGithub(URI uri) {
        Matcher matcher = GithubConstants.GITHUB_PATTERN.matcher(uri.toString());
        if (matcher.find()) {
            return new GithubInfo(matcher.group(1), matcher.group(2));
        }
        return null;
    }

    public Long parseStackOverflow(URI uri) {
        Matcher matcher = StackOverflowConstants.SO_PATTERN.matcher(uri.toString());
        if (matcher.find()) {
            return Long.parseLong(matcher.group(1));
        }
        return null;
    }
}
