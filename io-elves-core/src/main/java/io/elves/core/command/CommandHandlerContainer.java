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
    private final static Map<String, CommandMetadata> HANDLER_MAP = new ConcurrentHashMap<>();
    private final ServiceLoader<CommandHandler> serviceLoader = ServiceLoaderUtil.getServiceLoader(CommandHandler.class);

    public static CommandHandlerContainer getInstance() {
        return INSTANCE;
    }

    public void destroyCommandResource() {
        for (Map.Entry<String, CommandMetadata> handler : HANDLER_MAP.entrySet()) {
            handler.getValue().getHandler().destroy();
            log.debug("destroy {} command handler resource", handler.getKey());
        }
        HANDLER_MAP.clear();
    }

    public Map<String, CommandMetadata> namedHandlers() {
        Map<String, CommandMetadata> map = new HashMap<>(16);
        for (CommandHandler handler : serviceLoader) {
            CommandMetadata metadata = parseCommand(handler);
            if (metadata != null) {
                if (map.containsKey(metadata.getName())) {
                    log.warn("Register failed (duplicate command): {}"
                            , metadata.getName());
                    throw new RuntimeException(String.format("command Register failed (duplicate command) %s"
                            , metadata.getName()));
                }
                map.put(metadata.getName(), metadata);
            }
        }
        log.debug("load command handle item count: {}", map.size());
        return ImmutableMap.copyOf(map);
    }

    private void registerCommand(String commandName, CommandMetadata commandMetadata) {
        if (StringUtils.isEmpty(commandName) || commandMetadata == null) {
            return;
        }

        if (HANDLER_MAP.containsKey(commandName)) {
            log.warn("[NettyHttpCommandCenter] Register failed (duplicate command): {}", commandName);
            throw new RuntimeException(String.format("command Register failed (duplicate command) %s", commandName));
        }
        HANDLER_MAP.put(commandName, commandMetadata);
    }

    public CommandHandler getHandle(String name) {
        return HANDLER_MAP.get(name).getHandler();
    }

    public CommandMetadata getMetadata(String name) {
        return HANDLER_MAP.get(name);
    }

    public void registerHandle() {
        Map<String, CommandMetadata> handleMap = namedHandlers();
        for (Map.Entry<String, CommandMetadata> handleEntry : handleMap.entrySet()) {
            registerCommand(handleEntry.getKey(), handleEntry.getValue());
            initCommandHandle(handleEntry.getValue().getHandler());
            log.debug("[{} -> {}] register done,init done.", handleEntry.getKey(), handleEntry.getValue());
        }
    }

    private void initCommandHandle(CommandHandler handler) {
        handler.init();
    }

    private String parseCommandName(CommandHandler handler) {
        CommandMapping commandMapping = handler.getClass().getAnnotation(CommandMapping.class);
        if (commandMapping != null) {
            return String.format("%s-%s", commandMapping.httpMethod(), commandMapping.name());
        } else {
            return null;
        }
    }

    private CommandMetadata parseCommand(CommandHandler handler) {
        CommandMapping commandMapping = handler.getClass().getAnnotation(CommandMapping.class);
        if (commandMapping == null) {
            return null;
        }

        return new CommandMetadata(handler
                , String.format("%s-%s", commandMapping.httpMethod(), commandMapping.name())
                , commandMapping.desc()
                , commandMapping.async()
                , commandMapping.httpMethod()
                , commandMapping.respEncoderType());
    }
}
