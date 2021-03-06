package io.elves.http.server.handler;

import com.google.common.base.Strings;
import io.elves.common.exception.CommandException;
import io.elves.common.util.IpUtil;
import io.elves.core.CommandHandler;
import io.elves.core.coder.Coder;
import io.elves.core.coder.CoderContainer;
import io.elves.core.command.CommandHandlerApplicationContext;
import io.elves.core.context.RequestContext;
import io.elves.core.context.RequestContextInner;
import io.elves.core.context.ResponseContext;
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

/**
 * @author lee
 * @deprecated {@link io.elves.http.server.handler.DispatchV2CommandHandler}
 */
@Slf4j
@Deprecated
public class DispatchV1CommandHandler {
    public static final DispatchV1CommandHandler INSTANCE = new DispatchV1CommandHandler();

    private DispatchV1CommandHandler() {

    }

    public ResponseContext dispatch(final FullHttpRequest request) {
        setRequestId(request);
        RequestContextInner requestContextInner = RequestContextBuild.parseRequest(request);

        if (StringUtils.isBlank(requestContextInner.getCommandName())) {
            throw new CommandException(BAD_REQUEST, "Invalid command name.");
        }

        CommandHandler<?> commandHandler = CommandHandlerApplicationContext
                .getInstance()
                .getHandle(requestContextInner.getCommandName());
        if (commandHandler == null) {
            throw new CommandException(NOT_FOUND, String.format("not found -> \"%s\""
                    , requestContextInner.getCommandName()));
        }

        try {
            return innerHandler(requestContextInner, commandHandler);
        } finally {
            commandHandler.destroy();
            requestContextInner.clean();
        }
    }


    private void setRequestId(FullHttpRequest httpRequest) {
        MDC.put(REQUEST_ID_KEY, Strings.isNullOrEmpty(httpRequest.headers().get(REQUEST_ID_KEY))
                ? UUID.randomUUID().toString().replaceAll("-", "").toLowerCase()
                : httpRequest.headers().get(REQUEST_ID_KEY));
        MDC.put(SERVER_IP_KEY, IpUtil.getLocalIp());
    }

    private Coder lookupEncoder(Class<?> dataClass, String requestContextType) {
        if (dataClass == null) {
            throw new IllegalArgumentException("Bad class metadata");
        }

        Coder encoder = CoderContainer.getCoder(requestContextType);
        if (encoder == null) {
            log.error("command handle encoder {} not support", requestContextType);
            throw new CommandException(NOT_IMPLEMENTED, String.format("server encoder not implement -> %s"
                    , requestContextType));
        }

        if (encoder.canEncode(dataClass)) {
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

    private ResponseContext innerHandler(RequestContextInner context
            , CommandHandler<?> commandHandler) {

        long start = System.currentTimeMillis();

        CommandResponse<?> resp = commandHandler.handle(new RequestContext(context.getBodyByte()
                , context.getHeaders()
                , context.getUrl()
                , context.getParameters()
                , context.getDecoder()));

        log.debug("command {} - cost {} ms"
                , context.getCommandName()
                , System.currentTimeMillis() - start);
        byte[] bytes = encodeResponseBody(resp
                , CommandHandlerApplicationContext.getInstance().getCommandContext(context.getCommandName()).getRespEncoderType());

        return new ResponseContext(bytes
                , new DefaultHttpHeaders().add("Content-Type", String.format("%s; charset=UTF-8"
                , context.getResponseContentType()))
                , resp.getStatus());
    }
}
