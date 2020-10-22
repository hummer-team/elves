package io.elves.core.command;

import com.google.common.collect.ImmutableMap;
import io.elves.core.handle.CommandHandle;
import io.elves.core.util.ServiceLoaderUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class CommandHandlerContainer {
    private static final CommandHandlerContainer INSTANCE = new CommandHandlerContainer();
    private final static Map<String, CommandHandle> HANDLER_MAP = new ConcurrentHashMap<>();
    private final ServiceLoader<CommandHandle> serviceLoader = ServiceLoaderUtil.getServiceLoader(CommandHandle.class);

    public static CommandHandlerContainer getInstance() {
        return INSTANCE;
    }

    public Map<String, CommandHandle> namedHandlers() {
        Map<String, CommandHandle> map = new HashMap<>(16);
        for (CommandHandle handler : serviceLoader) {
            String name = parseCommandName(handler);
            if (!StringUtils.isEmpty(name)) {
                map.put(name, handler);
            }
        }
        log.debug("load command handle item count: {}", map.size());
        return ImmutableMap.copyOf(map);
    }

    private void registerCommand(String commandName, CommandHandle handler) {
        if (StringUtils.isEmpty(commandName) || handler == null) {
            return;
        }

        if (HANDLER_MAP.containsKey(commandName)) {
            log.warn("[NettyHttpCommandCenter] Register failed (duplicate command): {}", commandName);
            return;
        }
        HANDLER_MAP.put(commandName, handler);
        log.debug("[{} -> {}] command handle register done.", commandName, handler.getClass().getName());
    }

    public CommandHandle getHandle(String name) {
        return HANDLER_MAP.get(name);
    }

    public void registerHandle() {
        Map<String, CommandHandle> handleMap = namedHandlers();
        for (Map.Entry<String, CommandHandle> handleEntry : handleMap.entrySet()) {
            registerCommand(parseCommandName(handleEntry.getValue()), handleEntry.getValue());
        }
    }

    private String parseCommandName(CommandHandle handler) {
        CommandMapping commandMapping = handler.getClass().getAnnotation(CommandMapping.class);
        if (commandMapping != null) {
            return String.format("%s-%s", commandMapping.httpMethod(), commandMapping.name());
        } else {
            return null;
        }
    }
}
