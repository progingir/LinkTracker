package backend.academy.linktracker.scrapper.exception;

import io.grpc.ForwardingServerCallListener;
import io.grpc.Metadata;
import io.grpc.ServerCall;
import io.grpc.ServerCallHandler;
import io.grpc.ServerInterceptor;
import io.grpc.Status;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class GrpcExceptionInterceptor implements ServerInterceptor {

    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(
            ServerCall<ReqT, RespT> call, Metadata headers, ServerCallHandler<ReqT, RespT> next) {

        ServerCall.Listener<ReqT> delegate = next.startCall(call, headers);

        return new ForwardingServerCallListener.SimpleForwardingServerCallListener<ReqT>(delegate) {
            @Override
            public void onHalfClose() {
                try {
                    super.onHalfClose();
                } catch (IllegalArgumentException e) {
                    closeWithStatus(call, Status.INVALID_ARGUMENT, e);
                } catch (ChatNotFoundException | LinkNotFoundException e) {
                    closeWithStatus(call, Status.NOT_FOUND, e);
                } catch (LinkAlreadyTrackedException e) {
                    closeWithStatus(call, Status.ALREADY_EXISTS, e);
                } catch (Exception e) {
                    closeWithStatus(call, Status.INTERNAL, e);
                }
            }
        };
    }

    private void closeWithStatus(ServerCall<?, ?> call, Status status, Exception e) {
        log.atError().setCause(e).log("gRPC Error: {}", status.getCode());
        call.close(status.withDescription(e.getMessage()).withCause(e), new Metadata());
    }
}
