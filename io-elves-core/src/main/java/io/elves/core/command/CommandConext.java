package io.elves.core.command;

import io.elves.core.handle.CommandHandler;
import io.elves.core.request.HttpMethod;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.lang.reflect.Method;

@Getter
@AllArgsConstructor
public class CommandConext {
    private final CommandHandler handler;
    private final String name;
    private final String desc;
    private final boolean async;
    private final HttpMethod httpMethod;
    private final String respEncoderType;
    private final Method method;
    private final Object targetCommandObject;
}
