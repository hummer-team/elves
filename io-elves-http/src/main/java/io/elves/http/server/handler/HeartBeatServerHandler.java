package io.elves.http.server.handler;

import io.elves.core.properties.ElvesProperties;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import lombok.extern.slf4j.Slf4j;

/**
 * close idle channel
 *
 * @author edz
 */
@Slf4j
public class HeartBeatServerHandler extends ChannelInboundHandlerAdapter {
    /**
     * Calls {@link ChannelHandlerContext#fireUserEventTriggered(Object)} to forward
     * to the next {@link ChannelInboundHandler} in the {@link ChannelPipeline}.
     * <p>
     * Sub-classes may override this method to change behavior.
     *
     * @param ctx
     * @param evt
     */
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            boolean rIdle = ((IdleStateEvent) evt).state() == IdleState.READER_IDLE;
            if (rIdle) {
                log.debug("close this idle read {} s - write {} s channel: {}"
                        , ElvesProperties.getIdleReadTimeOutSecond()
                        , ElvesProperties.getIdleWriteTimeoutSecond()
                        , ctx.channel());
                ctx.channel().close();
            }
        }
        super.userEventTriggered(ctx, evt);
    }
}
