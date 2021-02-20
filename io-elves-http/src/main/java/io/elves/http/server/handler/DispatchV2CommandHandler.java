package io.elves.http.server.handler;

import com.google.common.base.Strings;
import com.google.common.collect.Iterables;
import io.elves.common.exception.CommandException;
import io.elves.common.util.IpUtil;
import io.elves.core.coder.Coder;
import io.elves.core.coder.CoderContainer;
import io.elves.core.command.CommandBody;
import io.elves.core.command.CommandConext;
import io.elves.core.command.CommandHandlerApplicationContext;
import io.elves.core.command.CommandUrlQueryParam;
import io.elves.core.command.GlobalExceptionHandler;
import io.elves.core.context.RequestContext;
import io.elves.core.context.RequestContextInner;
import io.elves.core.context.ResponseContext;
import io.elves.core.response.CommandResponse;
import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.MDC;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Parameter;
import java.nio.charset.StandardCharsets;
import java.util.Map;
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

    public ResponseContext dispatch(final FullHttpRequest request) {
        setRequestId(request);
        RequestContextInner requestContextInner = RequestContextBuild.parseRequest(request);

        if (StringUtils.isBlank(requestContextInner.getCommandName())) {
            throw new CommandException(BAD_REQUEST, "invalid command name.");
        }

        CommandConext commandConext = CommandHandlerApplicationContext.getInstance()
                .getCommandContext(requestContextInner.getCommandName());
        if (commandConext == null) {
            throw new CommandException(NOT_FOUND, String.format("this bad request not found url: \"%s - %s\""
                    , requestContextInner.getUrl(), requestContextInner.getMethod()));
        }

        try {
            CommandResponse<?> resp = invoke(commandConext, requestContextInner);

            byte[] bytes = encodeResponseBody(resp, commandConext.getRespEncoderType());

            return new ResponseContext(bytes
                    , new DefaultHttpHeaders().add("Content-Type", String.format("%s; charset=UTF-8"
                    , requestContextInner.getResponseContentType()))
                    , resp.getStatus());

        } catch (Throwable e) {
            return handlerException(requestContextInner, e);
        } finally {
            requestContextInner.clean();
        }
    }

    private ResponseContext handlerException(RequestContextInner requestContextInner, Throwable e) {
        GlobalExceptionHandler intercept = CommandHandlerApplicationContext.getInstance().getExceptionIntercept();
        if (intercept != null) {
            if (CollectionUtils.isEmpty(intercept.filter())
                    || Iterables.any(intercept.filter()
                    , ex -> ex.getName().equals(
                            e.getCause() != null
                                    ? e.getCause().getClass().getName()
                                    : e.getClass().getName()))) {
                intercept.handler(e.getCause(), new RequestContext(requestContextInner.getBodyByte()
                        , requestContextInner.getHeaders(), requestContextInner.getUrl()));
                return new ResponseContext(new byte[0]
                        , new DefaultHttpHeaders().add("Content-Type", String.format("%s; charset=UTF-8"
                        , requestContextInner.getResponseContentType()))
                        , intercept.code());
            }
        }
        log.error("execute {} failed", requestContextInner.getUrl(), e);
        throw new RuntimeException(e);
    }

    private CommandResponse<?> invoke(CommandConext commandConext, RequestContextInner requestContextInner)
            throws IllegalAccessException, InvocationTargetException {
        Parameter[] parameters = commandConext.getMethod().getParameters();
        Object obj;
        if (parameters == null) {
            obj = commandConext.getMethod().invoke(commandConext.getTargetCommandObject());
        } else {
            Object[] parameterList = parseInvokeParameter(requestContextInner, parameters);
            obj = commandConext.getMethod().invoke(commandConext.getTargetCommandObject(), parameterList);
        }
        return obj instanceof CommandResponse
                ? (CommandResponse) obj
                : CommandResponse.ok(obj, HttpResponseStatus.OK);
    }

    private Object[] parseInvokeParameter(RequestContextInner requestContextInner, Parameter[] parameters) {
        Object[] params = new Object[parameters.length];
        for (int i = 0; i < parameters.length; i++) {
            Parameter parameter = parameters[i];

            if (parameter.getAnnotation(CommandBody.class) != null) {
                params[i] = requestContextInner.body(parameter.getType());
            } else if (parameter.getAnnotation(CommandUrlQueryParam.class) != null) {
                if (parameter.getType().equals(Map.class)) {
                    params[i] = requestContextInner.getParameters();
                } else {
                    params[i] = requestContextInner.bodyForParameter(parameter.getType());
                }
            }
            if (parameter.getType().equals(RequestContext.class)) {
                RequestContext context = new RequestContext(requestContextInner.getBodyByte()
                        , requestContextInner.getHeaders(), requestContextInner.getUrl());
                params[i] = context;
            }
        }
        return params;
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
        if (response.getCode() == 0) {
            if (response.getData() != null) {
                Coder encoder = lookupEncoder(response.getData().getClass(), requestContextType);
                return encoder.encode(response, StandardCharsets.UTF_8);
            }
            return new byte[]{};
        }
        //
        return response.getExceptionMessage() != null
                ? response.getExceptionMessage().getBytes(StandardCharsets.UTF_8)
                : new byte[]{};
    }
}
