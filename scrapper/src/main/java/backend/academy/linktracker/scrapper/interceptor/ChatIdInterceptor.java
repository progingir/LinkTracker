package backend.academy.linktracker.scrapper.interceptor;

import io.grpc.*;
import org.springframework.stereotype.Component;

@Component
public class ChatIdInterceptor implements ServerInterceptor {

    public static final Metadata.Key<String> TG_CHAT_ID_KEY =
            Metadata.Key.of("tg-chat-id", Metadata.ASCII_STRING_MARSHALLER);

    public static final Context.Key<Long> CHAT_ID_CTX = Context.key("chatId");

    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(
            ServerCall<ReqT, RespT> call, Metadata headers, ServerCallHandler<ReqT, RespT> next) {

        String chatIdStr = headers.get(TG_CHAT_ID_KEY);
        if (chatIdStr != null) {
            Long chatId = Long.valueOf(chatIdStr);
            return Contexts.interceptCall(Context.current().withValue(CHAT_ID_CTX, chatId), call, headers, next);
        }

        return next.startCall(call, headers);
    }
}
