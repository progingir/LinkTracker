package backend.academy.linktracker.scrapper.constant;

import java.util.regex.Pattern;
import lombok.experimental.UtilityClass;

@UtilityClass
public class StackOverflowConstants {
    public static final Pattern SO_PATTERN = Pattern.compile("stackoverflow\\.com/questions/([0-9]+)");

    public static final String ANSWER_TYPE = "Новый ответ";
    public static final String COMMENT_TYPE = "Новый комментарий";

    public static final String UNKNOWN_QUESTION_TITLE = "Неизвестная тема";

    public static final String DESCRIPTION_TEMPLATE = "🔔 **Обновление на StackOverflow!**%n📌 **Тема:** %s%n"
            + "Тип: %s%n"
            + "👤 Автор: %s%n"
            + "⏱ Время: %s%n%n"
            + "📄 Превью:%n%s";
}
