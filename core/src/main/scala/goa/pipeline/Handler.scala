package goa.pipeline

import java.nio.ByteBuffer

import scala.concurrent.Future

trait Handler extends (HandlerContext => Unit) {
  override def apply(ctx: HandlerContext): Unit

  def channelRead(ctx: HandlerContext): Future[ByteBuffer] = {
    ctx.read()
  }

  def channelWrite(ctx: HandlerContext, msg: Object): Future[Unit] = {
    ctx.write(msg)
  }

}