package io.elves.http.server.handler;

import com.google.common.base.Strings;
import io.elves.common.exception.CommandException;
import io.elves.common.util.IpUtil;
import io.elves.core.coder.CodecContainer;
import io.elves.core.coder.Coder;
import io.elves.core.command.CommandHandlerContainer;
import io.elves.core.context.RequestContext;
import io.elves.core.context.ResponseConext;
import io.elves.core.handle.CommandHandle;
import io.elves.core.response.CommandResponse;
import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.FullHttpRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.MDC;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

import static io.elves.core.ElvesConstants.REQUEST_ID_KEY;
import static io.elves.core.ElvesConstants.SERVER_IP_KEY;
import static io.netty.handler.codec.http.HttpResponseStatus.BAD_REQUEST;
import static io.netty.handler.codec.http.HttpResponseStatus.INTERNAL_SERVER_ERROR;
import static io.netty.handler.codec.http.HttpResponseStatus.NOT_FOUND;
import static io.netty.handler.codec.http.HttpResponseStatus.NOT_IMPLEMENTED;

@Slf4j
public class DispatchCommandHandler {
    public static final DispatchCommandHandler INSTANCE = new DispatchCommandHandler();

    private DispatchCommandHandler() {

    }

    public ResponseConext handler(final FullHttpRequest request) {
        setRequestId(request);
        RequestContext context = BuildRequestContext.parseRequest(request);

        if (StringUtils.isBlank(context.getCommandName())) {
            throw new CommandException(BAD_REQUEST, "Invalid command name.");
        }

        CommandHandle<?> commandHandler = CommandHandlerContainer.getInstance().getHandle(context.getCommandName());
        if (commandHandler == null) {
            throw new CommandException(NOT_FOUND, String.format("not found -> \"%s\"", context.getCommandName()));
        }

        return new ResponseConext(innerHandler(context, commandHandler)
                , new DefaultHttpHeaders().add("Content-Type", String.format("%s; charset=UTF-8"
                , context.getContentType(true))));
    }


    private void setRequestId(FullHttpRequest httpRequest) {
        MDC.put(REQUEST_ID_KEY, Strings.isNullOrEmpty(httpRequest.headers().get(REQUEST_ID_KEY))
                ? UUID.randomUUID().toString().replaceAll("-", "").toLowerCase()
                : httpRequest.headers().get(REQUEST_ID_KEY));
        MDC.put(SERVER_IP_KEY, IpUtil.getLocalIp());
    }

    private Coder lookupEncoder(Class<?> clazz, String requestContextType) {
        if (clazz == null) {
            throw new IllegalArgumentException("Bad class metadata");
        }

        Coder encoder = CodecContainer.getCoder(requestContextType);
        if (encoder == null) {
            log.error("command handle encoder {} not support", requestContextType);
            throw new CommandException(NOT_IMPLEMENTED, "server encoder not implement");
        }

        if (encoder.canEncode(clazz)) {
            return encoder;
        }

        throw new CommandException(INTERNAL_SERVER_ERROR, "not support coder");
    }

    private byte[] encodeResponseBody(CommandResponse<?> response, String requestContextType) {
        byte[] body = new byte[]{};
        if (response.getCode() == 0) {
            if (response.getData() == null) {
                body = new byte[]{};
            } else {
                Coder encoder = lookupEncoder(response.getData().getClass(), requestContextType);
                body = encoder.encode(response, StandardCharsets.UTF_8);
            }
        } else {
            body = response.getExceptionMessage() != null
                    ? response.getExceptionMessage().getBytes(StandardCharsets.UTF_8)
                    : body;
        }
        return body;
    }

    private byte[] innerHandler(RequestContext context
            , CommandHandle<?> commandHandler) {

        CommandResponse<?> resp = commandHandler.handle(context);
        return encodeResponseBody(resp, context.getContentType(true));
    }
}
