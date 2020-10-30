package io.elves.core.command;

import io.elves.core.handle.CommandHandler;
import io.elves.core.request.HttpMethod;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CommandMetadata {
    private CommandHandler handler;
    private String name;
    private String desc;
    private boolean async;
    private HttpMethod httpMethod;
    private String respEncoderType;
}
