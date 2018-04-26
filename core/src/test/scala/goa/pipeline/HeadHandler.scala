package goa.pipeline

import java.nio.ByteBuffer

import scala.concurrent.Future

private[pipeline] class HeadHandler extends Handler {
  override def apply(ctx: HandlerContext): Unit = {
    ctx.next()
  }

  override def channelWrite(ctx: HandlerContext, msg: Object): Future[Unit] = {
    Future.successful(())
  }

  override def channelRead(ctx: HandlerContext): Future[ByteBuffer] = {
    Future.successful(ByteBuffer.allocate(10))
  }
}
