package backend.academy.linktracker.scrapper.constant;

import lombok.experimental.UtilityClass;

@UtilityClass
public class NotificationConstants {
    public static final String UPDATE_SEPARATOR = "\n\n---\n\n";

    public static final String ERROR_REPORT_HEADER = "Отчет о стабильных сбоях\n\n";
    public static final String ERROR_REPORT_BODY =
            "Ресурсы недоступны длительное время (рекомендуем проверить их или отписаться):\n";

    public static final String LIST_ITEM_PREFIX = "• ";
}
