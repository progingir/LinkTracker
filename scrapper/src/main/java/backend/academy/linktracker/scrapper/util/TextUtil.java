package backend.academy.linktracker.scrapper.util;

public final class TextUtil {

    private TextUtil() {}

    public static String truncate(String text, int maxLength) {
        if (text == null || text.isBlank()) {
            return "Нет описания";
        }
        if (text.length() <= maxLength) {
            return text;
        }
        return text.substring(0, maxLength).trim() + "...";
    }

    public static String escapeMarkdown(String text) {
        if (text == null) return "";
        return text.replace("_", "\\_")
                .replace("*", "\\*")
                .replace("[", "\\[")
                .replace("`", "\\`")
                .replace("#", "\\#");
    }
}
