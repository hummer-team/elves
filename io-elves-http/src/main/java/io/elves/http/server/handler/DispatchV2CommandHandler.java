package io.elves.http.server.handler;

import com.google.common.base.Strings;
import io.elves.common.exception.CommandException;
import io.elves.common.util.IpUtil;
import io.elves.core.coder.Coder;
import io.elves.core.coder.CoderContainer;
import io.elves.core.command.CommandConext;
import io.elves.core.command.CommandHandlerContainer;
import io.elves.core.context.RequestContext;
import io.elves.core.context.ResponseContext;
import io.elves.core.response.CommandResponse;
import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.MDC;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Parameter;
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
 */
@Slf4j
public class DispatchV2CommandHandler {
    public static final DispatchV2CommandHandler INSTANCE = new DispatchV2CommandHandler();

    private DispatchV2CommandHandler() {

    }

    public ResponseContext handler(final FullHttpRequest request) {
        setRequestId(request);
        RequestContext requestContext = BuildRequestContext.parseRequest(request);

        if (StringUtils.isBlank(requestContext.getCommandName())) {
            throw new CommandException(BAD_REQUEST, "Invalid command name.");
        }

        CommandConext commandConext = CommandHandlerContainer
                .getInstance()
                .getCommandContext(requestContext.getCommandName());
        if (commandConext == null) {
            throw new CommandException(NOT_FOUND, String.format("not found -> \"%s\""
                    , requestContext.getCommandName()));
        }

        try {
            CommandResponse<?> resp = invoke(commandConext);

            byte[] bytes = encodeResponseBody(resp, commandConext.getRespEncoderType());

            return new ResponseContext(bytes
                    , new DefaultHttpHeaders().add("Content-Type", String.format("%s; charset=UTF-8"
                    , requestContext.getResponseContentType()))
                    , resp.getStatus());

        } catch (Throwable e) {
            //todo handler exception
            log.error("execute {} failed", commandConext.getName(), e);
            throw new RuntimeException(e);
        } finally {
            requestContext.clean();
        }
    }

    private CommandResponse<?> invoke(CommandConext commandConext)
            throws IllegalAccessException, InvocationTargetException {
        Parameter[] parameters = commandConext.getMethod().getParameters();
        Object obj = null;
        if (parameters == null) {
            obj = commandConext.getMethod().invoke(commandConext.getTargetCommandObject());
        } else {
            obj = commandConext.getMethod().invoke(commandConext.getTargetCommandObject());
        }
        return obj instanceof CommandResponse
                ? (CommandResponse) obj
                : CommandResponse.ok(obj, HttpResponseStatus.OK);
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
}
