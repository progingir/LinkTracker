package backend.academy.linktracker.scrapper.service;

import java.net.URI;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.stereotype.Service;

@Service
public class LinkParser {
    private static final Pattern GITHUB_PATTERN = Pattern.compile("github\\.com/([^/]+)/([^/]+)");

    private static final Pattern SO_PATTERN = Pattern.compile("stackoverflow\\.com/questions/([0-9]+)");

    public record GithubInfo(String owner, String repo) {}

    public GithubInfo parseGithub(URI uri) {
        Matcher matcher = GITHUB_PATTERN.matcher(uri.toString());
        return matcher.find() ? new GithubInfo(matcher.group(1), matcher.group(2)) : null;
    }

    public Long parseStackOverflow(URI uri) {
        Matcher matcher = SO_PATTERN.matcher(uri.toString());
        return matcher.find() ? Long.parseLong(matcher.group(1)) : null;
    }
}
