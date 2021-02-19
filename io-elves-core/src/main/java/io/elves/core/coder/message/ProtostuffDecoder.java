package io.elves.core.coder.message;

import io.elves.core.coder.ProtostuffCoder;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;

import java.nio.charset.StandardCharsets;
import java.util.List;

public class ProtostuffDecoder extends MessageToMessageDecoder<ByteBuf> {

    /**
     * Decode from one message to an other. This method will be called for each written message that can be handled
     * by this decoder.
     *
     * @param ctx           the {@link ChannelHandlerContext} which this {@link MessageToMessageDecoder} belongs to
     * @param msg           the message to decode to an other one
     * @param out           the {@link List} to which decoded messages should be added
     * @throws Exception    is thrown if an error occurs
     */
    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf msg, List<Object> out) throws Exception {
        byte[] body = new byte[msg.readableBytes()];
        msg.readBytes(body);

        out.add(ProtostuffCoder.INSTANCE.decode(body, msg.getClass(), StandardCharsets.UTF_8));
    }
}
