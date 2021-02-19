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
    private final static Map<String, CommandConext> HANDLER_MAP = new ConcurrentHashMap<>();
    private final ServiceLoader<CommandHandler> serviceLoader = ServiceLoaderUtil.getServiceLoader(CommandHandler.class);

    public static CommandHandlerContainer getInstance() {
        return INSTANCE;
    }

    public void destroyCommandResource() {
        for (Map.Entry<String, CommandConext> handler : HANDLER_MAP.entrySet()) {
            handler.getValue().getHandler().destroy();
            log.debug("destroy {} command handler resource", handler.getKey());
        }
        HANDLER_MAP.clear();
    }

    public Map<String, CommandConext> namedHandlers() {
        Map<String, CommandConext> map = new HashMap<>(16);
        for (CommandHandler handler : serviceLoader) {
            CommandConext metadata = parseCommand(handler);
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

    public void registerCommand(String commandName, CommandConext commandConext) {
        if (StringUtils.isEmpty(commandName) || commandConext == null) {
            return;
        }

        if (HANDLER_MAP.containsKey(commandName)) {
            log.warn("register failed (duplicate command): {}", commandName);
            throw new RuntimeException(String.format("command Register failed (duplicate command) %s", commandName));
        }
        HANDLER_MAP.put(commandName, commandConext);
        log.debug("[{} -> {}] register done,init done.",commandName,commandConext);
    }

    public CommandHandler getHandle(String name) {
        return HANDLER_MAP.get(name).getHandler();
    }

    public CommandConext getCommandContext(String name) {
        return HANDLER_MAP.get(name);
    }

    public void registerHandle() {
        Map<String, CommandConext> handleMap = namedHandlers();
        for (Map.Entry<String, CommandConext> handleEntry : handleMap.entrySet()) {
            registerCommand(handleEntry.getKey(), handleEntry.getValue());
            initCommandHandle(handleEntry.getValue().getHandler());
        }
    }

    private void initCommandHandle(CommandHandler handler) {
        handler.init();
    }

    private String parseCommandName(CommandHandler handler) {
        CommandActionMapping commandMapping = handler.getClass().getAnnotation(CommandActionMapping.class);
        if (commandMapping != null) {
            return String.format("%s-%s", commandMapping.httpMethod(), commandMapping.name());
        } else {
            return null;
        }
    }

    private CommandConext parseCommand(CommandHandler handler) {
        CommandActionMapping commandMapping = handler.getClass().getAnnotation(CommandActionMapping.class);
        if (commandMapping == null) {
            return null;
        }

        return new CommandConext(handler
                , String.format("%s-%s", commandMapping.httpMethod(), commandMapping.name())
                , commandMapping.desc()
                , commandMapping.async()
                , commandMapping.httpMethod()
                , commandMapping.respEncoderType()
                , null
                , null);
    }
}
