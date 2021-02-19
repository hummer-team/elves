package io.elves.core.coder.message;

import io.elves.core.coder.ProtostuffCoder;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import io.netty.handler.codec.MessageToMessageEncoder;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static io.elves.core.ElvesConstants.DELIMITER;

public class ProtostuffEncoder extends MessageToMessageEncoder<Object> {
    /**
     * Encode a message into a {@link ByteBuf}. This method will be called for each written message that can be handled
     * by this encoder.
     *
     * @param ctx the {@link ChannelHandlerContext} which this {@link MessageToByteEncoder} belongs to
     * @param msg the message to encode
     * @param out the {@link ByteBuf} into which the encoded message will be written
     * @throws Exception is thrown if an error occurs
     */
//    @Override
//    protected void encode(ChannelHandlerContext ctx, Object msg, ByteBuf out) throws Exception {
//        byte[] bytes = ProtostuffCoder.INSTANCE.encode(msg, StandardCharsets.UTF_8);
//
//        byte[] total = new byte[bytes.length + DELIMITER.length];
//        System.arraycopy(bytes, 0, total, 0, bytes.length);
//        System.arraycopy(DELIMITER, 0, total, bytes.length, DELIMITER.length);
//
//        out.writeBytes(total);
//    }

    /**
     * Encode from one message to an other. This method will be called for each written message that can be handled
     * by this encoder.
     *
     * @param ctx the {@link ChannelHandlerContext} which this {@link MessageToMessageEncoder} belongs to
     * @param msg the message to encode to an other one
     * @param out the {@link List} into which the encoded msg should be added
     *            needs to do some kind of aggregation
     * @throws Exception is thrown if an error occurs
     */
    @Override
    protected void encode(ChannelHandlerContext ctx, Object msg, List<Object> out) throws Exception {
        byte[] bytes = ProtostuffCoder.INSTANCE.encode(msg, StandardCharsets.UTF_8);

        byte[] total = new byte[bytes.length + DELIMITER.length];
        System.arraycopy(bytes, 0, total, 0, bytes.length);
        System.arraycopy(DELIMITER, 0, total, bytes.length, DELIMITER.length);
        out.add(total);
    }
}
