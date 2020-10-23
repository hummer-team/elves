package io.elves.core.command;

import com.google.common.collect.ImmutableMap;
import io.elves.core.handle.CommandHandler;
import io.elves.core.util.ServiceLoaderUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author lee
 */
@Slf4j
public class CommandHandlerContainer {
    private final static CommandHandlerContainer INSTANCE = new CommandHandlerContainer();
    private final static Map<String, CommandHandler> HANDLER_MAP = new ConcurrentHashMap<>();
    private final ServiceLoader<CommandHandler> serviceLoader = ServiceLoaderUtil.getServiceLoader(CommandHandler.class);

    public static CommandHandlerContainer getInstance() {
        return INSTANCE;
    }

    public void destroyCommandResource() {
        for (Map.Entry<String, CommandHandler> handler : HANDLER_MAP.entrySet()) {
            handler.getValue().destroy();
            log.debug("destroy {} command handler resource", handler.getKey());
        }
        HANDLER_MAP.clear();
    }

    public Map<String, CommandHandler> namedHandlers() {
        Map<String, CommandHandler> map = new HashMap<>(16);
        for (CommandHandler handler : serviceLoader) {
            String name = parseCommandName(handler);
            if (!StringUtils.isEmpty(name)) {
                map.put(name, handler);
            }
        }
        log.debug("load command handle item count: {}", map.size());
        return ImmutableMap.copyOf(map);
    }

    private void registerCommand(String commandName, CommandHandler handler) {
        if (StringUtils.isEmpty(commandName) || handler == null) {
            return;
        }

        if (HANDLER_MAP.containsKey(commandName)) {
            log.warn("[NettyHttpCommandCenter] Register failed (duplicate command): {}", commandName);
            throw new RuntimeException(String.format("command Register failed (duplicate command) %s", commandName));
        }
        HANDLER_MAP.put(commandName, handler);
        log.debug("[{} -> {}] command handle register done.", commandName, handler.getClass().getName());
    }

    public CommandHandler getHandle(String name) {
        return HANDLER_MAP.get(name);
    }

    public void registerHandle() {
        Map<String, CommandHandler> handleMap = namedHandlers();
        for (Map.Entry<String, CommandHandler> handleEntry : handleMap.entrySet()) {
            registerCommand(parseCommandName(handleEntry.getValue()), handleEntry.getValue());
            initCommandHandle(handleEntry.getValue());
        }
    }

    private void initCommandHandle(CommandHandler handler) {
        handler.init();
        log.debug("handler {} init done.", handler);
    }

    private String parseCommandName(CommandHandler handler) {
        CommandMapping commandMapping = handler.getClass().getAnnotation(CommandMapping.class);
        if (commandMapping != null) {
            return String.format("%s-%s", commandMapping.httpMethod(), commandMapping.name());
        } else {
            return null;
        }
    }
}
