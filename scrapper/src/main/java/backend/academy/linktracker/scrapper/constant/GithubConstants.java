package backend.academy.linktracker.scrapper.constant;

import java.util.regex.Pattern;
import lombok.experimental.UtilityClass;

@UtilityClass
public class GithubConstants {
    public static final Pattern GITHUB_PATTERN = Pattern.compile("github\\.com/([^/]+)/([^/]+)");

    public static final String NEW_ISSUE_TITLE = "Новый Issue";
    public static final String NEW_PR_TITLE = "Новый Pull Request";
}
