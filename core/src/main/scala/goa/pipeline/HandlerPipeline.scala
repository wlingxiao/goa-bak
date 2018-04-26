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

private[pipeline] class TailHandler extends Handler {
  override def apply(ctx: HandlerContext): Unit = {

  }
}

class HandlerPipeline {

  val head: HandlerContext = new HandlerContext

  val tail: HandlerContext = new HandlerContext

  head.handler = new HeadHandler
  tail.handler = new TailHandler
  head.nextCtx = tail
  tail.prevCtx = head

  head.pipeline = this
  tail.pipeline = this

  def addLast(handler: Handler): HandlerPipeline = {
    val prev = tail.prevCtx
    val newCtx = new HandlerContext()
    newCtx.pipeline = this
    newCtx.handler = handler
    newCtx.prevCtx = prev
    newCtx.nextCtx = tail
    prev.nextCtx = newCtx
    tail.prevCtx = newCtx
    this
  }

  def sendInboundCommand(): Unit = {
    head.handler.apply(head)
  }
}
